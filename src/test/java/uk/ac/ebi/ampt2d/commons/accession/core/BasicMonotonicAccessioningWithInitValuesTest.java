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
package uk.ac.ebi.ampt2d.commons.accession.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionCouldNotBeGeneratedException;
import uk.ac.ebi.ampt2d.commons.accession.generators.monotonic.MonotonicAccessionGenerator;
import uk.ac.ebi.ampt2d.commons.accession.hashing.SHA1HashingFunction;
import uk.ac.ebi.ampt2d.test.TestModel;
import uk.ac.ebi.ampt2d.test.configuration.TestMonotonicDatabaseServiceTestConfiguration;
import uk.ac.ebi.ampt2d.test.persistence.TestMonotonicEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestMonotonicRepository;
import uk.ac.ebi.ampt2d.test.service.TestMonotonicDatabaseService;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {TestMonotonicDatabaseServiceTestConfiguration.class})
@TestPropertySource("classpath:application-test1.properties")
public class BasicMonotonicAccessioningWithInitValuesTest {

    @Autowired
    private TestMonotonicRepository repository;

    @Autowired
    private TestMonotonicDatabaseService databaseService;

    @Autowired
    private MonotonicAccessionGenerator<TestModel> monotonicAccessionGenerator;

    @Test
    public void testAccessionElements() throws AccessionCouldNotBeGeneratedException {
        BasicAccessioningService<TestModel, String, Long> accessioningService = getAccessioningService();

        List<AccessionModel<TestModel, String, Long>> accessions = accessioningService.getOrCreateAccessions(
                Arrays.asList(
                        TestModel.of("service-test-1"),
                        TestModel.of("service-test-2"),
                        TestModel.of("service-test-3")
                ));

        assertEquals(3, accessions.size());
        accessions.stream().forEach(entry -> assertTrue(entry.getAccession() >= 100L));
    }

    private BasicMonotonicAccessioningService<TestModel, String> getAccessioningService() {
        return new BasicMonotonicAccessioningService<>(
                monotonicAccessionGenerator,
                databaseService,
                TestModel::getSomething,
                new SHA1HashingFunction()
        );
    }

    @Test
    public void testGetOrCreateFiltersRepeated() throws AccessionCouldNotBeGeneratedException {
        BasicAccessioningService<TestModel, String, Long> accessioningService = getAccessioningService();

        List<AccessionModel<TestModel, String, Long>> accessions = accessioningService.getOrCreateAccessions(
                Arrays.asList(
                        TestModel.of("service-test-1"),
                        TestModel.of("service-test-2"),
                        TestModel.of("service-test-2"),
                        TestModel.of("service-test-3")
                ));
        assertEquals(3, accessions.size());
        accessions.stream().forEach(entry -> assertTrue(entry.getAccession() >= 100L));
    }

    @Test
    public void testGetAccessions() throws AccessionCouldNotBeGeneratedException {
        BasicAccessioningService<TestModel, String, Long> accessioningService = getAccessioningService();

        List<AccessionModel<TestModel, String, Long>> accessions = accessioningService.getAccessions(Arrays.asList(
                TestModel.of("service-test-1"),
                TestModel.of("service-test-2"),
                TestModel.of("service-test-3")
        ));
        assertEquals(0, accessions.size());
    }

    @Test
    public void testGetWithExistingEntries() throws AccessionCouldNotBeGeneratedException {
        repository.save(new TestMonotonicEntity(
                0L,
                "85C4F271CBD3E11A9F8595854F755ADDFE2C0732",
                true,
                "service-test-3"));

        BasicAccessioningService<TestModel, String, Long> accessioningService = getAccessioningService();

        List<AccessionModel<TestModel, String, Long>> accessions = accessioningService.getAccessions(Arrays.asList(
                TestModel.of("service-test-1"),
                TestModel.of("service-test-2"),
                TestModel.of("service-test-3")
        ));
        assertEquals(1, accessions.size());
    }

    @Test
    public void testGetOrCreateWithExistingEntries() throws AccessionCouldNotBeGeneratedException {
        repository.save(new TestMonotonicEntity(
                0L,
                "85C4F271CBD3E11A9F8595854F755ADDFE2C0732",
                true,
                "service-test-3"));

        BasicAccessioningService<TestModel, String, Long> accessioningService = getAccessioningService();

        List<AccessionModel<TestModel, String, Long>> accessions = accessioningService.getOrCreateAccessions(
                Arrays.asList(
                        TestModel.of("service-test-1"),
                        TestModel.of("service-test-2"),
                        TestModel.of("service-test-3")
                ));
        assertEquals(3, accessions.size());
        accessions.stream().forEach(entry ->
                assertTrue(entry.getAccession() == 0L || entry.getAccession() >= 100L));
    }

}
