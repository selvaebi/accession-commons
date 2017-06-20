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
package uk.ac.ebi.ampt2d.accession.server;

import uk.ac.ebi.ampt2d.accession.file.File;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class FileAccessionResponse {

    private List<FileAccession> accessions;

    public FileAccessionResponse(Map<File, UUID> accessionMap) {
        this.accessions = accessionMap.entrySet().stream()
                                      .map(entry -> new FileAccession(entry.getKey(), entry.getValue()))
                                      .collect(Collectors.toList());
    }

    public List<FileAccession> getAccessions() {
        return accessions;
    }

    public void setAccessions(List<FileAccession> accessions) {
        this.accessions = accessions;
    }

    class FileAccession {

        private File file;

        private UUID accession;

        FileAccession(File file, UUID accession) {
            this.file = file;
            this.accession = accession;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public UUID getAccession() {
            return accession;
        }

        public void setAccession(UUID accession) {
            this.accession = accession;
        }
    }
}
