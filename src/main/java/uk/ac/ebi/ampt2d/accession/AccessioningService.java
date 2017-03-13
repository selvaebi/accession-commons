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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccessioningService<T> {
    private AccessionRepository<T> accessionRepository;

    private AccessionGenerator<T> accessionGenerator;

    public AccessioningService(AccessionRepository<T> accessionRepository,
                               AccessionGenerator<T> accessionGenerator) {
        this.accessionRepository = accessionRepository;
        this.accessionGenerator = accessionGenerator;
    }

    public Map<T, String> createAccessions(List<T> objects) {
        Map<T, String> accessions = new HashMap<>();

        for (T object : objects) {
            // look for an existing accession for this object
            String accession = accessionRepository.get(object);

            if (accession == null) {
                // if there is no previous accession for the object, create a new one and store it in the repository
                accession = accessionGenerator.get(object);
                accessionRepository.add(object, accession);
            }

            accessions.put(object, accession);
        }

        return accessions;
    }
}
