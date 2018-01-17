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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@DataJpaTest
@TestPropertySource(properties = {"services=study-accession"})
public class StudyAccessioningRepositoryTest {

    @Autowired
    StudyAccessioningRepository accessioningRepository;

    private StudyEntity accessionObject1;
    private StudyEntity accessionObject2;

    @Before
    public void setUp() throws Exception {

        accessionObject1 = new StudyEntity();
        accessionObject1.setHash("study1");
        accessionObject1.setAccession("study1accession");
        accessionObject2 = new StudyEntity();
        accessionObject2.setHash("study2");
        accessionObject2.setAccession("study2accession");
    }

    @Test
    public void testStudiesAreStoredToRepository() throws Exception {
        List<StudyEntity> accessionObjects = new ArrayList<>();
        accessionObjects.add(accessionObject1);
        accessionObjects.add(accessionObject2);

        accessioningRepository.save(accessionObjects);
        assertEquals(2, accessioningRepository.count());

        StudyEntity accessionObject3 = new StudyEntity();
        accessionObject3.setHash("study3");
        accessionObject3.setAccession("study3accession");

        accessioningRepository.save(accessionObject3);
        assertEquals(3, accessioningRepository.count());

    }

    @Test
    public void testFindObjectsInRepository() throws Exception {
        assertEquals(0, accessioningRepository.count());
        List<StudyEntity> accessionObjects = new ArrayList<>();
        accessionObjects.add(accessionObject1);
        accessionObjects.add(accessionObject2);

        accessioningRepository.save(accessionObjects);
        assertEquals(2, accessioningRepository.count());

        List<String> hashes = accessionObjects.stream().map(obj -> obj.getHash()).collect(Collectors.toList());

        Collection<StudyEntity> objectsInRepo = accessioningRepository.findByHashIn(hashes);
        assertEquals(2, objectsInRepo.size());

        hashes = accessionObjects.stream().map(obj -> obj.getAccession()).collect(Collectors.toList());
        objectsInRepo = accessioningRepository.findByHashIn(hashes);
        assertEquals(0, objectsInRepo.size());
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void testSavingObjectsWithoutAccession() throws Exception {
        StudyEntity accessionObject = new StudyEntity();
        accessionObject.setHash("file");
        accessioningRepository.save(accessionObject);

    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void testSavingObjectsWithoutHash() throws Exception {
        StudyEntity accessionObject = new StudyEntity();
        accessionObject.setAccession("accession");
        accessioningRepository.save(accessionObject);
    }

}
