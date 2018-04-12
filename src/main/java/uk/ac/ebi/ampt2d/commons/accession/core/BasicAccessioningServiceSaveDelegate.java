package uk.ac.ebi.ampt2d.commons.accession.core;

import org.springframework.dao.DataIntegrityViolationException;
import uk.ac.ebi.ampt2d.commons.accession.persistence.DatabaseService;

import java.util.List;
import java.util.Stack;

/**
 * This class is delegate for {@link BasicAccessioningService} to manage the save operation.
 * <p>
 * The save operation can fail when an object has been already accessioned with a different value while having the
 * same hash message and elements. In this case a database constraint exception can be raised. Due to limited
 * information in those cases of error on JDBC driver, the save operation is implemented as an iterative version of a
 * binary partition.
 * <p>
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

    /**
     * Perform save operation, return saved elements
     *
     * @param accessionedModels
     * @return
     */
    public SaveResponse<ACCESSION> doSaveAccessionedModels(
            List<AccessionModel<MODEL, HASH, ACCESSION>> accessionedModels) {
        Stack<Partition> partitions = new Stack<>();
        partitions.add(new Partition(0, accessionedModels.size()));
        SaveResponse<ACCESSION> saveResponse = new SaveResponse<>();

        while (!partitions.isEmpty()) {
            Partition partition = partitions.pop();
            final List<AccessionModel<MODEL, HASH, ACCESSION>> partitionToSave = accessionedModels
                    .subList(partition.start, partition.end);
            try {
                dbService.save(partitionToSave);
                partitionToSave.stream().forEach(saveResponse::addSavedAccessions);
            } catch (DataIntegrityViolationException e) {
                if (partitionToSave.size() != 1) {
                    int start = partition.start;
                    int middle = (partition.end - partition.start) / 2;
                    int end = partition.end;
                    partitions.add(new Partition(start, middle));
                    partitions.add(new Partition(middle, end));
                }else{
                    saveResponse.addSaveFailedAccession(partitionToSave.get(0));
                }
            }
        }
        return saveResponse;
    }
}