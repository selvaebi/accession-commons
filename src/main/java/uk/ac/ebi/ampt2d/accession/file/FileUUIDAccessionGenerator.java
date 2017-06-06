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
package uk.ac.ebi.ampt2d.accession.file;

import uk.ac.ebi.ampt2d.accession.AccessionGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Accession generator that generates UUID accessions for give
 */
public class FileUUIDAccessionGenerator implements AccessionGenerator<File> {

    @Override
    public Map<File, String> get(Set<File> files) {
        Map<File, String> accessions = files.stream().collect(
                Collectors.toMap(Function.identity(), file -> generateAccesion(file.getChecksum())));

        return accessions;
    }

    private String generateAccesion(String checksum) {
        // TODO: we should add a domain name to the checksum
        UUID accession = UUID.nameUUIDFromBytes(checksum.getBytes());
        return accession.toString();
    }
}
