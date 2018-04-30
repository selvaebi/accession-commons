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
package uk.ac.ebi.ampt2d.commons.accession.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.ampt2d.commons.accession.core.AccessionWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDoesNotExistException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.HashAlreadyExistsException;

import java.util.Collection;
import java.util.List;

/**
 * Interface to the database service that handles the storage and queries of a object with their hashed version and
 * accession.
 *
 * @param <MODEL>
 * @param <HASH>
 * @param <ACCESSION>
 */
public interface DatabaseService<MODEL, HASH, ACCESSION> {

    List<AccessionWrapper<MODEL, HASH, ACCESSION>> findAllAccessionsByHash(Collection<HASH> hashes);

    @Transactional
    void insert(List<AccessionWrapper<MODEL, HASH, ACCESSION>> objects);

    List<AccessionWrapper<MODEL, HASH, ACCESSION>> findAllAccessionMappingsByAccessions(List<ACCESSION> accessions);

    void enableAccessions(List<AccessionWrapper<MODEL, HASH, ACCESSION>> accessionedObjects);

    AccessionWrapper<MODEL, HASH, ACCESSION> update(AccessionWrapper<MODEL, HASH, ACCESSION> accession)
            throws AccessionDoesNotExistException, HashAlreadyExistsException;

    List<AccessionWrapper<MODEL, HASH, ACCESSION>> findAllAccessionMappingsByAccessionAndVersion(ACCESSION accession,
                                                                                                 int version);
}
