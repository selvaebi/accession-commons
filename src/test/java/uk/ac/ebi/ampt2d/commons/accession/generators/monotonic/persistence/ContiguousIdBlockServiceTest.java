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
package uk.ac.ebi.ampt2d.commons.accession.generators.monotonic.persistence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.commons.accession.generators.monotonic.persistence.entities.ContiguousIdBlock;
import uk.ac.ebi.ampt2d.commons.accession.generators.monotonic.persistence.service.ContiguousIdBlockService;
import uk.ac.ebi.ampt2d.test.configuration.MonotonicAccessionGeneratorTestConfiguration;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {MonotonicAccessionGeneratorTestConfiguration.class})
public class ContiguousIdBlockServiceTest {

    private static final String CATEGORY_ID = "test-cat";
    private static final String INSTANCE_ID = "test-instance";
    private static final long TEST_SIZE = 1000l;
    private static final String INSTANCE_ID_2 = "test-instance2";

    @Autowired
    private ContiguousIdBlockService service;

    @Test
    public void testReserveNewBlocks() {
        ContiguousIdBlock block = service.reserveNewBlock(CATEGORY_ID, INSTANCE_ID, TEST_SIZE);
        assertEquals(0, block.getStart());
        assertEquals(999, block.getEnd());
        assertTrue(block.isNotFull());
        ContiguousIdBlock block2 = service.reserveNewBlock(CATEGORY_ID, INSTANCE_ID, TEST_SIZE);
        assertEquals(1000, block2.getStart());
        assertEquals(1999, block2.getEnd());
        assertTrue(block.isNotFull());
    }

    @Test
    public void testReserveWithExistingData() {
        // Save a block
        service.save(Arrays.asList(new ContiguousIdBlock(CATEGORY_ID, INSTANCE_ID, 0, 5)));
        ContiguousIdBlock block = service.reserveNewBlock(CATEGORY_ID, INSTANCE_ID, TEST_SIZE);
        assertEquals(5, block.getStart());
        assertEquals(1004, block.getEnd());
        assertTrue(block.isNotFull());
    }

    @Test
    public void testReserveNewBlocksWithMultipleInstances() {
        ContiguousIdBlock block = service.reserveNewBlock(CATEGORY_ID, INSTANCE_ID, TEST_SIZE);
        assertEquals(0, block.getStart());
        assertEquals(999, block.getEnd());
        assertTrue(block.isNotFull());
        ContiguousIdBlock block2 = service.reserveNewBlock(CATEGORY_ID, INSTANCE_ID_2, TEST_SIZE);
        assertEquals(1000, block2.getStart());
        assertEquals(1999, block2.getEnd());
        assertTrue(block.isNotFull());
        ContiguousIdBlock block3 = service.reserveNewBlock(CATEGORY_ID, INSTANCE_ID, TEST_SIZE);
        assertEquals(2000, block3.getStart());
        assertEquals(2999, block3.getEnd());
        assertTrue(block.isNotFull());
    }

    @Test
    public void testGetUncompleteBlocks() {
        ContiguousIdBlock uncompletedBlock = new ContiguousIdBlock(CATEGORY_ID, INSTANCE_ID, 0, 5);
        ContiguousIdBlock completedBlock = new ContiguousIdBlock(CATEGORY_ID, INSTANCE_ID, 10, 5);
        completedBlock.setLastCommitted(14);

        service.save(Arrays.asList(uncompletedBlock));
        service.save(Arrays.asList(new ContiguousIdBlock(CATEGORY_ID, INSTANCE_ID_2, 5, 5)));
        service.save(Arrays.asList(completedBlock));
        service.save(Arrays.asList(new ContiguousIdBlock(CATEGORY_ID, INSTANCE_ID, 15, 5)));

        List<ContiguousIdBlock> contiguousBlocks =
                service.getUncompletedBlocksByCategoryIdAndApplicationInstanceIdOrderByEndAsc(CATEGORY_ID, INSTANCE_ID);

        assertEquals(2, contiguousBlocks.size());
        assertEquals(0,contiguousBlocks.get(0).getStart());
        assertEquals(4,contiguousBlocks.get(0).getEnd());
        assertEquals(15,contiguousBlocks.get(1).getStart());
        assertEquals(19,contiguousBlocks.get(1).getEnd());
    }

}
