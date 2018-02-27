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
package uk.ac.ebi.ampt2d.accession.sample;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TestTransaction;
import uk.ac.ebi.ampt2d.accession.sample.persistence.SampleAccessioningRepository;
import uk.ac.ebi.ampt2d.accession.sample.persistence.SampleEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.ampt2d.test.utils.TestHelper.generateSampleEntities;
import static uk.ac.ebi.ampt2d.test.utils.TestHelper.generateSampleEntity;

@RunWith(SpringJUnit4ClassRunner.class)
@DataJpaTest
@TestPropertySource(properties = {"services=sample-accession"})
public class SampleAccessioningRepositoryTest {

    @Autowired
    private SampleAccessioningRepository accessioningRepository;

    @Test
    public void testSamplesAreStoredToRepository() throws Exception {
        accessioningRepository.save(generateSampleEntities(1, 2));
        assertEquals(2, accessioningRepository.count());
        accessioningRepository.save(generateSampleEntity(3));
        assertEquals(3, accessioningRepository.count());
    }

    @Test
    public void testFindObjectsInRepository() throws Exception {
        List<SampleEntity> entities = generateSampleEntities(1, 2);
        accessioningRepository.save(entities);
        assertEquals(2, accessioningRepository.count());
        List<String> hashes = entities.stream().map(obj -> obj.getHashedMessage()).collect(Collectors.toList());
        Collection<SampleEntity> objectsInRepo = accessioningRepository.findByHashedMessageIn(hashes);
        assertEquals(2, objectsInRepo.size());
    }

    //JpaSystemException is due to the id of entity being null
    @Test(expected = JpaSystemException.class)
    public void testSavingObjectsWithoutAccession() throws Exception {
        SampleEntity accessionObject = new SampleEntity(new HashMap<>(), null, "hashedMessage1");
        accessioningRepository.save(accessionObject);
    }

    @Test(expected = DataIntegrityViolationException.class)
    @Commit
    public void testSavingObjectsWithoutHashedMessage() throws Exception {
        SampleEntity accessionObject = new SampleEntity(new HashMap<>(), "accession1", null);
        accessioningRepository.save(accessionObject);
        TestTransaction.end();
    }

    @Test
    public void testSavingObjectsWithSameAccessionOverwrites() throws Exception {
        accessioningRepository.save(generateSampleEntities(1, 1));
        assertEquals(1, accessioningRepository.count());
    }
}
