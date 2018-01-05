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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.ampt2d.accession.serial.block.MonotonicRange;
import uk.ac.ebi.ampt2d.accession.serial.block.MonotonicRangePriorityQueue;
import uk.ac.ebi.ampt2d.accession.serial.block.persistence.entities.ContinuousIdBlock;
import uk.ac.ebi.ampt2d.accession.serial.block.persistence.repositories.ContinuousIdBlockRepository;
import uk.ac.ebi.ampt2d.accession.utils.ExponentialBackOff;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MonotonicAccessionGenerator implements InitializingBean {

    private long blockSize;

    private String categoryId;

    private String instanceId;

    private ContinuousIdBlockRepository continuousIdBlockRepository;

    private final PriorityQueue<ContinuousIdBlock> activeBlocks;

    private final MonotonicRangePriorityQueue availableRanges;

    private final PriorityQueue<Long> committed;

    public MonotonicAccessionGenerator(long blockSize, String categoryId, String instanceId, ContinuousIdBlockRepository continuousIdBlockRepository) {
        this.blockSize = blockSize;
        this.categoryId = categoryId;
        this.instanceId = instanceId;
        this.continuousIdBlockRepository = continuousIdBlockRepository;
        this.activeBlocks = new PriorityQueue<>(ContinuousIdBlock::compareTo);
        this.availableRanges = new MonotonicRangePriorityQueue();
        this.committed = new PriorityQueue<>(Long::compareTo);
    }

    public synchronized long[] generateAccessions(int totalAccessionsToGenerate) {
        long[] accessions = new long[totalAccessionsToGenerate];
        increaseAvailableRangesUntilSizeIs(totalAccessionsToGenerate);

        int i = 0;
        while (i != totalAccessionsToGenerate) {
            int remainingAccessionsToGenerate = totalAccessionsToGenerate - i;
            MonotonicRange monotonicRange = pollNextMonotonicRange(remainingAccessionsToGenerate);
            int idsInRange = monotonicRange.getTotalOfValues();
            System.arraycopy(monotonicRange.getIds(), 0, accessions, i, idsInRange);
            i += idsInRange;
        }
        return accessions;
    }

    public synchronized List<MonotonicRange> generateAccessionRanges(int totalAccessionsToGenerate) {
        List<MonotonicRange> accessionRanges = new ArrayList<>();
        increaseAvailableRangesUntilSizeIs(totalAccessionsToGenerate);

        int i = 0;
        while (i != totalAccessionsToGenerate) {
            int remainingAccessionsToGenerate = totalAccessionsToGenerate - i;
            MonotonicRange monotonicRange = pollNextMonotonicRange(remainingAccessionsToGenerate);
            accessionRanges.add(monotonicRange);
            i += monotonicRange.getTotalOfValues();
        }
        return accessionRanges;
    }

    /**
     * Polls the next monotonic range to use, if the next available range contains more values than needed, splits
     * the range and returns the excess to available ranges.
     *
     * @param totalValuesNeeded
     * @return
     */
    private synchronized MonotonicRange pollNextMonotonicRange(int totalValuesNeeded) {
        MonotonicRange monotonicRange = availableRanges.poll();
        if (monotonicRange.getTotalOfValues() > totalValuesNeeded) {
            availableRanges.add(monotonicRange.splitRight(totalValuesNeeded));
            monotonicRange = monotonicRange.splitLeft(totalValuesNeeded);
        }
        return monotonicRange;
    }

    /**
     * Ensures that the available ranges queue hold @param totalAccessionsToGenerate or more elements
     *
     * @param totalAccessionsToGenerate
     */
    private synchronized void increaseAvailableRangesUntilSizeIs(int totalAccessionsToGenerate) {
        while (availableRanges.getTotalOfValuesInQueue() < totalAccessionsToGenerate) {
            try {
                ExponentialBackOff.execute(() -> reserveNewBlockBlock(categoryId, instanceId, blockSize), 10, 30);
            } catch (RuntimeException e) {
                // Ignore, max backoff have been reached, we will try again until we can reserve blocks
            }
        }
    }

    /**
     * Initialize the block service. If no block entity found on the database
     */
    @Override
    public void afterPropertiesSet() {
        List<ContinuousIdBlock> uncompletedBlocks = getUncompletedBlocksForThisInstanceOrdered();
        if (uncompletedBlocks.isEmpty()) {
            reserveNewBlockBlock(categoryId, instanceId, blockSize);
        } else {
            //Insert as available ranges
            for (ContinuousIdBlock block : uncompletedBlocks) {
                addBlock(block);
            }
        }
    }

    /**
     * TODO this can be changed to a query to recover and avoid the filter
     */
    private List<ContinuousIdBlock> getUncompletedBlocksForThisInstanceOrdered() {
        try (Stream<ContinuousIdBlock> reservedBlocksOfThisInstance = continuousIdBlockRepository
                .findAllByCategoryIdAndInstanceIdOrderByEndAsc(categoryId, instanceId)) {
            return reservedBlocksOfThisInstance.filter(ContinuousIdBlock::isNotFull).collect(Collectors.toList());
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    protected synchronized void reserveNewBlockBlock(String categoryId, String instanceId, long size) {
        ContinuousIdBlock lastBlock = continuousIdBlockRepository.findFirstByCategoryIdOrderByEnd(categoryId);
        if (lastBlock == null) {
            addBlock(continuousIdBlockRepository.save(new ContinuousIdBlock(categoryId, instanceId, 0, size)));
        } else {
            addBlock(continuousIdBlockRepository.save(lastBlock.nextBlock(instanceId, size)));
        }
    }

    private void addBlock(ContinuousIdBlock block) {
        activeBlocks.offer(block);
        availableRanges.add(new MonotonicRange(block.getLastCommitted() + 1, block.getEnd()));
    }

    public MonotonicRangePriorityQueue getAvailableRanges() {
        return availableRanges;
    }

    public synchronized void recoverState(long[] committedElements) {
        List<MonotonicRange> ranges = MonotonicRange.convertToMonotonicRanges(committedElements);
        List<MonotonicRange> newAvailableRanges = new ArrayList<>();
        for (MonotonicRange monotonicRange : newAvailableRanges) {
            newAvailableRanges.addAll(monotonicRange.exclude(ranges));
        }

        this.availableRanges.clear();
        this.availableRanges.addAll(newAvailableRanges);
        commit(committedElements);
    }

    public synchronized void commit(long... accessions) {
        addToCommited(accessions);

        ContinuousIdBlock block = activeBlocks.peek();
        long lastCommitted = block.getLastCommitted();
        while (committed.peek() != null && lastCommitted + 1 == committed.peek()) {
            lastCommitted++;
            committed.poll();
            if (lastCommitted == block.getEnd()) {
                activeBlocks.poll();
                updateBlockLastCommited(block, lastCommitted);
                block = activeBlocks.peek();
                lastCommitted = block.getLastCommitted();
            }
        }

        if (lastCommitted != block.getLastCommitted()) {
            updateBlockLastCommited(block, lastCommitted);
        }
    }

    private void addToCommited(long[] accessions) {
        for (long accession : accessions) {
            committed.offer(accession);
        }
    }

    private void updateBlockLastCommited(ContinuousIdBlock block, long lastCommitted) {
        block.setLastCommitted(lastCommitted);
        continuousIdBlockRepository.save(block);
    }

    public synchronized void release(long... accessions) {
        availableRanges.addAll(MonotonicRange.convertToMonotonicRanges(accessions));
    }

}
