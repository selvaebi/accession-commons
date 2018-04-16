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
package uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.accession;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import uk.ac.ebi.ampt2d.commons.accession.core.AccessionModel;
import uk.ac.ebi.ampt2d.commons.accession.persistence.BasicSpringDataRepositoryDatabaseService;
import uk.ac.ebi.ampt2d.test.TestModel;
import uk.ac.ebi.ampt2d.test.configuration.TestJpaDatabaseServiceTestConfiguration;
import uk.ac.ebi.ampt2d.test.persistence.TestEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {TestJpaDatabaseServiceTestConfiguration.class})
public class JpaBasicSpringDataRepositoryDatabaseServiceTest {

    public static final AccessionModel<TestModel, String, String> TEST_MODEL_1 =
            AccessionModel.of("a1", "h1", TestModel.of("something1"));
    public static final AccessionModel<TestModel, String, String> TEST_MODEL_2 =
            AccessionModel.of("a2", "h2", TestModel.of("something2"));
    public static final AccessionModel<TestModel, String, String> TEST_MODEL_3 =
            AccessionModel.of("a3", "h3", TestModel.of("something3"));

    @Autowired
    private BasicSpringDataRepositoryDatabaseService<TestModel, TestEntity, String> service;

    @Test
    public void testFindInEmptyRepository() {
        assertEquals(0, service.findAllAccessionMappingsByAccessions(Arrays.asList("a1", "a2")).size());
        assertEquals(0, service.findAllAccessionsByHash(Arrays.asList("h1", "h2")).size());
        assertEquals(0, service.getExistingAccessions(Arrays.asList("h1", "h2")).size());
    }

    @Autowired
    private TestRepository repository;

    @Test
    public void saveUniqueElements() {
        service.save(Arrays.asList(TEST_MODEL_1, TEST_MODEL_2, TEST_MODEL_3));

        List<AccessionModel<TestModel, String, String>> result = service.findAllAccessionMappingsByAccessions(
                Arrays.asList("a1", "a2"));
        assertEquals(2, result.size());
        assertTrue(result.contains(TEST_MODEL_1));
        assertTrue(result.contains(TEST_MODEL_2));

        List<AccessionModel<TestModel, String, String>> results2 = service.findAllAccessionsByHash(
                Arrays.asList("h1", "h2"));
        assertEquals(2, results2.size());
        assertTrue(result.contains(TEST_MODEL_1));
        assertTrue(result.contains(TEST_MODEL_2));

        Map<String, String> hashToAccession = service.getExistingAccessions(Arrays.asList("h1", "h3"));
        assertEquals(2, hashToAccession.size());
        assertTrue(hashToAccession.containsKey(TEST_MODEL_1.getHash()));
        assertTrue(hashToAccession.containsKey(TEST_MODEL_3.getHash()));
    }

    @Test(expected = DataIntegrityViolationException.class)
    @Commit
    public void saveNonUniqueElements() {
        service.save(Arrays.asList(
                TEST_MODEL_1,
                AccessionModel.of("a2", "h1", TestModel.of("something2")),
                TEST_MODEL_3
        ));
        TestTransaction.end();
    }

}
