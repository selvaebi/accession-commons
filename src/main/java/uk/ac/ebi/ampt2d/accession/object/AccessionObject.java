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
package uk.ac.ebi.ampt2d.accession.object;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.ampt2d.accession.AccessioningObject;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Entity
public class AccessionObject implements AccessioningObject<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;

    @NotNull
    @ElementCollection
    private Map<String, String> accessionObject;

    @Column(nullable = false, unique = true)
    private String accession;

    @JsonIgnore
    @Column(nullable = false, unique = true)
    private String hash;

    AccessionObject() {
    }

    AccessionObject(Map<String, String> accessionObject) {
        this.accessionObject = accessionObject;
        this.hash = getHash();
    }

    public Map<String, String> getAccessionObject() {
        return accessionObject;
    }

    public Long getId() {
        return id;
    }

    public String getHash() {
        return StringUtils.join(this.accessionObject.values().toArray());
    }

    void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String getAccession() {
        return accession;
    }

    void setAccession(String accession) {
        this.accession = accession;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccessionObject AccessionObject = (AccessionObject) o;

        return getHash().equals(AccessionObject.getHash());
    }

    @Override
    public int hashCode() {
        return getHash().hashCode();
    }
}
