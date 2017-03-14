/*
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
 */
package uk.ac.ebi.ampt2d.accession;


import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AccessioningService<T> {
    private AccessionRepository<T> accessionRepository;

    private AccessionGenerator<T> accessionGenerator;

    public AccessioningService(AccessionRepository<T> accessionRepository,
                               AccessionGenerator<T> accessionGenerator) {
        this.accessionRepository = accessionRepository;
        this.accessionGenerator = accessionGenerator;
    }

    public Map<T, String> createAccessions(List<T> objects) {
        // look for accessions for those objects in the repository
        Map<T, String> storedAccessions = accessionRepository.get(objects);

        // create accessions for all objects that are not in the repository
        Map<T, String> newAccessions = objects.stream().
                filter(object -> !storedAccessions.containsKey(object)).distinct().
                collect(Collectors.toMap(Function.identity(), object -> accessionGenerator.get(object)));

        // store the new accessions in the repository and return them all
        accessionRepository.add(newAccessions);

        storedAccessions.putAll(newAccessions);

        return storedAccessions;
    }
}
