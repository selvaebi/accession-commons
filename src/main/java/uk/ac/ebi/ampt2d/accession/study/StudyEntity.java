/*
 *
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ampt2d.accession.study;

import uk.ac.ebi.ampt2d.accession.AccessionableEntity;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Size;
import java.util.Map;

@Entity
public class StudyEntity implements AccessionableEntity {

    @Id
    @Column(nullable = false, unique = true, updatable = false)
    @Size(max = 230, min = 0)
    private String accession;

    @ElementCollection
    private Map<String, String> study;

    @Column(nullable = false, unique = true)
    private String hashedMessage;

    StudyEntity() {
    }

    public StudyEntity(Map<String, String> study, String accession, String hashedMessage) {
        this.study = study;
        this.accession = accession;
        this.hashedMessage = hashedMessage;
    }

    public String getAccession() {
        return this.accession;
    }

    public void setHashedMessage(String hashedMessage) {
        this.hashedMessage = hashedMessage;
    }

    public String getHashedMessage() {
        return hashedMessage;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public Map<String, String> getStudy() {
        return study;
    }

    public void setStudy(Map<String, String> study) {
        this.study = study;
    }

}
