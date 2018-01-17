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
import uk.ac.ebi.ampt2d.accession.DatabaseService;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "services", havingValue = "study-accession")
public class StudyAccessioningDatabaseService implements DatabaseService<StudyMessage> {

    @Autowired
    StudyAccessioningRepository studyAccessioningRepository;

    @Override
    public Collection findObjectsInDB(List<StudyMessage> accessionObjects) {
        List<String> checksums = accessionObjects.stream().map(obj -> obj.getHash()).collect(Collectors.toList());
        Collection<StudyEntity> studyEntitiesInDb = studyAccessioningRepository.findByHashIn(checksums);
        Map<StudyEntity, String> studyEntitiesStringMap = studyEntitiesInDb.stream().collect(Collectors.toMap(Function.identity(), StudyEntity::getAccession));

        return accessionObjects.stream().filter(object -> studyEntitiesStringMap.containsKey(object)).
                map(obj -> {
                    obj.setAccession(studyEntitiesStringMap.get(obj));
                    return obj;
                }).collect(Collectors.toSet());

    }

    @Override
    public void save(Set<StudyMessage> accessioningObjects) {
        HashSet<StudyEntity> studySet = new HashSet<>();
        accessioningObjects.forEach(accObj -> {
            StudyEntity studyEntity = new StudyEntity();
            studyEntity.setAccession(accObj.getAccession());
            studyEntity.setHash(accObj.getHash());
            studySet.add(studyEntity);
        });
        studyAccessioningRepository.save(studySet);
    }

    @Override
    public long count() {
        return studyAccessioningRepository.count();
    }

    @Override
    public void save(StudyMessage accObj) {
        StudyEntity studyEntity = new StudyEntity();
        studyEntity.setAccession(accObj.getAccession());
        studyEntity.setHash(accObj.getHash());
        studyAccessioningRepository.save(studyEntity);
    }
}
