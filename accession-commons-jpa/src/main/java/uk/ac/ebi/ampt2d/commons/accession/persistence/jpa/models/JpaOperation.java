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
package uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.models;

import uk.ac.ebi.ampt2d.commons.accession.core.models.EventType;
import uk.ac.ebi.ampt2d.commons.accession.persistence.models.IAccessionedObject;
import uk.ac.ebi.ampt2d.commons.accession.persistence.models.IOperation;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.entities.InactiveAccessionEntity;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.entities.OperationEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class JpaOperation<MODEL, ACCESSION extends Serializable> implements IOperation<MODEL, ACCESSION> {

    private ACCESSION accessionIdOrigin;

    private ACCESSION accessionIdDestination;

    private EventType eventType;

    private String reason;

    private LocalDateTime createdDate;

    private List<? extends InactiveAccessionEntity<MODEL, ACCESSION>> inactiveEntities;

    public JpaOperation(OperationEntity<ACCESSION> lastOperation,
                        List<? extends InactiveAccessionEntity<MODEL, ACCESSION>> inactiveEntities) {
        this.accessionIdOrigin = lastOperation.getAccessionIdOrigin();
        this.accessionIdDestination = lastOperation.getAccessionIdDestination();
        this.eventType = lastOperation.getEventType();
        this.reason = lastOperation.getReason();
        this.createdDate = lastOperation.getCreatedDate();
        this.inactiveEntities = inactiveEntities;
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
    public EventType getEventType() {
        return eventType;
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
    public List<? extends IAccessionedObject<MODEL, ?, ACCESSION>> getInactiveObjects() {
        return inactiveEntities;
    }

}
