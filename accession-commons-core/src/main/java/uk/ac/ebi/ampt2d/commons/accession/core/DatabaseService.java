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
package uk.ac.ebi.ampt2d.commons.accession.core;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDeprecatedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDoesNotExistException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionMergedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.HashAlreadyExistsException;
import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionVersionsWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.models.SaveResponse;

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

    /**
     * Finds all valid accessioned model data that has a hashed message in the collection @param hashes.
     *
     * @param hashes
     * @return
     */
    List<AccessionWrapper<MODEL, HASH, ACCESSION>> findAllByHash(Collection<HASH> hashes);

    /**
     * @param accession
     * @return Active accession with versioning data.
     * @throws AccessionDoesNotExistException when accession does not exist.
     * @throws AccessionMergedException       when accession has been merged with another one, its accession id is included
     *                                        in the exception.
     * @throws AccessionDeprecatedException   accession is no longer active.
     */
    AccessionVersionsWrapper<MODEL, HASH, ACCESSION> findByAccession(ACCESSION accession) throws
            AccessionDoesNotExistException, AccessionMergedException, AccessionDeprecatedException;

    /**
     * Finds last version of provided accession with its possible data model representations.
     *
     * @param accession
     * @return valid accession. No deprecated ids will be returned.
     * @throws AccessionDoesNotExistException
     * @throws AccessionMergedException
     * @throws AccessionDeprecatedException
     */
    AccessionWrapper<MODEL, HASH, ACCESSION> findLastVersionByAccession(ACCESSION accession)
            throws AccessionDoesNotExistException, AccessionMergedException,AccessionDeprecatedException;

    /**
     * Finds a specific version of accession and their data model representations.
     *
     * @param accession
     * @param version
     * @return
     * @throws AccessionDoesNotExistException when accession does not exist.
     * @throws AccessionMergedException       when accession has been merged with another one, its accession id is included
     *                                        in the exception.
     * @throws AccessionDeprecatedException   accession is no longer active.
     */
    AccessionWrapper<MODEL, HASH, ACCESSION> findByAccessionVersion(ACCESSION accession, int version)
            throws AccessionDoesNotExistException, AccessionDeprecatedException, AccessionMergedException;

    SaveResponse<ACCESSION> save(List<AccessionWrapper<MODEL, HASH, ACCESSION>> objects);

    @Transactional(rollbackFor = {AccessionDoesNotExistException.class, HashAlreadyExistsException.class,
            AccessionDeprecatedException.class, AccessionMergedException.class})
    AccessionVersionsWrapper<MODEL, HASH, ACCESSION> patch(ACCESSION accession, HASH hash, MODEL model, String reason)
            throws AccessionDoesNotExistException, HashAlreadyExistsException, AccessionDeprecatedException,
            AccessionMergedException;

    @Transactional(rollbackFor = {AccessionDoesNotExistException.class, HashAlreadyExistsException.class,
            AccessionDeprecatedException.class, AccessionMergedException.class})
    AccessionVersionsWrapper<MODEL, HASH, ACCESSION> update(ACCESSION accession, HASH hash, MODEL model, int version)
            throws AccessionDoesNotExistException, HashAlreadyExistsException, AccessionMergedException,
            AccessionDeprecatedException;

    @Transactional(rollbackFor = {AccessionDoesNotExistException.class, AccessionDeprecatedException.class,
            AccessionMergedException.class})
    void deprecate(ACCESSION accession, String reason) throws AccessionDoesNotExistException, AccessionMergedException,
            AccessionDeprecatedException;

    @Transactional(rollbackFor = {AccessionDoesNotExistException.class, AccessionDeprecatedException.class,
            AccessionMergedException.class})
    void merge(ACCESSION accession, ACCESSION mergeInto, String reason) throws AccessionMergedException,
            AccessionDoesNotExistException, AccessionDeprecatedException;
}