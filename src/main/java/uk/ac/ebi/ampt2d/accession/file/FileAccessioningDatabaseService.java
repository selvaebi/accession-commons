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
import uk.ac.ebi.ampt2d.accession.DatabaseService;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "services", havingValue = "file-accession")
public class FileAccessioningDatabaseService implements DatabaseService<FileMessage> {

    @Autowired
    FileAccessioningRepository fileAccessioningRepository;

    @Override
    public Collection<FileMessage> findObjectsInDB(List<FileMessage> accessionObjects) {
        List<String> checksums = accessionObjects.stream().map(obj ->
                obj.getHash()).collect(Collectors.toList());
        Collection<FileEntity> fileEntities = fileAccessioningRepository.findByHashIn(checksums);
        Map<FileEntity, String> fileEntityStringMap = fileEntities.stream().collect(Collectors.toMap(Function.identity(), FileEntity::getAccession));

        return accessionObjects.stream().filter(object -> fileEntityStringMap.containsKey(object)).
                map(obj -> {
                    obj.setAccession(fileEntityStringMap.get(obj));
                    return obj;
                }).collect(Collectors.toSet());

    }

    @Override
    public void save(Set<FileMessage> accessioningObjects) {
        HashSet<FileEntity> fileSet = new HashSet<>();
        accessioningObjects.forEach(accObj -> {
            FileEntity fileEntity = new FileEntity();
            fileEntity.setAccession(accObj.getAccession());
            fileEntity.setHash(accObj.getHash());
            fileSet.add(fileEntity);
        });
        fileAccessioningRepository.save(fileSet);
    }

    @Override
    public void save(FileMessage accObj) {
        FileEntity fileEntity = new FileEntity();
        fileEntity.setAccession(accObj.getAccession());
        fileEntity.setHash(accObj.getHash());
        fileAccessioningRepository.save(fileEntity);
    }

    @Override
    public long count() {
        return fileAccessioningRepository.count();
    }
}
