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
import uk.ac.ebi.ampt2d.commons.accession.generators.DecoratedAccessionGenerator;
import uk.ac.ebi.ampt2d.commons.accession.generators.monotonic.MonotonicAccessionGenerator;
import uk.ac.ebi.ampt2d.commons.accession.hashing.SHA1HashingFunction;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.service.ContiguousIdBlockService;
import uk.ac.ebi.ampt2d.commons.accession.service.BasicMonotonicAccessioningService;
import uk.ac.ebi.ampt2d.test.TestModel;
import uk.ac.ebi.ampt2d.test.configuration.TestMonotonicDatabaseServiceTestConfiguration;
import uk.ac.ebi.ampt2d.test.service.TestMonotonicDatabaseService;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private static final int BLOCK_SIZE = 5;
    private static final String INSTANCE_ID = "APP1";
    private static final long NEXT_BLOCK_INTERVAL = 5L;
    private static final String CATEGORY_ID = "eva";

    @Test
    public void testAlternateRanges() throws AccessionCouldNotBeGeneratedException {
        BasicAccessioningService<TestModel, String, Long> accessioningService = getAccessioningService1();

        List<AccessionWrapper<TestModel, String, Long>> evaAccessions = accessioningService.getOrCreate(
                Arrays.asList(
                        TestModel.of("service-test-1"),
                        TestModel.of("service-test-2"),
                        TestModel.of("service-test-3"),
                        TestModel.of("service-test-4"),
                        TestModel.of("service-test-5"),
                        TestModel.of("service-test-6")
                ));

        assertEquals(6, evaAccessions.size());
        evaAccessions.stream().allMatch(accession -> accession.getAccession() < 5L || accession.getAccession() == 11L);
    }

    @Test
    public void testAlternateRangesWithPrefixes() throws AccessionCouldNotBeGeneratedException {
        Map<String, TestModel> objects = new LinkedHashMap<>();
        objects.put("hash1", TestModel.of("service-test-1"));
        objects.put("hash2", TestModel.of("service-test-2"));
        objects.put("hash3", TestModel.of("service-test-3"));
        objects.put("hash4", TestModel.of("service-test-4"));
        objects.put("hash5", TestModel.of("service-test-5"));
        objects.put("hash6", TestModel.of("service-test-6"));
        DecoratedAccessionGenerator<TestModel, Long> generator = DecoratedAccessionGenerator
                .buildPrefixSuffixAccessionGenerator(getGenerator(), "RS", null, Long::parseLong);
        List<AccessionWrapper<TestModel, String, String>> generated = generator.generateAccessions(objects);
        assertEquals(6, generated.size());
        assertEquals("RS1", generated.get(0).getAccession());
        assertEquals("RS2", generated.get(1).getAccession());
        assertEquals("RS11", generated.get(5).getAccession());

    }

    private BasicMonotonicAccessioningService<TestModel, String> getAccessioningService1() {
        return new BasicMonotonicAccessioningService<>(
                getGenerator(),
                databaseService,
                TestModel::getValue,
                new SHA1HashingFunction()
        );
    }

    private MonotonicAccessionGenerator<TestModel> getGenerator() {
        return new MonotonicAccessionGenerator(BLOCK_SIZE, NEXT_BLOCK_INTERVAL,
                CATEGORY_ID,INSTANCE_ID,contiguousIdBlockService);
    }
}

