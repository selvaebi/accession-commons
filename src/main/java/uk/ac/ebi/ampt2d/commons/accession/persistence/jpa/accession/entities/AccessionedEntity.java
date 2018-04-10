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
package uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.accession.entities;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uk.ac.ebi.ampt2d.commons.accession.persistence.IAccessionedObject;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AccessionedEntity<ACCESSION extends Serializable> implements IAccessionedObject<ACCESSION> {

    @NotNull
    @Column(nullable = false, unique = true)
    private String hashedMessage;

    private boolean active;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    public AccessionedEntity(String hashedMessage, ACCESSION accession) {
        this.hashedMessage = hashedMessage;
        active = true;
    }

    @Override
    public String getHashedMessage() {
        return hashedMessage;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
}
