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
import uk.ac.ebi.ampt2d.accession.AccessionedObject;

import java.util.Map;
import java.util.stream.Collectors;

public class StudyMessage implements AccessionedObject<String> {

    private Map<String, String> study;

    private String accession;

    @JsonIgnore
    private String hash;

    StudyMessage() {
    }

    public StudyMessage(Map<String, String> study) {
        this.study = study;
    }

    @Override
    public String getHash() {
        return this.study.values().stream().collect(Collectors.joining(","));
    }

    public Map<String, String> getStudy() {
        return study;
    }

    @Override
    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        AccessionedObject that = (AccessionedObject) o;
        return getHash().equals(that.getHash());
    }

    @Override
    public int hashCode() {
        return getHash().hashCode();
    }
}
