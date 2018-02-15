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

import java.util.Map;

/**
 * Response that the DB layer needs to return after storing elements with the information about the accession ids used,
 * not used and the relationship between the id and the object.
 *
 * @param <T>
 */
class StoreMonotonicAccessionsResponse<T> {

    private final long[] usedIds;

    private final long[] unusedIds;

    private final Map<T, Long> objectsToAccessions;

    public StoreMonotonicAccessionsResponse(long[] usedIds, long[] unusedIds, Map<T, Long> objectsToAccessions) {
        this.usedIds = usedIds;
        this.unusedIds = unusedIds;
        this.objectsToAccessions = objectsToAccessions;
    }

    public long[] getUsedIds() {
        return usedIds;
    }

    public long[] getUnusedIds() {
        return unusedIds;
    }

    public Map<T, Long> getObjectsToAccessions() {
        return objectsToAccessions;
    }
}
