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
package uk.ac.ebi.ampt2d.accession.sha1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.ac.ebi.ampt2d.accession.AccessioningObject;
import uk.ac.ebi.ampt2d.accession.AccessioningRepository;
import uk.ac.ebi.ampt2d.accession.AccessioningService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service("sha1-accession")
@ConditionalOnProperty(name = "accessionBy", havingValue = "sha1")
public class SHA1AccessioningService extends AccessioningService<AccessioningObject, String> {

    @Autowired
    private AccessioningRepository accessioningRepository;

    public SHA1AccessioningService() {
        super(new SHA1AccessionGenerator());
    }

    @Override
    public Map<AccessioningObject, String> get(List<AccessioningObject> AccessionObjects) {
        List<String> checksums = AccessionObjects.stream().map(obj -> {
            obj.setHash(obj.getHash());
            return obj.getHash();
        }).collect(Collectors.toList());
        Collection<AccessioningObject> objectsInRepo = accessioningRepository.findByHashIn(checksums);
        return objectsInRepo.stream().collect(Collectors.toMap(Function.identity(), a -> a.getAccession().toString()));

    }

    @Override
    public void add(Map<AccessioningObject, String> accessions) {
        for (AccessioningObject obj : accessions.keySet()) {
            obj.setAccession(accessions.get(obj));
        }
        accessioningRepository.save(accessions.keySet());
    }
}
