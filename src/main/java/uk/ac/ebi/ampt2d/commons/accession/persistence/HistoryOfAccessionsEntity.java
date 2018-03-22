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
package uk.ac.ebi.ampt2d.commons.accession.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Date;

@Entity
public class HistoryOfAccessionsEntity {

    @Id
    @Column(nullable = false)
    private String accession;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Date createdOrModifiedDate;

    @Column()
    private String reasonForChange;

    HistoryOfAccessionsEntity() {
    }

    public HistoryOfAccessionsEntity(String accession, String status, Date createdOrModifiedDate, String reasonForChange) {
        this.accession = accession;
        this.status = status;
        this.createdOrModifiedDate = createdOrModifiedDate;
        this.reasonForChange = reasonForChange;
    }

    public String getAccession() {
        return accession;
    }

    public String getStatus() {
        return status;
    }

    public Date getCreatedOrModifiedDate() {
        return createdOrModifiedDate;
    }

    public String getReasonForChange() {
        return reasonForChange;
    }
}