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

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.index.Indexed;
import uk.ac.ebi.ampt2d.commons.accession.core.OperationType;
import uk.ac.ebi.ampt2d.commons.accession.persistence.IOperation;

import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for operations with accessions to be serialized as MongoDB documents. The derived classes must be
 * annotated as Document.
 *
 * @param <ACCESSION>
 * @param <INACTIVE_DOCUMENT>
 */
public abstract class OperationDocument<
        ACCESSION extends Serializable,
        INACTIVE_DOCUMENT extends InactiveSubDocument<ACCESSION>>
        implements IOperation<ACCESSION>, Persistable<String> {

    @Id
    private String id;

    private OperationType operationType;

    @Indexed
    private ACCESSION accessionIdOrigin;

    private ACCESSION accessionIdDestination;

    private String reason;

    private List<INACTIVE_DOCUMENT> inactiveObjects;

    @CreatedDate
    private LocalDateTime createdDate;

    protected OperationDocument() {
    }

    public void fill(OperationType operationType, ACCESSION accessionIdOrigin, ACCESSION accessionIdDestiny,
                     String reason, List<INACTIVE_DOCUMENT> inactiveObjects) {
        this.operationType = operationType;
        this.accessionIdOrigin = accessionIdOrigin;
        this.accessionIdDestination = accessionIdDestiny;
        this.reason = reason;
        this.inactiveObjects = new ArrayList<>();
        this.inactiveObjects.addAll(inactiveObjects);
    }

    @Override
    public OperationType getOperationType() {
        return operationType;
    }

    @Override
    public ACCESSION getAccessionIdOrigin() {
        return accessionIdOrigin;
    }

    @Override
    public ACCESSION getAccessionIdDestination() {
        return accessionIdDestination;
    }

    @Override
    public String getReason() {
        return reason;
    }

    @Override
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return true;
    }

    public List<INACTIVE_DOCUMENT> getInactiveObjects() {
        return inactiveObjects;
    }
}
