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
package uk.ac.ebi.ampt2d.commons.accession.block.initialization;

import java.util.Map;

public class BlockInitialization {

    private long blockStartValue;
    private long blockSize;
    private long nextBlockInterval;

    public BlockInitialization(Map<String, Object> propertiesMap) {
        Object blockStartValue = propertiesMap.get(BlockInitializationParams.BLOCK_START_VALUE);
        Object blockSize = propertiesMap.get(BlockInitializationParams.BLOCK_SIZE);
        Object nextBlockInterval = propertiesMap.get(BlockInitializationParams.NEXT_BLOCK_INTERVAL);
        this.blockStartValue = (blockStartValue != null) ? Long.parseLong(blockStartValue.toString()) : 0;
        this.blockSize = Long.parseLong(blockSize.toString());
        this.nextBlockInterval = (nextBlockInterval != null) ? Long.parseLong(nextBlockInterval.toString()) : 0;
    }

    public static void checkIsBlockSizeValid(Map<String, Object> blockInitializations) {
        if (blockInitializations == null || blockInitializations.get(BlockInitializationParams.BLOCK_SIZE) == null
                || Long.parseLong(blockInitializations.get(BlockInitializationParams.BLOCK_SIZE).toString()) <= 0)
            throw new BlockInitializationException("BlockSize not initialized for the category or invalid");
    }

    public long getBlockStartValue() {
        return blockStartValue;
    }

    public long getBlockSize() {
        return blockSize;
    }

    public long getNextBlockInterval() {
        return nextBlockInterval;
    }

    @Override
    public String toString() {
        return "BlockInitialization{" +
                "blockStartValue=" + blockStartValue +
                ", blockSize=" + blockSize +
                ", nextBlockInterval=" + nextBlockInterval +
                '}';
    }
}
