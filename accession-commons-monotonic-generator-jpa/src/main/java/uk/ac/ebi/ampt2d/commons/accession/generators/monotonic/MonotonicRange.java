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

import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents an inclusive range of ascending monotonic integer values
 */
public class MonotonicRange implements Comparable<MonotonicRange> {

    private final long start;

    private final long end;

    public MonotonicRange(long start, long end) {
        if (end < start) {
            throw new IndexOutOfBoundsException("In a monotonically increasing range the end value must be equals or " +
                    "greater than the start.");
        }
        this.start = start;
        this.end = end;
    }

    @Override
    public int compareTo(MonotonicRange monotonicRange) {
        int value = Long.compare(start, monotonicRange.start);
        if (value == 0) {
            return Long.compare(end, monotonicRange.end);
        }
        return value;
    }

    /**
     * Returns a array of monotonically increasing ids from start to end inclusively. The array is generated and
     * returned on demand to make the life span of the array as short as possible in order to have a smaller memory
     * footprint
     *
     * @return
     */
    public long[] getIds() {
        int size = getTotalOfValues();
        long[] ids = new long[size];
        long tempId = start;
        for (int i = 0; i < size; i++) {
            ids[i] = tempId;
            tempId++;
        }
        return ids;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public int getTotalOfValues() {
        return Math.toIntExact((end - start + 1));
    }

    public Pair<MonotonicRange, MonotonicRange> split(int numberOfElements) {
        return Pair.of(new MonotonicRange(start, start + numberOfElements - 1),
                new MonotonicRange(start + numberOfElements, end));
    }

    public static List<MonotonicRange> convertToMonotonicRanges(long... accessions) {
        List<MonotonicRange> ranges = new ArrayList<>();

        if (accessions.length == 0) {
            return ranges;
        }

        Arrays.sort(accessions);
        long minMonotonicValue = accessions[0];
        long maxMonotonicValue = accessions[0];
        for (int i = 1; i < accessions.length; i++) {
            if (maxMonotonicValue + 1 == accessions[i]) {
                maxMonotonicValue = accessions[i];
            } else {
                ranges.add(new MonotonicRange(minMonotonicValue, maxMonotonicValue));
                minMonotonicValue = accessions[i];
                maxMonotonicValue = accessions[i];
            }
        }
        ranges.add(new MonotonicRange(minMonotonicValue, maxMonotonicValue));
        return ranges;
    }

    /**
     * Returns the result of excluding a range of values of the current range.
     *
     * @param range
     * @return Returns empty, one, two (ordered), or the original range
     */
    public List<MonotonicRange> excludeIntersection(MonotonicRange range) {
        List<MonotonicRange> result = new ArrayList<>();
        if (!this.intersects(range)) {
            // Nothing was excluded, return this
            result.add(this);
        } else {
            if (range.start > start) {
                if (range.end >= end) {
                    //Return left
                    result.add(new MonotonicRange(start, range.start - 1));
                } else {
                    //Return left & right
                    result.add(new MonotonicRange(start, range.start - 1));
                    result.add(new MonotonicRange(range.end + 1, end));
                }
            } else {
                if (range.end < end) {
                    //Return right
                    result.add(new MonotonicRange(range.end + 1, end));
                }
                // Else excluded the full range, return empty list
            }

        }
        return result;
    }

    public List<MonotonicRange> excludeIntersections(List<MonotonicRange> ranges) {
        //Sorted ensures that start values are sorted monotonically
        List<MonotonicRange> orderedRanges = ranges.stream().sorted()
                .collect(Collectors.toList());
        List<MonotonicRange> result = new ArrayList<>();
        MonotonicRange iterator = this;
        for (MonotonicRange range : orderedRanges) {
            List<MonotonicRange> temp = iterator.excludeIntersection(range);
            if (temp.isEmpty()) {
                // No more range space to intersect, exit prematurely
                return result;
            } else {
                if (temp.size() > 1) {
                    result.add(temp.get(0));
                    iterator = temp.get(1);
                } else {
                    iterator = temp.get(0);
                }
            }
        }
        result.add(iterator);
        return result;
    }

    public boolean intersects(MonotonicRange monotonicRange) {
        return start <= monotonicRange.getEnd() && end >= monotonicRange.getStart();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MonotonicRange)) return false;

        MonotonicRange that = (MonotonicRange) o;

        if (start != that.start) return false;
        return end == that.end;
    }

    @Override
    public int hashCode() {
        int result = (int) (start ^ (start >>> 32));
        result = 31 * result + (int) (end ^ (end >>> 32));
        return result;
    }

}
