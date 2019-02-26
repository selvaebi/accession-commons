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
 * Interface to the database service that handles the storage and queries of an object with their hashed version and
 * accession.
 *
 * @param <MODEL> Specifies the type of the Accession model
 * @param <HASH> Hash key used for accessioning service
 * @param <ACCESSION> Accession ID of object
 */
public interface DatabaseService<MODEL, HASH, ACCESSION> {

    /**
     * Finds all valid accessioned model data that has a hashed message in the collection @param hashes.
     *
     * @param hashes Hash value of objects
     * @return List of wrappers containing accessions
     */
    List<AccessionWrapper<MODEL, HASH, ACCESSION>> findAllByHash(Collection<HASH> hashes);

    /**
     * @param accession Accession of the object
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
     * @param accession Accession of the object
     * @return valid accession. No deprecated or merged ids will be returned.
     * @throws AccessionDoesNotExistException
     * @throws AccessionMergedException
     * @throws AccessionDeprecatedException
     */
    AccessionWrapper<MODEL, HASH, ACCESSION> findLastVersionByAccession(ACCESSION accession)
            throws AccessionDoesNotExistException, AccessionMergedException, AccessionDeprecatedException;

    /**
     * Finds a specific version of accession and their data model representations.
     *
     * @param accession Accession of the object
     * @param version Version number of the accessioned object
     * @return Wrapper containing the accessioned object
     * @throws AccessionDoesNotExistException when accession does not exist.
     * @throws AccessionMergedException       when accession has been merged with another one, its accession id is included
     *                                        in the exception.
     * @throws AccessionDeprecatedException   accession is no longer active.
     */
    AccessionWrapper<MODEL, HASH, ACCESSION> findByAccessionVersion(ACCESSION accession, int version)
            throws AccessionDoesNotExistException, AccessionDeprecatedException, AccessionMergedException;

    /**
     *
     * @param objects List of wrappers containing the accessioned object
     * @return State of the accession after persisting in DB layer
     */
    SaveResponse<ACCESSION> save(List<AccessionWrapper<MODEL, HASH, ACCESSION>> objects);

    /**
     * Persists a new patch version of an accession
     *
     * @param accession Accession ID of the object
     * @param hash Hash value of the object
     * @param model Type of the accession model
     * @param reason Cause of patch
     * @return Accession with complete patch information
     * @throws AccessionDoesNotExistException when the accession has never existed.
     * @throws HashAlreadyExistsException     when another accessioned object exists already with the same hash
     * @throws AccessionDeprecatedException   when the accession exists but has been deprecated
     * @throws AccessionMergedException       when the accession exists but has been merged into another accession
     */
    @Transactional(rollbackFor = {AccessionDoesNotExistException.class, HashAlreadyExistsException.class,
            AccessionDeprecatedException.class, AccessionMergedException.class})
    AccessionVersionsWrapper<MODEL, HASH, ACCESSION> patch(ACCESSION accession, HASH hash, MODEL model, String reason)
            throws AccessionDoesNotExistException, HashAlreadyExistsException, AccessionDeprecatedException,
            AccessionMergedException;

    /**
     * Updates a specific patch version of an accessioned object
     *
     * @param accession Accession ID of the object
     * @param hash Hash value of the object
     * @param model Type of the accession model
     * @param version Version number of the accessioned object
     * @return updated accession with all the patch information
     * @throws AccessionDoesNotExistException when the accession has never existed.
     * @throws HashAlreadyExistsException     when another accessioned object exists already with the same hash
     * @throws AccessionDeprecatedException   when the accession exists but has been deprecated
     * @throws AccessionMergedException       when the accession exists but has been merged into another accession
     */
    @Transactional(rollbackFor = {AccessionDoesNotExistException.class, HashAlreadyExistsException.class,
            AccessionDeprecatedException.class, AccessionMergedException.class})
    AccessionVersionsWrapper<MODEL, HASH, ACCESSION> update(ACCESSION accession, HASH hash, MODEL model, int version)
            throws AccessionDoesNotExistException, HashAlreadyExistsException, AccessionMergedException,
            AccessionDeprecatedException;

    /**
     * Deprecates an accession
     *
     * @param accession Accession ID of the object
     * @param reason comment or the necessity of deprecation
     * @throws AccessionDoesNotExistException when the accession has never existed.
     * @throws AccessionDeprecatedException   when the accession exists but has been deprecated
     * @throws AccessionMergedException       when the accession exists but has been merged into another accession
     */
    @Transactional(rollbackFor = {AccessionDoesNotExistException.class, AccessionDeprecatedException.class,
            AccessionMergedException.class})
    void deprecate(ACCESSION accession, String reason) throws AccessionDoesNotExistException, AccessionMergedException,
            AccessionDeprecatedException;

    /**
     * Merges an accession into another
     *
     * @param accession accession which will be merged to destination accession
     * @param mergeInto destination accesion to which other accession will be merged
     * @param reason comment or the necessity of merge
     * @throws AccessionDoesNotExistException when either accession has never existed.
     * @throws AccessionDeprecatedException   when either accession exists but has been deprecated
     * @throws AccessionMergedException       when either accession exists but has been merged into another accession
     */
    @Transactional(rollbackFor = {AccessionDoesNotExistException.class, AccessionDeprecatedException.class,
            AccessionMergedException.class})
    void merge(ACCESSION accession, ACCESSION mergeInto, String reason) throws AccessionMergedException,
            AccessionDoesNotExistException, AccessionDeprecatedException;
}