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
package uk.ac.ebi.ampt2d.accession.service;

import org.springframework.data.util.Pair;
import uk.ac.ebi.ampt2d.accession.serial.block.MonotonicRange;
import uk.ac.ebi.ampt2d.accession.serial.block.MonotonicRangePriorityQueue;
import uk.ac.ebi.ampt2d.accession.serial.block.persistence.entities.ContiguousIdBlock;
import uk.ac.ebi.ampt2d.accession.service.exceptions.AccessionIsNotPending;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * This class holds the state of the monotonic id blocks used at this moment on the application.
 * This class is not thread safe.
 */
class BlockState {

    private final PriorityQueue<ContiguousIdBlock> assignedBlocks;

    private final MonotonicRangePriorityQueue availableRanges;

    private final HashSet<Long> generatedAccessions;

    private final PriorityQueue<Long> committedAccessions;

    public BlockState() {
        this.assignedBlocks = new PriorityQueue<>(ContiguousIdBlock::compareTo);
        this.availableRanges = new MonotonicRangePriorityQueue();
        this.generatedAccessions = new HashSet<>();
        this.committedAccessions = new PriorityQueue<>(Long::compareTo);
    }

    public void addBlock(ContiguousIdBlock block) {
        assignedBlocks.offer(block);
        availableRanges.add(new MonotonicRange(block.getLastCommitted() + 1, block.getEnd()));
    }

    public MonotonicRangePriorityQueue getAvailableRanges() {
        return availableRanges;
    }

    /**
     * Polls the next continuous array of monotonic values. Array size will be less or equal than @param maxValues
     *
     * @param maxValues
     * @return
     */
    public long[] pollNextMonotonicValues(int maxValues) {
        MonotonicRange monotonicRange = pollNextMonotonicRange(maxValues);
        long[] ids = monotonicRange.getIds();
        generatedAccessions.addAll(LongStream.of(ids).boxed().collect(Collectors.toList()));
        return ids;
    }

    /**
     * Polls the next monotonic range to use. If the next available range is larger than @param maxSize, then splits
     * the range and returns the left part with @param maxSize elements and returns the right part to the pool of
     * available ranges.
     *
     * @param maxSize
     * @return
     */
    private MonotonicRange pollNextMonotonicRange(int maxSize) {
        MonotonicRange monotonicRange = availableRanges.poll();
        if (monotonicRange.getTotalOfValues() > maxSize) {
            Pair<MonotonicRange, MonotonicRange> splitResult = monotonicRange.split(maxSize);
            monotonicRange = splitResult.getFirst();
            availableRanges.add(splitResult.getSecond());
        }

        return monotonicRange;
    }

    public boolean isAvailableSpaceLessThan(int totalAccessionsToGenerate) {
        return availableRanges.getTotalOfValuesInQueue() < totalAccessionsToGenerate;
    }

    private void addToCommitted(long[] accessions) {
        for (long accession : accessions) {
            committedAccessions.offer(accession);
            generatedAccessions.remove(accession);
        }
    }

    public List<ContiguousIdBlock> commit(long[] accessions) throws AccessionIsNotPending {
        assertAccessionIsPending(accessions);
        return doCommit(accessions);
    }

    private void assertAccessionIsPending(long[] accessions) throws AccessionIsNotPending {
        for (long accession : accessions) {
            if (!generatedAccessions.contains(accession)) {
                throw new AccessionIsNotPending(accession);
            }
        }
    }

    private List<ContiguousIdBlock> doCommit(long[] accessions) {
        List<ContiguousIdBlock> blocksToUpdate = new ArrayList<>();

        addToCommitted(accessions);

        ContiguousIdBlock block = assignedBlocks.peek();
        long lastCommitted = block.getLastCommitted();
        while (committedAccessions.peek() != null && committedAccessions.peek() == lastCommitted + 1) {
            lastCommitted++;
            committedAccessions.poll();
            if (lastCommitted == block.getEnd()) {
                assignedBlocks.poll();
                block.setLastCommitted(lastCommitted);
                blocksToUpdate.add(block);
                block = assignedBlocks.peek();
                lastCommitted = block.getLastCommitted();
            }
        }

        if (lastCommitted != block.getLastCommitted()) {
            block.setLastCommitted(lastCommitted);
            blocksToUpdate.add(block);
        }

        return blocksToUpdate;
    }

    public void release(long[] accessions) throws AccessionIsNotPending {
        assertAccessionIsPending(accessions);
        doRelease(accessions);
    }

    private void doRelease(long[] accessions) {
        availableRanges.addAll(MonotonicRange.convertToMonotonicRanges(accessions));
        generatedAccessions.removeAll(LongStream.of(accessions).boxed().collect(Collectors.toList()));
    }

    /**
     * This function will recover the internal state of committed elements and will remove them from the available
     * ranges.
     *
     * @param committedElements
     * @throws AccessionIsNotPending
     */
    public void recoverState(long[] committedElements) throws AccessionIsNotPending {
        List<MonotonicRange> ranges = MonotonicRange.convertToMonotonicRanges(committedElements);
        List<MonotonicRange> newAvailableRanges = new ArrayList<>();
        for (MonotonicRange monotonicRange : this.availableRanges) {
            newAvailableRanges.addAll(monotonicRange.excludeIntersections(ranges));
        }

        this.availableRanges.clear();
        this.availableRanges.addAll(newAvailableRanges);
        doCommit(committedElements);
    }
}
