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

/**
 * Initialization parameters for blocks of monotonic accession
 */
public class BlockParameters {

    private static final String BLOCK_SIZE = "blockSize";
    private static final String BLOCK_START_VALUE = "blockStartValue";
    private static final String NEXT_BLOCK_INTERVAL = "nextBlockInterval";

    private long blockStartValue;
    private long blockSize;
    private long nextBlockInterval;

    public BlockParameters(Map<String, String> blockInitializations) {
        try {
            this.blockStartValue = Long.parseLong(blockInitializations.get(BLOCK_START_VALUE));
            this.blockSize = Long.parseLong(blockInitializations.get(BLOCK_SIZE));
            this.nextBlockInterval = Long.parseLong(blockInitializations.get(NEXT_BLOCK_INTERVAL));
            if (!isBlockParametersValid())
                throw new BlockInitializationException("BlockParameters are invalid");
        } catch (RuntimeException ex) {
            throw new BlockInitializationException("BlockParameters not initialized for the category or invalid");
        }
    }

    private boolean isBlockParametersValid() {
        if (this.blockStartValue >= 0 && this.blockSize > 0 && this.nextBlockInterval >= 0)
            return true;
        return false;
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
        return "BlockParameters{" +
                "blockStartValue=" + blockStartValue +
                ", blockSize=" + blockSize +
                ", nextBlockInterval=" + nextBlockInterval +
                '}';
    }
}
