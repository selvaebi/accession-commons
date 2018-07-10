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
package uk.ac.ebi.ampt2d.commons.accession.generators;

import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.models.SaveResponse;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionCouldNotBeGeneratedException;

import java.util.List;
import java.util.Map;

/**
 * A generator for unique accessions for given objects
 *
 * @param <MODEL>     Type of the object to accession
 * @param <ACCESSION> Type of the accession
 */
public interface AccessionGenerator<MODEL, ACCESSION> {

    /**
     * Generate unique accessions for a set of objects. Returned accessions must be unique: two different
     * objects cannot get the same accession
     *
     * @param messages Set of objects to accession
     * @return A map of objects to unique accessions
     */
    <HASH> List<AccessionWrapper<MODEL, HASH, ACCESSION>> generateAccessions(Map<HASH, MODEL> messages)
            throws AccessionCouldNotBeGeneratedException;

    /**
     * This method returns to the accession generator the result of the database insert operation in case any operation
     * needs to be performed at the generator level.
     *
     * @param response
     */
    void postSave(SaveResponse<ACCESSION> response);
}
