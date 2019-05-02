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

import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionCouldNotBeGeneratedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDeprecatedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDoesNotExistException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionMergedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.HashAlreadyExistsException;
import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionVersionsWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionWrapper;

import java.util.List;

/**
 * Service for creation, retrieval and modifications of object accessions.
 *
 * @param <MODEL> Type of the objects identified by the accessions
 * @param <HASH> Type of the hash calculated based on the fields that uniquely identify an accessioned object
 * @param <ACCESSION> Type of the accession that identifies an object of a particular model
 */
public interface AccessioningService<MODEL, HASH, ACCESSION> {

    /**
     * Find or create the accessions associated with a list of objects.
     * Searches object's accession in the repository, and if it does not exist, new accession is generated and stored in repository
     *
     * @param messages List of objects to be accessioned or already accessioned
     * @return List of wrapper objects containing the accessioned objects and their associated accessions and hashes
     * @throws AccessionCouldNotBeGeneratedException when accession could not be generated
     */
    List<AccessionWrapper<MODEL, HASH, ACCESSION>> getOrCreate(List<? extends MODEL> messages)
            throws AccessionCouldNotBeGeneratedException;

    /**
     * Find the accessions associated with a list of objects
     *
     * @param accessionedObjects List of already accessioned objects
     * @return List of wrapper objects containing the accessioned objects and their associated accessions and hashes
     */
    List<AccessionWrapper<MODEL, HASH, ACCESSION>> get(List<? extends MODEL> accessionedObjects);

    /**
     * Find last version of provided accession with its possible data model representations.
     *
     * @param accession Accession that identifies object
     * @return Wrapper containing the object and associated accession and hash
     * @throws AccessionDoesNotExistException when the accession has never existed.
     * @throws AccessionMergedException       when the accession exists but has been merged into another accession
     * @throws AccessionDeprecatedException   when the accession exists but has been deprecated
     */
    AccessionWrapper<MODEL, HASH, ACCESSION> getByAccession(ACCESSION accession)
            throws AccessionDoesNotExistException, AccessionMergedException, AccessionDeprecatedException;

    /**
     * Find the provided accession with its possible data model representations.
     *
     * @param accession Accession that identifies object
     * @param version Version number of the accessioned object
     * @return Wrapper containing the object and associated accession and hash
     * @throws AccessionDoesNotExistException when the accession has never existed.
     * @throws AccessionDeprecatedException   when the accession exists but has been deprecated
     * @throws AccessionMergedException       when the accession exists but has been merged into another accession
     */
    AccessionWrapper<MODEL, HASH, ACCESSION> getByAccessionAndVersion(ACCESSION accession, int version)
            throws AccessionDoesNotExistException, AccessionMergedException, AccessionDeprecatedException;

    /**
     * Updates a specific patch version of an accessioned object. It does not create a new version / patch
     *
     * @param accession Accession that identifies object
     * @param version Version number of the accessioned object
     * @param message Details of the object of type MODEL
     * @return updated accession with all the patch information
     * @throws AccessionDoesNotExistException when the accession has never existed.
     * @throws HashAlreadyExistsException     when another accessioned object exists already with the same hash
     * @throws AccessionDeprecatedException   when the accession exists but has been deprecated
     * @throws AccessionMergedException       when the accession exists but has been merged into another accession
     */
    AccessionVersionsWrapper<MODEL, HASH, ACCESSION> update(ACCESSION accession, int version, MODEL message)
            throws AccessionDoesNotExistException, HashAlreadyExistsException, AccessionDeprecatedException,
            AccessionMergedException;

    /**
     * Create a new patch version of an accession.
     *
     * @param accession Accession that identifies object
     * @param message Details of the object of type MODEL
     * @return Accession with complete patch information
     * @throws AccessionDoesNotExistException when the accession has never existed.
     * @throws HashAlreadyExistsException     when another accessioned object exists already with the same hash
     * @throws AccessionDeprecatedException   when the accession exists but has been deprecated
     * @throws AccessionMergedException       when the accession exists but has been merged into another accession
     */
    AccessionVersionsWrapper<MODEL, HASH, ACCESSION> patch(ACCESSION accession, MODEL message)
            throws AccessionDoesNotExistException, HashAlreadyExistsException, AccessionDeprecatedException,
            AccessionMergedException;

    /**
     * Deprecate an accession
     *
     * @param accession Accession that identifies object
     * @param reason the reason for deprecation
     * @throws AccessionDoesNotExistException when the accession has never existed.
     * @throws AccessionDeprecatedException   when the accession exists but has been deprecated
     * @throws AccessionMergedException       when the accession exists but has been merged into another accession
     */
    void deprecate(ACCESSION accession, String reason) throws AccessionMergedException, AccessionDoesNotExistException,
            AccessionDeprecatedException;

    /**
     * Merge an accession into another
     *
     * @param accessionOrigin accession which will be merged into destination accession
     * @param mergeInto destination accesion to which original accession will be merged
     * @param reason the reason for merge
     * @throws AccessionDoesNotExistException when the accession has never existed
     * @throws AccessionDeprecatedException   when accession exists but has been deprecated
     * @throws AccessionMergedException       when accession exists but has been merged into another accession
     */
    void merge(ACCESSION accessionOrigin, ACCESSION mergeInto, String reason) throws AccessionMergedException,
            AccessionDoesNotExistException, AccessionDeprecatedException;

}
