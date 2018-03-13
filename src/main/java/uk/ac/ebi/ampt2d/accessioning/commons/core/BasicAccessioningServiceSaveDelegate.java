package uk.ac.ebi.ampt2d.accessioning.commons.core;

import org.springframework.dao.DataIntegrityViolationException;
import uk.ac.ebi.ampt2d.accessioning.commons.generators.ModelHashAccession;
import uk.ac.ebi.ampt2d.accessioning.commons.persistence.DatabaseService;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * This class is delegate for {@link BasicAccessioningService} to manage the save operation.
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