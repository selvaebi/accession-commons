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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.commons.accession.autoconfigure.MonotonicGeneratorProperties;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {TestMonotonicDatabaseServiceTestConfiguration.class})
@TestPropertySource("classpath:application-test1.properties")
public class BasicMonotonicTestForAlternateRanges {

    @Autowired
    private TestMonotonicDatabaseService databaseService;

    @Autowired
    private ContiguousIdBlockService contiguousIdBlockService;

    @Autowired
    @Qualifier("getMonotonicGeneratorPropertiesForEva")
    private MonotonicGeneratorProperties monotonicGeneratorProperties;

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

    @Test
    public void testAlternateRangesWithDifferentBlockSizes() throws AccessionCouldNotBeGeneratedException {
        monotonicGeneratorProperties.setBlockSize(5);
        monotonicGeneratorProperties.setNextBlockInterval(5);
        List<AccessionWrapper<TestModel, String, Long>> evaAccessions = getAccessioningService1().getOrCreate
                (IntStream.range(1, 21).mapToObj(i -> TestModel.of("Test-" + i)).collect(Collectors.toList()));
        assertEquals(20, evaAccessions.size());
        assertEquals(35, evaAccessions.get(19).getAccession().longValue());
        assertFalse(evaAccessions.stream().anyMatch(accession -> accession.getAccession() % 10 > 5));
        monotonicGeneratorProperties.setBlockSize(10);
        monotonicGeneratorProperties.setNextBlockInterval(10);
        evaAccessions = getAccessioningService1().getOrCreate
                (IntStream.range(21, 41).mapToObj(i -> TestModel.of("Test-" + i)).collect(Collectors.toList()));
        assertEquals(20, evaAccessions.size());
        assertEquals(46, evaAccessions.get(0).getAccession().longValue());
        assertEquals(75, evaAccessions.get(19).getAccession().longValue());
        monotonicGeneratorProperties.setNextBlockInterval(0);
        evaAccessions = getAccessioningService1().getOrCreate(Arrays.asList(TestModel.of("Test-41")));
        assertEquals(76,evaAccessions.get(0).getAccession().longValue());

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
        return new MonotonicAccessionGenerator(monotonicGeneratorProperties.getBlockSize(),
                monotonicGeneratorProperties.getNextBlockInterval(),
                monotonicGeneratorProperties.getCategoryId(), monotonicGeneratorProperties.getApplicationInstanceId(), contiguousIdBlockService);
    }
}

