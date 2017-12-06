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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.ac.ebi.ampt2d.accession.AccessioningProperties;
import uk.ac.ebi.ampt2d.accession.AccessioningService;
import uk.ac.ebi.ampt2d.accession.UuidAccessionGenerator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "services", havingValue = "study-uuid")
public class StudyAccessioningService extends AccessioningService<Study, UUID> {

    @Autowired
    private StudyAccessioningRepository studyAccessionRepository;

    public StudyAccessioningService(AccessioningProperties properties) {
        super(new UuidAccessionGenerator<>(properties.getNamespace()));
    }

    @Override
    public Map<Study, UUID> get(List<Study> objects) {
        List<String> checksums = objects.stream().map(Study::getHash).collect(Collectors.toList());
        objects.stream().forEach(study -> study.setHash(study.getHash()));
        Collection<Study> studiesInRepository = studyAccessionRepository.findByHashIn(checksums);
        return studiesInRepository.stream().collect(Collectors.toMap(Function.identity(), Study::getAccession));
    }

    @Override
    public void add(Map<Study, UUID> accessions) {
        for (Study study : accessions.keySet()) {
            study.setAccession(accessions.get(study));
        }
        studyAccessionRepository.save(accessions.keySet());
    }
}
