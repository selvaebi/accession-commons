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
package uk.ac.ebi.ampt2d.accessioning.commons.persistence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import uk.ac.ebi.ampt2d.accessioning.commons.generators.ModelHashAccession;
import uk.ac.ebi.ampt2d.test.TestModel;
import uk.ac.ebi.ampt2d.test.configuration.TestDatabaseServiceTestConfiguration;
import uk.ac.ebi.ampt2d.test.persistence.TestEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestRepository;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {TestDatabaseServiceTestConfiguration.class})
public class BasicSpringDataRepositoryDatabaseServiceTest {

    @Autowired
    private BasicSpringDataRepositoryDatabaseService<TestModel, TestEntity, String, String> service;

    @Test
    public void testFindInEmptyRepository() {
        assertEquals(0, service.findAllAccessionByAccessions(Arrays.asList("a1", "a2")).size());
        assertEquals(0, service.findAllAccessionsByHash(Arrays.asList("h1", "h2")).size());
        assertEquals(0, service.getExistingAccessions(Arrays.asList("h1", "h2")).size());
    }

    @Test
    public void saveUniqueElements() {
        service.save(Arrays.asList(
                ModelHashAccession.of(TestModel.of("something1"), "h1", "a1"),
                ModelHashAccession.of(TestModel.of("something2"), "h2", "a2"),
                ModelHashAccession.of(TestModel.of("something3"), "h3", "a3")
        ));

        Map<String, ? extends TestModel> accessionsToModels = service.findAllAccessionByAccessions(
                Arrays.asList("a1", "a2"));
        assertEquals(2, accessionsToModels.size());
        assertEquals("something1", accessionsToModels.get("a1").getSomething());
        assertEquals("something2", accessionsToModels.get("a2").getSomething());

        Map<String, ? extends TestModel> accessionsToModels2 = service.findAllAccessionsByHash(
                Arrays.asList("h1", "h2"));
        assertEquals(2, accessionsToModels2.size());
        assertEquals("something1", accessionsToModels2.get("a1").getSomething());
        assertEquals("something2", accessionsToModels2.get("a2").getSomething());

        Map<String, String> hashToAccession = service.getExistingAccessions(Arrays.asList("h1", "h3"));
        assertEquals(2, hashToAccession.size());
        assertEquals("a1", hashToAccession.get("h1"));
        assertEquals("a3", hashToAccession.get("h3"));
    }

    @Test(expected = DataIntegrityViolationException.class)
    @Commit
    public void saveNonUniqueElements() {
        service.save(Arrays.asList(
                ModelHashAccession.of(TestModel.of("something1"), "h1", "a1"),
                ModelHashAccession.of(TestModel.of("something2"), "h1", "a2"),
                ModelHashAccession.of(TestModel.of("something3"), "h3", "a3")
        ));
        TestTransaction.end();
    }

}
