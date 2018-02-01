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
import uk.ac.ebi.ampt2d.accession.serial.block.MonotonicRange;
import uk.ac.ebi.ampt2d.accession.service.exceptions.AccessionIsNotPending;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generic service to get a list of unique monotonically increasing ids for objects.
 *
 * @param <T>
 */
public abstract class GenericMonotonicAccessioningService<T> implements InitializingBean {

    private MonotonicAccessionGenerator accessionGenerator;

    /**
     * Get accessions for a list of objects. It looks for the object's accessions in a repository, and it they don't
     * exist, generate new ones, storing them in the repository
     *
     * @param objects List of objects to accession
     * @return Objects to accessions map
     */
    public Map<T, Long> getAccessions(List<T> objects) throws AccessionIsNotPending {
        // look for accessions for those objects in the repository
        Map<T, Long> storedAccessions = this.get(objects);

        // get all objects that are not in the repository
        List<T> objectsNotInRepository = objects.stream().filter(object -> !storedAccessions.containsKey(object))
                .distinct().collect(Collectors.toList());

        if (!objectsNotInRepository.isEmpty()) {
            // generate accessions for all the new objects, adding them to the repository
            List<MonotonicRange> accessions = accessionGenerator.generateAccessionRanges(objectsNotInRepository.size());
            StoreMonotonicAccessionsResponse<T> response = this.add(accessions, objectsNotInRepository);
            accessionGenerator.commit(response.getUsedIds());
            accessionGenerator.release(response.getUnusedIds());
            storedAccessions.putAll(response.getObjectsToAccessions());
        }

        return storedAccessions;
    }

    protected abstract StoreMonotonicAccessionsResponse<T> add(List<MonotonicRange> accessions, List<T> objects);

    public abstract Map<T, Long> get(List<T> objects);

    public abstract long[] getExistingIds(Collection<MonotonicRange> ids);

    @Override
    public void afterPropertiesSet() throws Exception {
        Collection<MonotonicRange> unconfirmedAccessionRanges = accessionGenerator.getAvailableRanges();
        accessionGenerator.recoverState(getExistingIds(unconfirmedAccessionRanges));
    }

}
