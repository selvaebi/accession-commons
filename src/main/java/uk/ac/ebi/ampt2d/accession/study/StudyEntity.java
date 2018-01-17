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

import uk.ac.ebi.ampt2d.accession.AccessionedObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class StudyEntity implements AccessionedObject<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String accession;

    @Column(nullable = false, unique = true)
    private String hash;

    @Override
    public String getHash() {
        return this.hash;
    }

    @Override
    public String getAccession() {
        return this.accession;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public void setAccession(String accession) {
        this.accession = accession;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        StudyEntity that = (StudyEntity) o;
        return getHash().equals(that.getHash());
    }

    @Override
    public int hashCode() {
        return getHash().hashCode();
    }
}
