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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.accession.AccessioningRepository;

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

    private FileEntity accessionObject1;
    private FileEntity accessionObject2;
    private Set<FileEntity> accessionObjects;

    @Before
    public void setUp() throws Exception {
        accessionObject1 = new FileEntity("file1", "file1");
        accessionObject2 = new FileEntity("file2", "file2");
        accessionObjects = new HashSet<>();
        accessionObjects.add(accessionObject1);
        accessionObjects.add(accessionObject2);
    }

    @Test
    public void testFilesAreStoredInTheRepository() throws Exception {
        fileAccessioningRepository.save(accessionObjects);
        assertEquals(2, fileAccessioningRepository.count());

        FileEntity accessionObject3 = new FileEntity("file3", "file3");
        accessionObjects.add(accessionObject3);
        fileAccessioningRepository.save(accessionObjects);
        assertEquals(3, fileAccessioningRepository.count());
    }

    @Test
    public void testFindObjectsInRepository() throws Exception {
        assertEquals(0, fileAccessioningRepository.count());

        fileAccessioningRepository.save(accessionObjects);
        assertEquals(2, fileAccessioningRepository.count());

        List<String> hashes = accessionObjects.stream().map(obj -> obj.getHashedMessage()).collect(Collectors.toList());

        Collection<FileEntity> objectsInRepo = fileAccessioningRepository.findByHashedMessageIn(hashes);
        assertEquals(2, objectsInRepo.size());
    }

    @Test(expected = org.springframework.orm.jpa.JpaSystemException.class)
    public void testSavingObjectsWithoutAccession() throws Exception {
        accessionObjects.clear();
        accessionObjects.add(new FileEntity("file1", null));
        fileAccessioningRepository.save(accessionObjects);
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void testSavingObjectsWithoutHash() throws Exception {
        accessionObjects.clear();
        accessionObjects.add(new FileEntity(null, "accession"));
        fileAccessioningRepository.save(accessionObjects);
        fileAccessioningRepository.flush();
    }
}
