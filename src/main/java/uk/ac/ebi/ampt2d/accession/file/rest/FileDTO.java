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
package uk.ac.ebi.ampt2d.accession.file.rest;

import uk.ac.ebi.ampt2d.accession.file.FileModel;

public class FileDTO implements FileModel {

    private String hash;

    FileDTO() {
    }

    public FileDTO(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileDTO that = (FileDTO) o;

        return getHash() != null ? getHash().equals(that.getHash()) : that.getHash() == null;
    }

    @Override
    public int hashCode() {
        return getHash() != null ? getHash().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "{" +
                "hash='" + hash + '\'' +
                '}';
    }
}
