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
package uk.ac.ebi.ampt2d.accession.serial.block;

import java.util.PriorityQueue;

public class MonotonicRangePriorityQueue extends PriorityQueue<MonotonicRange> {

    private long totalOfValuesInQueue;

    public MonotonicRangePriorityQueue() {
        super(MonotonicRange::compareTo);
        totalOfValuesInQueue = 0;
    }

    @Override
    public void clear() {
        super.clear();
        totalOfValuesInQueue = 0;
    }

    @Override
    public boolean offer(MonotonicRange monotonicRange) {
        boolean status = super.offer(monotonicRange);
        if (status) {
            totalOfValuesInQueue += monotonicRange.getTotalOfValues();
        }
        return status;
    }

    @Override
    public boolean remove(Object o) {
        boolean status = super.remove(o);
        if (status) {
            totalOfValuesInQueue -= ((MonotonicRange) o).getTotalOfValues();
        }
        return status;
    }

    @Override
    public MonotonicRange poll() {
        MonotonicRange monotonicRange = super.poll();
        if (monotonicRange != null) {
            totalOfValuesInQueue -= monotonicRange.getTotalOfValues();
        }
        return monotonicRange;
    }

    public long getTotalOfValuesInQueue() {
        return totalOfValuesInQueue;
    }

}
