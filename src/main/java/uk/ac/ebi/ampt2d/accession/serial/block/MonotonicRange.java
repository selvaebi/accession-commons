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

import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MonotonicRange implements Comparable<MonotonicRange> {

    private final long start;

    private final long end;

    public MonotonicRange(long start, long end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public int compareTo(MonotonicRange monotonicRange) {
        return Long.compare(start, monotonicRange.start);
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

    public List<MonotonicRange> exclude(List<MonotonicRange> ranges) {
        //Sorted ensures that start values are sorted monotonically
        List<MonotonicRange> intersectingRanges = ranges.stream().filter(this::intersects).sorted()
                .collect(Collectors.toList());
        long i = start;
        List<MonotonicRange> result = new ArrayList<>();
        if (intersectingRanges.isEmpty()) {
            result.add(new MonotonicRange(start, end));
            return result;
        }
        for (MonotonicRange range : intersectingRanges) {
            if (i == range.start) {
                i = Math.min(end, range.end + 1);
                continue;
            } else {
                if (i < range.start) {
                    result.add(new MonotonicRange(i, range.start - 1));
                }
                i = range.end + 1;
            }
        }
        if (i < end) {
            result.add(new MonotonicRange(i, end));
        }
        return result;
    }

    public boolean intersects(MonotonicRange monotonicRange) {
        if (start > monotonicRange.getEnd() || end < monotonicRange.getStart()) {
            return false;
        } else {
            return true;
        }
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

    @Override
    public String toString() {
        return "MonotonicRange{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
