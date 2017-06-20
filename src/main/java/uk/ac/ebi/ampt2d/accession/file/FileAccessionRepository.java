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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.ampt2d.accession.AccessionRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FileAccessionRepository implements AccessionRepository<File, UUID> {

    @Autowired
    private FileCrudRepository fileJpaRepository;

    @Override
    public Map<File, UUID> get(List<File> objects) {
        List<String> checksums = objects.stream().map(File::getChecksum).collect(Collectors.toList());
        List<File> filesInRepository = fileJpaRepository.findByChecksumIn(checksums);
        return filesInRepository.stream().collect(Collectors.toMap(Function.identity(), File::getAccession));
    }

    @Override
    public void add(Map<File, UUID> accessions) {
        for (File file : accessions.keySet()) {
            file.setAccession(accessions.get(file));
            fileJpaRepository.save(file);
        }
    }

    FileCrudRepository getFileJpaRepository() {
        return fileJpaRepository;
    }
}
