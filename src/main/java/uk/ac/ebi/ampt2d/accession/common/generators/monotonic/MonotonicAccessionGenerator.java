/*
 *
 * Copyright 2018 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.ampt2d.accession.common.generators.monotonic;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.ampt2d.accession.common.generators.AccessionGenerator;
import uk.ac.ebi.ampt2d.accession.common.generators.ModelHashAccession;
import uk.ac.ebi.ampt2d.accession.common.accessioning.SaveResponse;
import uk.ac.ebi.ampt2d.accession.common.generators.monotonic.persistence.entities.ContiguousIdBlock;
import uk.ac.ebi.ampt2d.accession.common.generators.monotonic.persistence.repositories.ContiguousIdBlockRepository;
import uk.ac.ebi.ampt2d.accession.common.generators.monotonic.exceptions.AccessionIsNotPending;
import uk.ac.ebi.ampt2d.accession.common.utils.ExponentialBackOff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates monotonically increasing ids for type of objects across multiple application instances. Each
 * application reserves blocks with a serialized transaction. Each reserved block contains an id of the application
 * instance, an id for type of object and a counter to keep track of the confirmed generated ids.
 * <p>
 * In case of application restart, the previous application state can be loaded with {@link #recoverState(long[])}
 */
public class MonotonicAccessionGenerator<MODEL> implements AccessionGenerator<MODEL, Long> {

    private long blockSize;

    private String categoryId;

    private String applicationInstanceId;

    private ContiguousIdBlockRepository contiguousIdBlockRepository;

    private final BlockManager blockManager;

    public MonotonicAccessionGenerator(long blockSize, String categoryId, String applicationInstanceId,
                                       ContiguousIdBlockRepository contiguousIdBlockRepository) {
        this.blockSize = blockSize;
        this.categoryId = categoryId;
        this.applicationInstanceId = applicationInstanceId;
        this.contiguousIdBlockRepository = contiguousIdBlockRepository;
        this.blockManager = new BlockManager();
        loadIncompleteBlocks();
    }

    private void loadIncompleteBlocks() {
        List<ContiguousIdBlock> uncompletedBlocks = getUncompletedBlocksForThisInstanceOrdered();
        //Insert as available ranges
        for (ContiguousIdBlock block : uncompletedBlocks) {
            blockManager.addBlock(block);
        }
    }

    /**
     * TODO this can be changed to a query to recover and avoid the filter
     */
    private List<ContiguousIdBlock> getUncompletedBlocksForThisInstanceOrdered() {
        try (Stream<ContiguousIdBlock> reservedBlocksOfThisInstance = contiguousIdBlockRepository
                .findAllByCategoryIdAndApplicationInstanceIdOrderByEndAsc(categoryId, applicationInstanceId)) {
            return reservedBlocksOfThisInstance.filter(ContiguousIdBlock::isNotFull).collect(Collectors.toList());
        }
    }

    /**
     * This function will recover the internal state of committed elements and will remove them from the available
     * ranges.
     *
     * @param committedElements
     * @throws AccessionIsNotPending
     */
    public synchronized void recoverState(long[] committedElements) throws AccessionIsNotPending {
        blockManager.recoverState(committedElements);
    }

    public synchronized long[] generateAccessions(int numAccessionsToGenerate) {
        long[] accessions = new long[numAccessionsToGenerate];
        reserveNewBlocksUntilSizeIs(numAccessionsToGenerate);

        int i = 0;
        while (i < numAccessionsToGenerate) {
            int remainingAccessionsToGenerate = numAccessionsToGenerate - i;
            long[] ids = blockManager.pollNext(remainingAccessionsToGenerate);
            System.arraycopy(ids, 0, accessions, i, ids.length);
            i += ids.length;
        }
        assert (i == numAccessionsToGenerate);

        return accessions;
    }

    /**
     * Ensures that the available ranges queue hold @param totalAccessionsToGenerate or more elements
     *
     * @param totalAccessionsToGenerate
     */
    private synchronized void reserveNewBlocksUntilSizeIs(int totalAccessionsToGenerate) {
        while (!blockManager.hasAvailableAccessions(totalAccessionsToGenerate)) {
            try {
                ExponentialBackOff.execute(() -> reserveNewBlock(categoryId, applicationInstanceId, blockSize), 10, 30);
            } catch (RuntimeException e) {
                // Ignore, max backoff have been reached, we will try again until we can reserve blocks
            }
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    protected synchronized void reserveNewBlock(String categoryId, String instanceId, long size) {
        ContiguousIdBlock lastBlock = contiguousIdBlockRepository.findFirstByCategoryIdOrderByEndDesc(categoryId);
        if (lastBlock != null) {
            blockManager.addBlock(contiguousIdBlockRepository.save(lastBlock.nextBlock(instanceId, size)));
        } else {
            ContiguousIdBlock newBlock = new ContiguousIdBlock(categoryId, instanceId, 0, size);
            blockManager.addBlock(contiguousIdBlockRepository.save(newBlock));
        }
    }

    public synchronized void commit(long... accessions) throws AccessionIsNotPending {
        contiguousIdBlockRepository.save(blockManager.commit(accessions));
    }

    public synchronized void release(long... accessions) throws AccessionIsNotPending {
        blockManager.release(accessions);
    }

    public synchronized MonotonicRangePriorityQueue getAvailableRanges() {
        return blockManager.getAvailableRanges();
    }

    @Override
    public <HASH> List<ModelHashAccession<MODEL, HASH, Long>> generateAccessions(Map<HASH, MODEL> messages) {
        long[] accessions = generateAccessions(messages.size());
        int i = 0;
        List<ModelHashAccession<MODEL, HASH, Long>> messageHashAccession = new ArrayList<>();
        for (Map.Entry<HASH, ? extends MODEL> entry : messages.entrySet()) {
            messageHashAccession.add(ModelHashAccession.of(entry.getValue(), entry.getKey(), accessions[i]));
        }

        return messageHashAccession;
    }

    @Override
    public synchronized void postSave(SaveResponse<Long, MODEL> response) {
        commit(response.getSavedAccessions().keySet().stream().mapToLong(l -> l).toArray());
        release(response.getUnsavedAccessions().keySet().stream().mapToLong(l -> l).toArray());
    }
}
