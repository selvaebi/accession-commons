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
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionCouldNotBeGeneratedException;
import uk.ac.ebi.ampt2d.commons.accession.generators.DecoratedAccessionGenerator;
import uk.ac.ebi.ampt2d.commons.accession.generators.monotonic.MonotonicAccessionGenerator;
import uk.ac.ebi.ampt2d.commons.accession.hashing.SHA1HashingFunction;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.service.ContiguousIdBlockService;
import uk.ac.ebi.ampt2d.commons.accession.service.BasicMonotonicAccessioningService;
import uk.ac.ebi.ampt2d.commons.accession.block.initialization.BlockInitializationException;
import uk.ac.ebi.ampt2d.test.TestModel;
import uk.ac.ebi.ampt2d.test.configuration.TestMonotonicDatabaseServiceTestConfiguration;
import uk.ac.ebi.ampt2d.test.service.TestMonotonicDatabaseService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {TestMonotonicDatabaseServiceTestConfiguration.class})
public class BasicMonotonicAccessioningWithAlternateRangesTest {

    private static final String CATEGORY_ID = "eva";
    private static final String CATEGORY_ID_2 = "eva_2";
    private static final String UNKNOWN_CATEGORY = "Unknown-Category";
    private static final String INVALID_BLOCK_SIZE_CATEGORY = "invalid-block-size-test";
    private static final String INVALID_BLOCK_START_VALUE_CATEGORY ="invalid-block-start-value-test";
    private static final String INVALID_NEXT_BLOCK_INTERVAL_CATEGORY="invalid-next-block-interval-test";
    private static final String INSTANCE_ID = "test-instance";
    private static final String INSTANCE_ID_2 = "test-instance_2";

    @Autowired
    private TestMonotonicDatabaseService databaseService;
    @Autowired
    private ContiguousIdBlockService contiguousIdBlockService;

    @Test(expected = BlockInitializationException.class)
    public void testUnknownCategory() throws AccessionCouldNotBeGeneratedException {
        List<AccessionWrapper<TestModel, String, Long>> evaAccessions =
                getAccessioningService1(UNKNOWN_CATEGORY, INSTANCE_ID)
                        .getOrCreate(getObjectsForAccessionsInRange(1, 10));
    }

    @Test(expected = BlockInitializationException.class)
    public void testInvalidBlockSize() throws AccessionCouldNotBeGeneratedException {
        List<AccessionWrapper<TestModel, String, Long>> evaAccessions =
                getAccessioningService1(INVALID_BLOCK_SIZE_CATEGORY, INSTANCE_ID)
                        .getOrCreate(getObjectsForAccessionsInRange(1, 10));
    }

    @Test(expected = BlockInitializationException.class)
    public void testInvalidBlockStartValue() throws AccessionCouldNotBeGeneratedException {
        List<AccessionWrapper<TestModel, String, Long>> evaAccessions =
                getAccessioningService1(INVALID_BLOCK_START_VALUE_CATEGORY, INSTANCE_ID)
                        .getOrCreate(getObjectsForAccessionsInRange(1, 10));
    }

    @Test(expected = BlockInitializationException.class)
    public void testInvalidNextblockInterval() throws AccessionCouldNotBeGeneratedException {
        List<AccessionWrapper<TestModel, String, Long>> evaAccessions =
                getAccessioningService1(INVALID_NEXT_BLOCK_INTERVAL_CATEGORY, INSTANCE_ID)
                        .getOrCreate(getObjectsForAccessionsInRange(1, 10));
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
        DecoratedAccessionGenerator<TestModel, Long> generator =
                DecoratedAccessionGenerator.buildPrefixSuffixAccessionGenerator
                        (getGenerator(CATEGORY_ID, INSTANCE_ID), "RS", null, Long::parseLong);
        List<AccessionWrapper<TestModel, String, String>> generated = generator.generateAccessions(objects);
        assertEquals(6, generated.size());
        assertEquals("RS1", generated.get(0).getAccession());
        assertEquals("RS2", generated.get(1).getAccession());
        assertEquals("RS3", generated.get(2).getAccession());
        assertEquals("RS4", generated.get(3).getAccession());
        assertEquals("RS5", generated.get(4).getAccession());
        assertEquals("RS11", generated.get(5).getAccession());
    }

    @Test
    public void testAlternateRangesWithDifferentGenerators() throws AccessionCouldNotBeGeneratedException {

        List<AccessionWrapper<TestModel, String, Long>> evaAccessions = getAccessioningService1(CATEGORY_ID_2, INSTANCE_ID)
                .getOrCreate(getObjectsForAccessionsInRange(1, 10));
        assertEquals(9, evaAccessions.size());
        assertEquals(1, evaAccessions.get(0).getAccession().longValue());
        assertEquals(9, evaAccessions.get(8).getAccession().longValue());
        assertEquals(1, contiguousIdBlockService
                .getUncompletedBlocksByCategoryIdAndApplicationInstanceIdOrderByEndAsc(CATEGORY_ID_2, INSTANCE_ID)
                .size());

        //get another service for same category
        evaAccessions = getAccessioningService1(CATEGORY_ID_2, INSTANCE_ID)
                .getOrCreate(getObjectsForAccessionsInRange(10, 30));
        assertEquals(20, evaAccessions.size());
        //Block ended here
        assertEquals(10, evaAccessions.get(0).getAccession().longValue());
        //New Block with 200 values interval
        assertEquals(211, evaAccessions.get(1).getAccession().longValue());
        assertEquals(220, evaAccessions.get(10).getAccession().longValue());
        assertEquals(421, evaAccessions.get(11).getAccession().longValue());
        assertEquals(429, evaAccessions.get(19).getAccession().longValue());
        assertEquals(1, contiguousIdBlockService.getUncompletedBlocksByCategoryIdAndApplicationInstanceIdOrderByEndAsc
                (CATEGORY_ID_2, INSTANCE_ID).size());

        //get another service for same category but different Instance
        evaAccessions = getAccessioningService1(CATEGORY_ID_2, INSTANCE_ID_2)
                .getOrCreate(getObjectsForAccessionsInRange(30, 39));
        assertEquals(9, evaAccessions.size());
        assertNotEquals(430, evaAccessions.get(0).getAccession().longValue());
        assertEquals(631, evaAccessions.get(0).getAccession().longValue());
        assertEquals(639, evaAccessions.get(8).getAccession().longValue());
        assertEquals(1, contiguousIdBlockService.getUncompletedBlocksByCategoryIdAndApplicationInstanceIdOrderByEndAsc
                (CATEGORY_ID_2, INSTANCE_ID_2).size());

        //get previous uncompleted service from instance1 and create accessions
        evaAccessions = getAccessioningService1(CATEGORY_ID_2, INSTANCE_ID)
                .getOrCreate(getObjectsForAccessionsInRange(39, 41));
        assertEquals(2, evaAccessions.size());
        assertEquals(430, evaAccessions.get(0).getAccession().longValue());  //Block ended here
        //New Block with 200 interval from last block made in INSTANCE_2
        assertEquals(841, evaAccessions.get(1).getAccession().longValue());
    }

    private List<TestModel> getObjectsForAccessionsInRange(int startRange, int endRange) {
        return IntStream.range(startRange, endRange).mapToObj(i -> TestModel.of("Test-" + i)).collect(Collectors.toList());
    }

    private BasicMonotonicAccessioningService<TestModel, String> getAccessioningService1(String categoryId,
                                                                                         String instanceId) {
        return new BasicMonotonicAccessioningService<>(
                getGenerator(categoryId, instanceId),
                databaseService,
                TestModel::getValue,
                new SHA1HashingFunction()
        );
    }

    private MonotonicAccessionGenerator<TestModel> getGenerator(String categoryId, String instanceId) {
        return new MonotonicAccessionGenerator(
                categoryId, instanceId, contiguousIdBlockService);
    }
}

