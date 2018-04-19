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
package uk.ac.ebi.ampt2d.commons.accession.persistence.history.entities;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uk.ac.ebi.ampt2d.commons.accession.core.AccessionStatus;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.ZonedDateTime;

/**
 * Base entity that represents an entry in an accession history table.
 * Must be extended to include the attribute that will represent the type of actual accession.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AccessionHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private AccessionStatus accessionStatus;

    @Column(nullable = false, length = 2000)
    private String reason;

    @CreatedDate
    @Column(updatable = false)
    private ZonedDateTime createdDate;

    public AccessionHistoryEntity(AccessionStatus accessionStatus, String reason) {
        this.accessionStatus = accessionStatus;
        this.reason = reason;
    }
}