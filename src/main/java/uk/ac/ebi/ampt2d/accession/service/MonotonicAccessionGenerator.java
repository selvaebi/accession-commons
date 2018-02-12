/*
 *
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ampt2d.accession.service;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.ampt2d.accession.serial.block.MonotonicRangePriorityQueue;
import uk.ac.ebi.ampt2d.accession.serial.block.persistence.entities.ContiguousIdBlock;
import uk.ac.ebi.ampt2d.accession.serial.block.persistence.repositories.ContiguousIdBlockRepository;
import uk.ac.ebi.ampt2d.accession.service.exceptions.AccessionIsNotPending;
import uk.ac.ebi.ampt2d.accession.utils.ExponentialBackOff;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Monotonic accession, generates blocks of ids on database with a blocking serialized transaction. Each block
 * contains a applicationInstanceId that identifies which application instance has created the block and a categoryId
 * to identify the type of resource. After acquiring a block, the application stores the counter in the block with a
 * non blocking operation. To reduce the load in the database, the application uses a cache in memory with the
 * current state of block usage.
 */
public class MonotonicAccessionGenerator {

    private long blockSize;

    private String categoryId;

    private String applicationInstanceId;

    private ContiguousIdBlockRepository contiguousIdBlockRepository;

    private final BlockState blockState;

    public MonotonicAccessionGenerator(long blockSize, String categoryId, String applicationInstanceId,
                                       ContiguousIdBlockRepository contiguousIdBlockRepository) {
        this.blockSize = blockSize;
        this.categoryId = categoryId;
        this.applicationInstanceId = applicationInstanceId;
        this.contiguousIdBlockRepository = contiguousIdBlockRepository;
        this.blockState = new BlockState();
        loadIncompleteBlocks();
    }

    private void loadIncompleteBlocks() {
        List<ContiguousIdBlock> uncompletedBlocks = getUncompletedBlocksForThisInstanceOrdered();
        //Insert as available ranges
        for (ContiguousIdBlock block : uncompletedBlocks) {
            blockState.addBlock(block);
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
        blockState.recoverState(committedElements);
    }

    public synchronized long[] generateAccessions(int numAccessionsToGenerate) {
        long[] accessions = new long[numAccessionsToGenerate];
        reserveNewBlocksUntilSizeIs(numAccessionsToGenerate);

        int i = 0;
        while (i < numAccessionsToGenerate) {
            int remainingAccessionsToGenerate = numAccessionsToGenerate - i;
            long[] ids = blockState.pollNextMonotonicValues(remainingAccessionsToGenerate);
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
        while (blockState.isAvailableSpaceLessThan(totalAccessionsToGenerate)) {
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
            blockState.addBlock(contiguousIdBlockRepository.save(lastBlock.nextBlock(instanceId, size)));
        } else {
            ContiguousIdBlock newBlock = new ContiguousIdBlock(categoryId, instanceId, 0, size);
            blockState.addBlock(contiguousIdBlockRepository.save(newBlock));
        }
    }

    public synchronized void commit(long... accessions) throws AccessionIsNotPending {
        contiguousIdBlockRepository.save(blockState.commit(accessions));
    }

    public synchronized void release(long... accessions) throws AccessionIsNotPending {
        blockState.release(accessions);
    }

    public synchronized MonotonicRangePriorityQueue getAvailableRanges() {
        return blockState.getAvailableRanges();
    }

}
