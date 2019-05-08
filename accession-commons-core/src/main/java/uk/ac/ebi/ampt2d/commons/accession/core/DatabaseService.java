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
 * @param <MODEL> Type of the objects identified by the accessions
 * @param <HASH> Type of the hash calculated based on the fields that uniquely identify an accessioned object
 * @param <ACCESSION> Type of the accession that identifies an object of a particular model
 */
public interface DatabaseService<MODEL, HASH, ACCESSION> {

    /**
     * Finds all valid accessioned model data that has a hashed message in the collection @param hashes.
     *
     * @param hashes A collection of hash values of objects
     * @return List of wrapper objects containing the accessioned objects and their associated accessions and hashes
     */
    List<AccessionWrapper<MODEL, HASH, ACCESSION>> findAllByHash(Collection<HASH> hashes);

    /**
     * Finds active (neither merged nor deprecated) accessioned objects identified by an accession
     *
     * @param accession Accession that identifies object
     * @return Active (neither merged nor deprecated) accession with version data
     * @throws AccessionDoesNotExistException when the accession has never existed
     * @throws AccessionMergedException       when the accession exists but has been merged into another accession
     * @throws AccessionDeprecatedException   when the accession exists but has been deprecated
     */
    AccessionVersionsWrapper<MODEL, HASH, ACCESSION> findByAccession(ACCESSION accession) throws
            AccessionDoesNotExistException, AccessionMergedException, AccessionDeprecatedException;

    /**
     * Finds the last version of the object identified by the provided accession.
     *
     * @param accession Accession that identifies object
     * @return Wrapper containing the object and associated accession and hash
     * @throws AccessionDoesNotExistException when the accession has never existed
     * @throws AccessionMergedException       when the accession exists but has been merged into another accession
     * @throws AccessionDeprecatedException   when the accession exists but has been deprecated
     */
    AccessionWrapper<MODEL, HASH, ACCESSION> findLastVersionByAccession(ACCESSION accession)
            throws AccessionDoesNotExistException, AccessionMergedException, AccessionDeprecatedException;

    /**
     * Finds the object identified by the provided accession and version.
     *
     * @param accession Accession that identifies object
     * @param version Version number of the accessioned object
     * @return Wrapper containing the accessioned object
     * @throws AccessionDoesNotExistException when the accession has never existed
     * @throws AccessionMergedException       when the accession exists but has been merged into another accession
     * @throws AccessionDeprecatedException   when the accession exists but has been deprecated
     */
    AccessionWrapper<MODEL, HASH, ACCESSION> findByAccessionVersion(ACCESSION accession, int version)
            throws AccessionDoesNotExistException, AccessionDeprecatedException, AccessionMergedException;

    /**
     * Saves the accessioned wrapper objects in repository.
     *
     * @param objects List of wrapper objects containing the accessioned objects and their associated accessions and hashes
     * @return State of the accession after persisting in DB layer
     */
    SaveResponse<ACCESSION> save(List<AccessionWrapper<MODEL, HASH, ACCESSION>> objects);

    /**
     * Persists a new version of an accession.
     *
     * @param accession Accession that identifies the object
     * @param hash Hash value of the object
     * @param model Details of the object of type MODEL
     * @param reason Reason for creating a new version
     * @return Accession with complete patch information
     * @throws AccessionDoesNotExistException when the accession has never existed
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
     * Updates a specific version of an accessioned object, without creating a new version.
     *
     * @param accession Accession that identifies the object
     * @param hash Hash value of the object
     * @param model Details of the object of type MODEL
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
     * Deprecates an accession.
     *
     * @param accession Accession that identifies the object
     * @param reason the reason for deprecation
     * @throws AccessionDoesNotExistException when the accession has never existed.
     * @throws AccessionDeprecatedException   when the accession exists but has been deprecated
     * @throws AccessionMergedException       when the accession exists but has been merged into another accession
     */
    @Transactional(rollbackFor = {AccessionDoesNotExistException.class, AccessionDeprecatedException.class,
            AccessionMergedException.class})
    void deprecate(ACCESSION accession, String reason) throws AccessionDoesNotExistException, AccessionMergedException,
            AccessionDeprecatedException;

    /**
     * Merges an accession into another one.
     *
     * @param accession Accession which will be merged
     * @param mergeInto Accession the original one will be merged into
     * @param reason The reason for merging one accession into another
     * @throws AccessionDoesNotExistException when the accession has never existed
     * @throws AccessionDeprecatedException   when accession exists but has been deprecated
     * @throws AccessionMergedException       when accession exists but has been merged into another accession
     */
    @Transactional(rollbackFor = {AccessionDoesNotExistException.class, AccessionDeprecatedException.class,
            AccessionMergedException.class})
    void merge(ACCESSION accession, ACCESSION mergeInto, String reason) throws AccessionMergedException,
            AccessionDoesNotExistException, AccessionDeprecatedException;
}