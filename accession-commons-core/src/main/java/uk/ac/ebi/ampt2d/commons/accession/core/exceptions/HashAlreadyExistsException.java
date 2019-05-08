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
package uk.ac.ebi.ampt2d.commons.accession.core.exceptions;

import java.io.Serializable;

/**
 * Exception thrown when the hash for the accessioned object is already present in the repository.
 */
public class HashAlreadyExistsException extends Exception {

    public <HASH, ACCESSION extends Serializable> HashAlreadyExistsException(HASH hash, ACCESSION accession) {
        super("Hash '" + hash + "' is present in accession '" + accession + "' ");
    }
}
