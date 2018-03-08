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
package uk.ac.ebi.ampt2d.accessioning.commons.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.ampt2d.accessioning.commons.accessioning.SaveResponse;
import uk.ac.ebi.ampt2d.accessioning.commons.generators.ModelHashAccession;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Interface to the database service that handles the storage and queries of a object with their hashed version and
 * accession.
 *
 * @param <MODEL>
 * @param <HASH>
 * @param <ACCESSION>
 */
public interface DatabaseService<MODEL, HASH, ACCESSION> {

    Map<ACCESSION, MODEL> findAllAccessionsByHash(Collection<HASH> hashes);

    Map<HASH, ACCESSION> getExistingAccessions(Collection<HASH> hashes);

    @Transactional
    void save(List<ModelHashAccession<MODEL, HASH, ACCESSION>> objects);

    Map<ACCESSION, ? extends MODEL> findAllAccessionByAccessions(List<ACCESSION> accessions);
}
