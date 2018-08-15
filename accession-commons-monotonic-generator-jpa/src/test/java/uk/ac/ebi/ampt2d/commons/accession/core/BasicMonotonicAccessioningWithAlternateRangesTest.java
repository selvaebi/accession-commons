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
import uk.ac.ebi.ampt2d.commons.accession.block.initialization.BlockInitializationException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionCouldNotBeGeneratedException;
import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionWrapper;
import uk.ac.ebi.ampt2d.commons.accession.generators.monotonic.MonotonicAccessionGenerator;
import uk.ac.ebi.ampt2d.commons.accession.hashing.SHA1HashingFunction;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.service.ContiguousIdBlockService;
import uk.ac.ebi.ampt2d.test.configuration.TestMonotonicDatabaseServiceTestConfiguration;
import uk.ac.ebi.ampt2d.test.models.TestModel;
import uk.ac.ebi.ampt2d.test.service.TestMonotonicDatabaseService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {TestMonotonicDatabaseServiceTestConfiguration.class})
public class BasicMonotonicAccessioningWithAlternateRangesTest {

    private static final String INSTANCE_ID = "test-instance";

    @Autowired
    private TestMonotonicDatabaseService databaseService;
    @Autowired
    private ContiguousIdBlockService contiguousIdBlockService;

    @Test(expected = BlockInitializationException.class)
    public void testUnknownCategory() throws AccessionCouldNotBeGeneratedException {
        List<AccessionWrapper<TestModel, String, Long>> evaAccessions =
                getAccessioningService("unknown-category", INSTANCE_ID)
                        .getOrCreate(getObjectsForAccessionsInRange(1, 10));
    }

    @Test
    public void testAlternateRangesWithDifferentGenerators() throws AccessionCouldNotBeGeneratedException {
        String categoryId = "eva_2";
        String instanceId2 = "test-instance_2";
        List<AccessionWrapper<TestModel, String, Long>> evaAccessions = getAccessioningService(categoryId, INSTANCE_ID)
                .getOrCreate(getObjectsForAccessionsInRange(1, 10));
        assertEquals(9, evaAccessions.size());
        assertEquals(1, evaAccessions.get(0).getAccession().longValue());
        assertEquals(9, evaAccessions.get(8).getAccession().longValue());
        assertEquals(1, contiguousIdBlockService
                .getUncompletedBlocksByCategoryIdAndApplicationInstanceIdOrderByEndAsc(categoryId, INSTANCE_ID)
                .size());

        //get another service for same category
        evaAccessions = getAccessioningService(categoryId, INSTANCE_ID)
                .getOrCreate(getObjectsForAccessionsInRange(10, 30));
        assertEquals(20, evaAccessions.size());
        //Previous block ended here
        assertEquals(10, evaAccessions.get(0).getAccession().longValue());
        //New Block after 200 values interval
        assertEquals(211, evaAccessions.get(1).getAccession().longValue());
        assertEquals(220, evaAccessions.get(10).getAccession().longValue());
        assertEquals(421, evaAccessions.get(11).getAccession().longValue());
        assertEquals(429, evaAccessions.get(19).getAccession().longValue());
        assertEquals(1, contiguousIdBlockService.getUncompletedBlocksByCategoryIdAndApplicationInstanceIdOrderByEndAsc
                (categoryId, INSTANCE_ID).size());

        //get another service for same category but different Instance
        evaAccessions = getAccessioningService(categoryId, instanceId2)
                .getOrCreate(getObjectsForAccessionsInRange(30, 39));
        assertEquals(9, evaAccessions.size());
        assertNotEquals(430, evaAccessions.get(0).getAccession().longValue());
        assertEquals(631, evaAccessions.get(0).getAccession().longValue());
        assertEquals(639, evaAccessions.get(8).getAccession().longValue());
        assertEquals(1, contiguousIdBlockService.getUncompletedBlocksByCategoryIdAndApplicationInstanceIdOrderByEndAsc
                (categoryId, instanceId2).size());

        //get previous uncompleted service from instance1 and create accessions
        evaAccessions = getAccessioningService(categoryId, INSTANCE_ID)
                .getOrCreate(getObjectsForAccessionsInRange(39, 41));
        assertEquals(2, evaAccessions.size());
        assertEquals(430, evaAccessions.get(0).getAccession().longValue());  //Block ended here
        //New Block with 200 interval from last block made in INSTANCE_2
        assertEquals(841, evaAccessions.get(1).getAccession().longValue());
    }

    private List<TestModel> getObjectsForAccessionsInRange(int startRange, int endRange) {
        return IntStream.range(startRange, endRange).mapToObj(i -> TestModel.of("Test-" + i)).collect(Collectors.toList());
    }

    private AccessioningService<TestModel, String, Long> getAccessioningService(String categoryId,
                                                                                String instanceId) {
        return new BasicAccessioningService<>(
                getGenerator(categoryId, instanceId),
                databaseService,
                TestModel::getValue,
                new SHA1HashingFunction()
        );
    }

    private MonotonicAccessionGenerator<TestModel> getGenerator(String categoryId, String instanceId) {
        return new MonotonicAccessionGenerator(categoryId, instanceId, contiguousIdBlockService, databaseService);
    }
}

