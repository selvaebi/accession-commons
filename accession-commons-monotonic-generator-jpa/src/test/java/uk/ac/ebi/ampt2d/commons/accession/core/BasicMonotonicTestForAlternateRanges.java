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
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.service.ContiguousIdBlockService;
import uk.ac.ebi.ampt2d.commons.accession.service.BasicMonotonicAccessioningService;
import uk.ac.ebi.ampt2d.test.TestModel;
import uk.ac.ebi.ampt2d.test.configuration.TestMonotonicDatabaseServiceTestConfiguration;
import uk.ac.ebi.ampt2d.test.service.TestMonotonicDatabaseService;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {TestMonotonicDatabaseServiceTestConfiguration.class})
@TestPropertySource("classpath:application-test1.properties")
public class BasicMonotonicTestForAlternateRanges {

    @Autowired
    private TestMonotonicDatabaseService databaseService;

    @Autowired
    private ContiguousIdBlockService contiguousIdBlockService;

    private static final int BLOCK_SIZE = 1000;
    private static final String INSTANCE_ID = "APP1";
    private static final long NEXT_BLOCK_INTERVAL = 5L;

    @Test
    public void testAlternateRanges() throws AccessionCouldNotBeGeneratedException {
        BasicAccessioningService<TestModel, String, Long> accessioningService = getAccessioningService1();
        BasicAccessioningService<TestModel, String, Long> accessioningService1 = getAccessioningService2();

        List<AccessionWrapper<TestModel, String, Long>> evaAccessions = accessioningService.getOrCreate(
                Arrays.asList(
                        TestModel.of("service-test-1"),
                        TestModel.of("service-test-2"),
                        TestModel.of("service-test-3"),
                        TestModel.of("service-test-4"),
                        TestModel.of("service-test-5"),
                        TestModel.of("service-test-6")
                ));
        List<AccessionWrapper<TestModel, String, Long>> dbSNPAccessions = accessioningService1.getOrCreate(
                Arrays.asList(
                        TestModel.of("service-test-7"),
                        TestModel.of("service-test-8"),
                        TestModel.of("service-test-9"),
                        TestModel.of("service-test-10"),
                        TestModel.of("service-test-11"),
                        TestModel.of("service-test-12")
                ));

        assertEquals(6, evaAccessions.size());
        assertEquals(6, dbSNPAccessions.size());
        evaAccessions.stream().allMatch(accession -> accession.getAccession() < 5L || accession.getAccession() == 11L);
        evaAccessions.stream().allMatch(accession -> (accession.getAccession() >= 6L
                && accession.getAccession() <= 10L) || accession.getAccession() == 16L);
    }

    private BasicMonotonicAccessioningService<TestModel, String> getAccessioningService1() {
        return new BasicMonotonicAccessioningService<>(
                new MonotonicAccessionGenerator<>(BLOCK_SIZE, NEXT_BLOCK_INTERVAL, INSTANCE_ID, "EVA",
                        contiguousIdBlockService),
                databaseService,
                TestModel::getValue,
                new SHA1HashingFunction()
        );
    }

    private BasicMonotonicAccessioningService<TestModel, String> getAccessioningService2() {
        return new BasicMonotonicAccessioningService<>(
                new MonotonicAccessionGenerator<>(BLOCK_SIZE, NEXT_BLOCK_INTERVAL, INSTANCE_ID, "dbSNP",
                        contiguousIdBlockService),
                databaseService,
                TestModel::getValue,
                new SHA1HashingFunction()
        );
    }
}

