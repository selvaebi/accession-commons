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

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.ac.ebi.ampt2d.accession.AccessioningObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class Study implements AccessioningObject<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;

    @Column(nullable = false)
    private String studyTitle;

    @Column(nullable = false)
    private String studyType;

    @Column(nullable = false)
    private String submitterEmail;

    @JsonIgnore
    @Column(nullable = false, unique = true)
    private String hash;

    @Column(nullable = false, unique = true)
    private UUID accession;

    Study() {
    }

    public Study(String studyTitle, String studyType, String submitterEmail) {
        this.studyTitle = studyTitle;
        this.studyType = studyType;
        this.submitterEmail = submitterEmail;
        this.hash = getHash();
    }

    public String getStudyTitle() {
        return studyTitle;
    }

    public String getStudyType() {
        return studyType;
    }

    public String getSubmitterEmail() {
        return submitterEmail;
    }

    @Override
    public String getHash() {
        return String.valueOf((this.studyTitle + this.studyType + this.submitterEmail).hashCode());
    }

    @Override
    public UUID getAccession() {
        return accession;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setAccession(UUID accession) {
        this.accession = accession;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Study study = (Study) o;

        return getHash().equals(study.getHash());
    }

    @Override
    public int hashCode() {
        return getHash().hashCode();
    }
}
