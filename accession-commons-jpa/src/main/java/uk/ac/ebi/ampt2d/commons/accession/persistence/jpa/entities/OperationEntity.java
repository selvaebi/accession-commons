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
package uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.entities;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uk.ac.ebi.ampt2d.commons.accession.core.OperationType;
import uk.ac.ebi.ampt2d.commons.accession.persistence.IOperation;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * Base class for operations with accessions to be serialized as relational entities. The derived classes must be
 * annotated as Entity.
 * 
 * @param <ACCESSION>
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class OperationEntity<ACCESSION> implements IOperation<ACCESSION> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private ACCESSION accessionIdOrigin;

    @Column
    private ACCESSION accessionIdDestination;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private OperationType operationType;

    @Column(nullable = false, length = 2000)
    private String reason;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    public Long getId() {
        return id;
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
    public OperationType getOperationType() {
        return operationType;
    }

    @Override
    public String getReason() {
        return reason;
    }

    @Override
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void fill(OperationType type, ACCESSION origin, ACCESSION destination, String reason) {
        this.operationType = type;
        this.accessionIdOrigin = origin;
        this.accessionIdDestination = destination;
        this.reason = reason;
    }
}