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
package uk.ac.ebi.ampt2d.accession.study;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TestTransaction;
import uk.ac.ebi.ampt2d.accession.study.persistence.StudyAccessioningRepository;
import uk.ac.ebi.ampt2d.accession.study.persistence.StudyEntity;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.ampt2d.test.utils.TestHelper.generateStudyEntities;
import static uk.ac.ebi.ampt2d.test.utils.TestHelper.generateStudyMap;

@RunWith(SpringJUnit4ClassRunner.class)
@DataJpaTest
@TestPropertySource(properties = {"services=study-accession"})
public class StudyAccessioningRepositoryTest {

    @Autowired
    private StudyAccessioningRepository repository;

    @Test
    public void testStudiesAreStoredToRepository() throws Exception {
        repository.save(generateStudyEntities(1, 2));
        assertEquals(2, repository.count());
        repository.save(generateStudyEntities(3));
        assertEquals(3, repository.count());
    }

    @Test
    public void testFindObjectsInRepository() throws Exception {
        final List<StudyEntity> entities = generateStudyEntities(1, 2);
        repository.save(entities);
        assertEquals(2, repository.count());
        List<String> hashes = entities.stream().map(obj -> obj.getHashedMessage()).collect(Collectors.toList());
        Collection<StudyEntity> objectsInRepo = repository.findByHashedMessageIn(hashes);
        assertEquals(2, objectsInRepo.size());
    }

    //JpaSystemException is due to the id of entity being null
    @Test(expected = org.springframework.orm.jpa.JpaSystemException.class)
    public void testSavingObjectsWithoutAccession() throws Exception {
        StudyEntity entity = new StudyEntity(generateStudyMap(1), null, "hashedMessage1");
        repository.save(entity);
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    @Commit
    public void testSavingObjectsWithoutHashedMessage() throws Exception {
        StudyEntity entity = new StudyEntity(generateStudyMap(1), "accession1", null);
        repository.save(entity);
        TestTransaction.end();
    }

    @Test
    public void testSavingObjectsWithSameAccessionOverwrites() throws Exception {
        repository.save(generateStudyEntities(1, 1));
        assertEquals(1, repository.count());
    }
}
