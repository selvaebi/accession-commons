package uk.ac.ebi.ampt2d.commons.accession.core;

import org.springframework.dao.DataIntegrityViolationException;
import uk.ac.ebi.ampt2d.commons.accession.generators.ModelHashAccession;
import uk.ac.ebi.ampt2d.commons.accession.persistence.DatabaseService;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * This class is delegate for {@link BasicAccessioningService} to manage the save operation.
 *
 * The save operation can fail when an object has been already accessioned with a different value while having the
 * same hash message and elements. In this case a database constraint exception can be raised. Due to limited
 * information in those cases of error on JDBC driver, the save operation is implemented as an iterative version of a
 * binary partition.
 *
 * If the save operation works, all elements have been stored, otherwise, because the save operation is expected to
 * be transactional, the saved elements are dropped from database. And we split in two the batch of elements to save
 * and try to save each part separately until all the split batches have been saved correctly and we find the specific
 * elements that provoke error.
 *
 * @param <MODEL>
 * @param <HASH>
 * @param <ACCESSION>
 */
class BasicAccessioningServiceSaveDelegate<MODEL, HASH, ACCESSION> {

    private class Partition {

        private int start;
        private int end;

        public Partition(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private final DatabaseService<MODEL, HASH, ACCESSION> dbService;

    public BasicAccessioningServiceSaveDelegate(DatabaseService<MODEL, HASH, ACCESSION> dbService) {
        this.dbService = dbService;
    }

    public SaveResponse<ACCESSION, MODEL> doSaveAccessions(
            List<ModelHashAccession<MODEL, HASH, ACCESSION>> modelHashAccessions) {
        Stack<Partition> partitions = new Stack<>();
        partitions.add(new Partition(0, modelHashAccessions.size()));
        HashMap<ACCESSION, MODEL> savedAccessions = new HashMap<ACCESSION, MODEL>();
        HashMap<ACCESSION, MODEL> notSavedAccessions = new HashMap<ACCESSION, MODEL>();

        while (!partitions.isEmpty()) {
            Partition partition = partitions.pop();
            final List<ModelHashAccession<MODEL, HASH, ACCESSION>> partitionToSave = modelHashAccessions
                    .subList(partition.start, partition.end);
            try {
                dbService.save(partitionToSave);
                partitionToSave.stream().forEach(mha -> savedAccessions.put(mha.accession(), mha.model()));
            } catch (DataIntegrityViolationException e) {
                if (partitionToSave.size() == 1) {
                    notSavedAccessions.put(partitionToSave.get(0).accession(), partitionToSave.get(0).model());
                } else {
                    int start = partition.start;
                    int middle = (partition.end - partition.start) / 2;
                    int end = partition.end;
                    partitions.add(new Partition(start, middle));
                    partitions.add(new Partition(middle, end));
                }
            }
        }
        return new SaveResponse<>(savedAccessions, notSavedAccessions);
    }
}