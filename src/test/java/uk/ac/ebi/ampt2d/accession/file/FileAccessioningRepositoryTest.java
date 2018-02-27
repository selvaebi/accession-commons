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
package uk.ac.ebi.ampt2d.accession.file;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import uk.ac.ebi.ampt2d.accession.common.accessioning.AccessioningRepository;
import uk.ac.ebi.ampt2d.accession.file.persistence.FileEntity;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(properties = {"services=file-accession"})
public class FileAccessioningRepositoryTest {

    @Autowired
    private AccessioningRepository fileAccessioningRepository;

    private FileEntity generateFileEntity(int value){
        return new FileEntity("file"+value,"file"+value);
    }

    private Set<FileEntity> generateEntities(){
        Set<FileEntity> accessionObjects = new HashSet<>();
        accessionObjects.add(generateFileEntity(1));
        accessionObjects.add(generateFileEntity(2));
        return accessionObjects;
    }

    @Test
    public void testFilesAreStoredInTheRepository() throws Exception {
        fileAccessioningRepository.save(generateEntities());
        assertEquals(2, fileAccessioningRepository.count());

        fileAccessioningRepository.save(generateFileEntity(3));
        assertEquals(3, fileAccessioningRepository.count());
    }

    @Test
    public void testFindObjectsInRepository() throws Exception {
        assertEquals(0, fileAccessioningRepository.count());

        final Set<FileEntity> entities = generateEntities();
        fileAccessioningRepository.save(entities);
        assertEquals(2, fileAccessioningRepository.count());

        List<String> hashes = entities.stream().map(obj -> obj.getHashedMessage()).collect(Collectors.toList());

        Collection<FileEntity> objectsInRepo = fileAccessioningRepository.findByHashedMessageIn(hashes);
        assertEquals(2, objectsInRepo.size());
    }

    @Test(expected = org.springframework.orm.jpa.JpaSystemException.class)
    public void testSavingObjectsWithoutAccession() throws Exception {
        fileAccessioningRepository.save(new FileEntity("file1", null));
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    @Commit
    public void testSavingObjectsWithoutHash() throws Exception {
        fileAccessioningRepository.save(new FileEntity(null, "accession"));
        TestTransaction.end();
    }
}
