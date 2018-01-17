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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(properties = {"services=file-accession"})
public class FileAccessioningRepositoryTest {

    @Autowired
    FileAccessioningRepository fileAccessioningRepository;

    private FileEntity accessionObject1;
    private FileEntity accessionObject2;

    @Before
    public void setUp() throws Exception {

        accessionObject1 = new FileEntity();
        accessionObject1.setHash("file1");
        accessionObject1.setAccession("file1accession");
        accessionObject2 = new FileEntity();
        accessionObject2.setHash("file2");
        accessionObject2.setAccession("file2accession");
    }

    @Test
    public void testFilesAreStoredInTheRepository() throws Exception {
        List<FileEntity> accessionObjects = new ArrayList<>();
        accessionObjects.add(accessionObject1);
        accessionObjects.add(accessionObject2);

        fileAccessioningRepository.save(accessionObjects);
        assertEquals(2, fileAccessioningRepository.count());

        FileEntity accessionObject3 = new FileEntity();
        accessionObject3.setHash("file3");
        accessionObject3.setAccession("file3accession");

        fileAccessioningRepository.save(accessionObject3);
        assertEquals(3, fileAccessioningRepository.count());
    }

    @Test
    public void testFindObjectsInRepository() throws Exception {
        assertEquals(0, fileAccessioningRepository.count());

        List<FileEntity> accessionObjects = new ArrayList<>();
        accessionObjects.add(accessionObject1);
        accessionObjects.add(accessionObject2);

        fileAccessioningRepository.save(accessionObjects);
        assertEquals(2, fileAccessioningRepository.count());

        List<String> hashes = accessionObjects.stream().map(obj -> obj.getHash()).collect(Collectors.toList());

        Collection<FileEntity> objectsInRepo = fileAccessioningRepository.findByHashIn(hashes);
        assertEquals(2, objectsInRepo.size());

        hashes = accessionObjects.stream().map(obj -> obj.getAccession()).collect(Collectors.toList());
        objectsInRepo = fileAccessioningRepository.findByHashIn(hashes);
        assertEquals(0, objectsInRepo.size());
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void testSavingObjectsWithoutAccession() throws Exception {
        FileEntity accessionObject = new FileEntity();
        accessionObject.setHash("file");
        fileAccessioningRepository.save(accessionObject);

    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void testSavingObjectsWithoutHash() throws Exception {
        FileEntity accessionObject = new FileEntity();
        accessionObject.setAccession("fileAccession");
        fileAccessioningRepository.save(accessionObject);
    }


}
