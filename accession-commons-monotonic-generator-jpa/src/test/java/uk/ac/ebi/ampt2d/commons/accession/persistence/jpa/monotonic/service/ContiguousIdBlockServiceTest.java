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
package uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.entities.ContiguousIdBlock;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.repositories.ContiguousIdBlockRepository;
import uk.ac.ebi.ampt2d.test.configuration.MonotonicAccessionGeneratorTestConfiguration;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {MonotonicAccessionGeneratorTestConfiguration.class})
public class ContiguousIdBlockServiceTest {

    private static final String CATEGORY_ID = "cat-test";
    private static final String CATEGORY_ID_2 = "contiguous-block-test";
    private static final String INSTANCE_ID = "test-instance";
    private static final String INSTANCE_ID_2 = "test-instance2";

    @Autowired
    private ContiguousIdBlockRepository repository;

    @Autowired
    private ContiguousIdBlockService service;

    @Test
    public void testReserveNewBlocks() {
        ContiguousIdBlock block = service.reserveNewBlock(CATEGORY_ID, INSTANCE_ID);
        assertEquals(0, block.getFirstValue());
        assertEquals(999, block.getLastValue());
        assertTrue(block.isNotFull());
        ContiguousIdBlock block2 = service.reserveNewBlock(CATEGORY_ID, INSTANCE_ID);
        assertEquals(1000, block2.getFirstValue());
        assertEquals(1999, block2.getLastValue());
        assertTrue(block.isNotFull());
    }

    @Test
    public void testReserveWithExistingData() {
        // Save a block
        service.save(Arrays.asList(new ContiguousIdBlock(CATEGORY_ID, INSTANCE_ID, 0, 5)));
        ContiguousIdBlock block = service.reserveNewBlock(CATEGORY_ID, INSTANCE_ID);
        assertEquals(5, block.getFirstValue());
        assertEquals(1004, block.getLastValue());
        assertTrue(block.isNotFull());
    }

    @Test
    public void testReserveNewBlocksWithMultipleInstances() {
        ContiguousIdBlock block = service.reserveNewBlock(CATEGORY_ID, INSTANCE_ID);
        assertEquals(0, block.getFirstValue());
        assertEquals(999, block.getLastValue());
        assertTrue(block.isNotFull());
        ContiguousIdBlock block2 = service.reserveNewBlock(CATEGORY_ID, INSTANCE_ID_2);
        assertEquals(1000, block2.getFirstValue());
        assertEquals(1999, block2.getLastValue());
        assertTrue(block.isNotFull());
        ContiguousIdBlock block3 = service.reserveNewBlock(CATEGORY_ID, INSTANCE_ID);
        assertEquals(2000, block3.getFirstValue());
        assertEquals(2999, block3.getLastValue());
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
        assertEquals(0, contiguousBlocks.get(0).getFirstValue());
        assertEquals(4, contiguousBlocks.get(0).getLastValue());
        assertEquals(15, contiguousBlocks.get(1).getFirstValue());
        assertEquals(19, contiguousBlocks.get(1).getLastValue());
    }

    @Test
    public void testBlockSizeAndIntervalForCategory() {
        ContiguousIdBlock block1 = service.reserveNewBlock(CATEGORY_ID_2, INSTANCE_ID);
        assertEquals(0, block1.getFirstValue());
        assertEquals(999, block1.getLastValue());
        ContiguousIdBlock block2 = service.reserveNewBlock(CATEGORY_ID_2, INSTANCE_ID);
        assertEquals(3000, block2.getFirstValue());
        assertEquals(3999, block2.getLastValue());

        List<ContiguousIdBlock> contiguousBlocks = service
                .getUncompletedBlocksByCategoryIdAndApplicationInstanceIdOrderByEndAsc(CATEGORY_ID_2, INSTANCE_ID);
        assertEquals(2, contiguousBlocks.size());
        assertTrue(contiguousBlocks.get(0).isNotFull());
        assertTrue(contiguousBlocks.get(1).isNotFull());
    }

    @Test
    public void testBlockSizeAndIntervalWithMultipleInstances() {
        ContiguousIdBlock block1 = service.reserveNewBlock(CATEGORY_ID_2, INSTANCE_ID);
        assertEquals(0, block1.getFirstValue());
        assertEquals(999, block1.getLastValue());
        ContiguousIdBlock block2 = service.reserveNewBlock(CATEGORY_ID_2, INSTANCE_ID_2);
        assertEquals(3000, block2.getFirstValue());
        assertEquals(3999, block2.getLastValue());
        assertEquals(block2, repository.findFirstByCategoryIdOrderByLastValueDesc(CATEGORY_ID_2));
    }
}
