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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.ampt2d.accession.AccessioningRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@DataJpaTest
@TestPropertySource(properties = {"services=study-accession"})
public class StudyAccessioningRepositoryTest {

    @Autowired
    private AccessioningRepository accessioningRepository;

    private Map<String, String> studyMap1;
    private Map<String, String> studyMap2;
    private StudyEntity studyEntity1;
    private StudyEntity studyEntity2;
    private Set<StudyEntity> accessionObjects;

    @Before
    public void setUp() throws Exception {

        studyMap1 = new HashMap<>();
        studyMap1.put("title", "Title1");
        studyMap1.put("type", "Type1");
        studyMap1.put("submitterEmail", "Email1");
        studyMap2 = new HashMap<>();
        studyMap2.put("title", "Title2");
        studyMap2.put("type", "Type2");
        studyMap2.put("submitterEmail", "Email2");
        studyEntity1 = new StudyEntity(studyMap1,"accession1","hashedmessage1");
        studyEntity2 = new StudyEntity(studyMap2,"accession2","hashedmessage2");
        accessionObjects = new HashSet<>();
        accessionObjects.add(studyEntity1);
        accessionObjects.add(studyEntity2);
    }

    @Test
    public void testStudiesAreStoredToRepository() throws Exception {
        accessioningRepository.save(accessionObjects);
        assertEquals(2, accessioningRepository.count());

        Map<String, String> studyMap3 = new HashMap<>();
        studyMap3.put("title", "Title3");
        studyMap3.put("type", "Type3");
        studyMap3.put("submitterEmail", "Email3");
        StudyEntity accessionObject3 = new StudyEntity(studyMap3,"accession3","hashedmessage3");

        accessionObjects.clear();
        accessionObjects.add(accessionObject3);

        accessioningRepository.save(accessionObjects);
        assertEquals(3, accessioningRepository.count());

    }

   @Test
    public void testFindObjectsInRepository() throws Exception {
        accessioningRepository.save(accessionObjects);
        assertEquals(2, accessioningRepository.count());

        List<String> hashes = accessionObjects.stream().map(obj -> obj.getHashedMessage()).collect(Collectors.toList());

        Collection<StudyEntity> objectsInRepo = accessioningRepository.findByHashedMessageIn(hashes);
        assertEquals(2, objectsInRepo.size());
    }

    //JpaSystemException is due to the id of entity being null
    @Test(expected = org.springframework.orm.jpa.JpaSystemException.class)
    public void testSavingObjectsWithoutAccession() throws Exception {
        StudyEntity accessionObject = new StudyEntity(studyMap1,null,"hashedMessage1");
        accessionObjects.clear();
        accessionObjects.add(accessionObject);
        accessioningRepository.save(accessionObjects);
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void testSavingObjectsWithoutHashedMessage() throws Exception {
        studyEntity1.setHashedMessage(null);
        accessionObjects.clear();
        accessionObjects.add(studyEntity1);
        accessioningRepository.save(accessionObjects);
        accessioningRepository.flush();
    }

    @Test
    public void testSavingObjectsWithSameAccessionOverwrites() throws Exception {
        accessionObjects.clear();
        StudyEntity accessionObject1 = new StudyEntity(studyMap1,"accession1","hashedMessage1");
        accessionObjects.add(accessionObject1);
        StudyEntity accessionObject2 = new StudyEntity(studyMap2,"accession1","hashedMessage2");
        accessionObjects.add(accessionObject2);
        accessioningRepository.save(accessionObjects);
        accessioningRepository.flush();
    }

}
