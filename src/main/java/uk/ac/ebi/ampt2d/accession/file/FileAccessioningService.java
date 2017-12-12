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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.ac.ebi.ampt2d.accession.AccessioningProperties;
import uk.ac.ebi.ampt2d.accession.AccessioningService;
import uk.ac.ebi.ampt2d.accession.SHA1AccessionGenerator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "services", havingValue = "file-accession")
public class FileAccessioningService extends AccessioningService<File, String> {

    @Autowired
    private FileAccessioningRepository fileRepository;

    public FileAccessioningService(AccessioningProperties properties) {
        super(new SHA1AccessionGenerator<>(properties.getNamespace()));
    }

    @Override
    public Map<File, String> get(List<File> objects) {
        List<String> checksums = objects.stream().map(File::getHash).collect(Collectors.toList());
        Collection<File> filesInRepository = fileRepository.findByHashIn(checksums);
        return filesInRepository.stream().collect(Collectors.toMap(Function.identity(), File::getAccession));
    }

    @Override
    public void add(Map<File, String> accessions) {
        for (File file : accessions.keySet()) {
            file.setAccession(accessions.get(file));
        }
        fileRepository.save(accessions.keySet());
    }
}
