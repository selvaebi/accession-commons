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
package uk.ac.ebi.ampt2d.commons.accession.generators.monotonic;

import uk.ac.ebi.ampt2d.commons.accession.generators.AccessionGenerator;
import uk.ac.ebi.ampt2d.commons.accession.utils.ExponentialBackOff;
import uk.ac.ebi.ampt2d.commons.accession.utils.exceptions.ExponentialBackOffMaxRetriesRuntimeException;
import uk.ac.ebi.ampt2d.commons.accession.core.SaveResponse;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionCouldNotBeGeneratedException;
import uk.ac.ebi.ampt2d.commons.accession.generators.ModelHashAccession;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionIsNotPending;
import uk.ac.ebi.ampt2d.commons.accession.persistence.monotonic.entities.ContiguousIdBlock;
import uk.ac.ebi.ampt2d.commons.accession.persistence.monotonic.service.ContiguousIdBlockService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private ContiguousIdBlockService blockService;

    private final BlockManager blockManager;

    public MonotonicAccessionGenerator(long blockSize, String categoryId, String applicationInstanceId,
                                       ContiguousIdBlockService contiguousIdBlockService) {
        this.blockSize = blockSize;
        this.categoryId = categoryId;
        this.applicationInstanceId = applicationInstanceId;
        this.blockService = contiguousIdBlockService;
        this.blockManager = new BlockManager();
        loadIncompleteBlocks();
    }

    private void loadIncompleteBlocks() {
        List<ContiguousIdBlock> uncompletedBlocks = blockService
                .getUncompletedBlocksByCategoryIdAndApplicationInstanceIdOrderByEndAsc(categoryId,
                        applicationInstanceId);
        //Insert as available ranges
        for (ContiguousIdBlock block : uncompletedBlocks) {
            blockManager.addBlock(block);
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

    public synchronized long[] generateAccessions(int numAccessionsToGenerate)
            throws AccessionCouldNotBeGeneratedException {
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
            } catch (ExponentialBackOffMaxRetriesRuntimeException e) {
                // Ignore, max backoff have been reached, we will try again until we can reserve blocks
            }
        }
    }

    private synchronized void reserveNewBlock(String categoryId, String instanceId, long size) {
        blockManager.addBlock(blockService.reserveNewBlock(categoryId, instanceId, size));
    }

    public synchronized void commit(long... accessions) throws AccessionIsNotPending {
        blockService.save(blockManager.commit(accessions));
    }

    public synchronized void release(long... accessions) throws AccessionIsNotPending {
        blockManager.release(accessions);
    }

    public synchronized MonotonicRangePriorityQueue getAvailableRanges() {
        return blockManager.getAvailableRanges();
    }

    @Override
    public <HASH> List<ModelHashAccession<MODEL, HASH, Long>> generateAccessions(Map<HASH, MODEL> messages)
            throws AccessionCouldNotBeGeneratedException {
        long[] accessions = generateAccessions(messages.size());
        int i = 0;
        List<ModelHashAccession<MODEL, HASH, Long>> messageHashAccession = new ArrayList<>();
        for (Map.Entry<HASH, ? extends MODEL> entry : messages.entrySet()) {
            messageHashAccession.add(ModelHashAccession.of(entry.getValue(), entry.getKey(), accessions[i]));
            i++;
        }

        return messageHashAccession;
    }

    @Override
    public synchronized void postSave(SaveResponse<Long, MODEL> response) {
        commit(response.getSavedAccessions().keySet().stream().mapToLong(l -> l).toArray());
        release(response.getUnsavedAccessions().keySet().stream().mapToLong(l -> l).toArray());
    }

}
