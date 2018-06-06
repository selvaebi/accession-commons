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
package uk.ac.ebi.ampt2d.commons.accession.persistence.mongodb.document;

import org.springframework.data.mongodb.core.index.Indexed;
import uk.ac.ebi.ampt2d.commons.accession.persistence.IAccessionedObject;

import java.time.LocalDateTime;

/**
 * Mongo subdocument that represents a particular accessioned object that is currently inactive. It is nested under the
 * document representing the operation that made it become inactive.
 *
 * @param <ACCESSION>
 * @see OperationDocument
 */
public abstract class InactiveSubDocument<ACCESSION> implements IAccessionedObject<ACCESSION> {

    private String hashedMessage;

    @Indexed
    private ACCESSION accession;

    private int version;

    private LocalDateTime createdDate;

    protected InactiveSubDocument() {
    }

    public InactiveSubDocument(IAccessionedObject<ACCESSION> object) {
        this.hashedMessage = object.getHashedMessage();
        this.accession = object.getAccession();
        this.version = object.getVersion();
        this.createdDate = object.getCreatedDate();
    }

    @Override
    public ACCESSION getAccession() {
        return accession;
    }

    @Override
    public String getHashedMessage() {
        return hashedMessage;
    }

    @Override
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    @Override
    public int getVersion() {
        return version;
    }

}
