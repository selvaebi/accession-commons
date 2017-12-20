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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.ac.ebi.ampt2d.accession.AccessioningService;
import uk.ac.ebi.ampt2d.accession.SHA1AccessionGenerator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "services", havingValue = "object-accession")
public class ObjectAccessioningService extends AccessioningService<AccessionObject, String> {

    @Autowired
    private ObjectAccessioningRepository accessioningRepository;

    public ObjectAccessioningService() {
        super(new SHA1AccessionGenerator());
    }

    @Override
    public Map<AccessionObject, String> get(List<AccessionObject> AccessionObjects) {
        List<String> checksums = AccessionObjects.stream().map(obj -> {
            obj.setHash(obj.getHash());
            return obj.getHash();
        }).collect(Collectors.toList());
        Collection<AccessionObject> studiesInRepo = accessioningRepository.findByHashIn(checksums);
        return studiesInRepo.stream().collect(Collectors.toMap(Function.identity(), AccessionObject::getAccession));

    }

    @Override
    public void add(Map<AccessionObject, String> accessions) {
        for (AccessionObject obj : accessions.keySet()) {
            obj.setAccession(accessions.get(obj));
        }
        accessioningRepository.save(accessions.keySet());
    }
}
