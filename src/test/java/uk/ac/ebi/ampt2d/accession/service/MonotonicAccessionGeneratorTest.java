/*
 *
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ampt2d.accession.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.accession.serial.block.MonotonicRange;
import uk.ac.ebi.ampt2d.accession.serial.block.persistence.entities.ContinuousIdBlock;
import uk.ac.ebi.ampt2d.accession.serial.block.persistence.repositories.ContinuousIdBlockRepository;
import uk.ac.ebi.ampt2d.accession.service.exceptions.AccessionIsNotPending;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class MonotonicAccessionGeneratorTest {

    private static final int BLOCK_SIZE = 1000;
    private static final int TENTH_BLOCK_SIZE = (int) (BLOCK_SIZE / 10);
    private static final String CATEGORY_ID = "CAT_TEST";
    private static final String INSTANCE_ID = "inst-01";
    private static final String INSTANCE_2_ID = "inst-02";

    @Autowired
    private ContinuousIdBlockRepository repository;

    @Test
    public void assertNoBlockGeneratedAtLoadIfNoneExists() throws Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();
        assertEquals(0, repository.count());
    }

    @Test
    public void assertBlockGeneratedAtGenerateOperationIfNoBlockExists() throws Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();
        generator.generateAccessions(TENTH_BLOCK_SIZE);
        assertEquals(1, repository.count());
        ContinuousIdBlock block = repository.findFirstByCategoryIdOrderByEndDesc(CATEGORY_ID);
        assertEquals(0, block.getStart());
        assertEquals(BLOCK_SIZE - 1, block.getEnd());
        assertEquals(-1, block.getLastCommitted());
    }

    private MonotonicAccessionGenerator getMonotonicAccessionGenerator() throws Exception {
        assertEquals(0, repository.count());

        MonotonicAccessionGenerator generator = new MonotonicAccessionGenerator(BLOCK_SIZE, CATEGORY_ID,
                INSTANCE_ID, repository);
        generator.afterPropertiesSet();
        return generator;
    }

    @Test
    public void assertBlockNotGeneratedIfPreviousExists() throws Exception {
        assertEquals(0, repository.count());
        repository.save(new ContinuousIdBlock(CATEGORY_ID, INSTANCE_ID, 0, BLOCK_SIZE));
        assertEquals(1, repository.count());
        MonotonicAccessionGenerator generator = new MonotonicAccessionGenerator(BLOCK_SIZE, CATEGORY_ID,
                INSTANCE_ID, repository);
        generator.afterPropertiesSet();

        assertEquals(1, repository.count());
    }

    @Test
    public void assertNewBlockGeneratedInSecondInstance() throws Exception {
        ContinuousIdBlock block;
        assertEquals(0, repository.count());

        MonotonicAccessionGenerator generator1 = new MonotonicAccessionGenerator(BLOCK_SIZE, CATEGORY_ID,
                INSTANCE_ID, repository);
        MonotonicAccessionGenerator generator2 = new MonotonicAccessionGenerator(BLOCK_SIZE, CATEGORY_ID,
                INSTANCE_2_ID, repository);

        generator1.afterPropertiesSet();
        generator1.generateAccessions(TENTH_BLOCK_SIZE);
        assertEquals(1, repository.count());
        block = repository.findFirstByCategoryIdAndInstanceIdOrderByEndDesc(CATEGORY_ID, INSTANCE_ID);
        assertEquals(0, block.getStart());
        assertEquals(BLOCK_SIZE - 1, block.getEnd());
        assertEquals(-1, block.getLastCommitted());

        generator2.afterPropertiesSet();
        generator2.generateAccessions(TENTH_BLOCK_SIZE);
        assertEquals(2, repository.count());
        block = repository.findFirstByCategoryIdAndInstanceIdOrderByEndDesc(CATEGORY_ID, INSTANCE_2_ID);
        assertEquals(BLOCK_SIZE, block.getStart());
        assertEquals(2 * BLOCK_SIZE - 1, block.getEnd());
        assertEquals(BLOCK_SIZE - 1, block.getLastCommitted());
    }

    @Test
    public void assertGenerateAccessions() throws Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();

        long[] accessions = generator.generateAccessions(TENTH_BLOCK_SIZE);
        assertEquals(TENTH_BLOCK_SIZE, accessions.length);
    }

    @Test
    public void assertGenerateMoreAccessionsThanBlockSizeGeneratesTwoBlocks() throws Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();

        //Generate BLOCK_SIZE accessions in BLOCK_SIZE/10 increments
        for (int i = 0; i < 10; i++) {
            long[] accessions = generator.generateAccessions(TENTH_BLOCK_SIZE);
            assertEquals(i * TENTH_BLOCK_SIZE, accessions[0]);
        }
        long[] accessions = generator.generateAccessions(TENTH_BLOCK_SIZE);
        assertEquals(BLOCK_SIZE, accessions[0]);
        assertEquals(2, repository.count());
    }

    @Test
    public void assertGenerateMoreAccessionsThanBlockSizeGeneratesInOneCall() throws Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();

        long[] accessions = generator.generateAccessions(BLOCK_SIZE + (BLOCK_SIZE / 2));
        assertEquals(2, repository.count());
    }

    @Test
    public void assertCommitModifiesLastCommitted() throws Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();
        long[] accessions = generator.generateAccessions(TENTH_BLOCK_SIZE);

        generator.commit(accessions);

        ContinuousIdBlock block = repository.findFirstByCategoryIdAndInstanceIdOrderByEndDesc(CATEGORY_ID, INSTANCE_ID);
        assertEquals(TENTH_BLOCK_SIZE - 1, block.getLastCommitted());
    }

    @Test
    public void assertNotCommitingDoesNotModifyLastCommitted() throws Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();
        long[] accessions = generator.generateAccessions(TENTH_BLOCK_SIZE);

        ContinuousIdBlock block = repository.findFirstByCategoryIdAndInstanceIdOrderByEndDesc(CATEGORY_ID, INSTANCE_ID);
        assertEquals(-1, block.getLastCommitted());
    }

    @Test
    public void assertCommitOutOfOrderDoesNotModifyLastCommittedUntilTheSequenceIsComplete() throws Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();
        long[] accessions1 = generator.generateAccessions(TENTH_BLOCK_SIZE);
        long[] accessions2 = generator.generateAccessions(TENTH_BLOCK_SIZE);

        generator.commit(accessions2);

        ContinuousIdBlock block = repository.findFirstByCategoryIdAndInstanceIdOrderByEndDesc(CATEGORY_ID, INSTANCE_ID);
        assertEquals(-1, block.getLastCommitted());

        generator.commit(accessions1);

        block = repository.findFirstByCategoryIdAndInstanceIdOrderByEndDesc(CATEGORY_ID, INSTANCE_ID);
        assertEquals(2 * TENTH_BLOCK_SIZE - 1, block.getLastCommitted());
    }

    @Test
    public void assertCommitOutOfOrderDoesNotModifyLastCommittedUntilTheSequenceIsCompleteMultipleBlocks() throws
            Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();
        long[] accessions1 = generator.generateAccessions(BLOCK_SIZE + TENTH_BLOCK_SIZE);
        long[] accessions2 = generator.generateAccessions(TENTH_BLOCK_SIZE);

        generator.commit(accessions2);

        ContinuousIdBlock block = repository.findFirstByCategoryIdAndInstanceIdOrderByEndDesc(CATEGORY_ID, INSTANCE_ID);
        assertEquals(BLOCK_SIZE, block.getStart());
        assertEquals(BLOCK_SIZE - 1, block.getLastCommitted());

        generator.commit(accessions1);

        block = repository.findFirstByCategoryIdAndInstanceIdOrderByEndDesc(CATEGORY_ID, INSTANCE_ID);
        assertEquals(BLOCK_SIZE, block.getStart());
        assertEquals(BLOCK_SIZE + 2 * TENTH_BLOCK_SIZE - 1, block.getLastCommitted());
    }

    @Test
    public void assertGenerateDoesNotReuseIds() throws Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();
        generator.generateAccessions(TENTH_BLOCK_SIZE);
        long[] accessions2 = generator.generateAccessions(TENTH_BLOCK_SIZE);
        assertEquals(TENTH_BLOCK_SIZE, accessions2[0]);
    }

    @Test
    public void assertReleaseMakesGenerateReuseIds() throws Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();
        long[] accessions1 = generator.generateAccessions(TENTH_BLOCK_SIZE);
        generator.release(accessions1);
        long[] accessions2 = generator.generateAccessions(TENTH_BLOCK_SIZE);
        assertEquals(0, accessions2[0]);
    }

    @Test
    public void assertReleaseSomeIdsMakesGenerateReuseReleasedIdsAndNewOnes() throws Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();
        generator.generateAccessions(TENTH_BLOCK_SIZE);
        generator.release(0, 1);
        long[] accessions2 = generator.generateAccessions(TENTH_BLOCK_SIZE);
        assertEquals(0, accessions2[0]);
        assertEquals(1, accessions2[1]);
        assertEquals(TENTH_BLOCK_SIZE, accessions2[2]);
    }

    @Test
    public void assertMultipleReleaseSomeIdsMakesGenerateReuseReleasedIdsAndNewOnes() throws Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();
        long[] accessions1 = generator.generateAccessions(TENTH_BLOCK_SIZE);
        generator.release(0, 1);
        generator.release(3, 4, 5);
        generator.release(8, 9, 10);
        long[] accessions2 = generator.generateAccessions(TENTH_BLOCK_SIZE);
        assertEquals(0, accessions2[0]);
        assertEquals(1, accessions2[1]);
        assertEquals(3, accessions2[2]);
        assertEquals(4, accessions2[3]);
        assertEquals(5, accessions2[4]);
        assertEquals(8, accessions2[5]);
        assertEquals(9, accessions2[6]);
        assertEquals(10, accessions2[7]);
        assertEquals(TENTH_BLOCK_SIZE, accessions2[8]);
    }

    @Test
    public void assertMultipleReleaseAndCommitsWorks() throws Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();
        generator.generateAccessions(BLOCK_SIZE);
        generator.commit(2);
        generator.release(0, 1);
        generator.release(3, 4, 5);
        generator.commit(6, 7, 11);
        generator.release(8, 9, 10);
        generator.commit(getLongArray(12, 998));
        //999 is waiting somewhere taking a big nap and no elements have been confirmed due to element 0 being released
        ContinuousIdBlock block = repository.findFirstByCategoryIdAndInstanceIdOrderByEndDesc(CATEGORY_ID, INSTANCE_ID);
        assertEquals(-1, block.getLastCommitted());

        long[] accessions2 = generator.generateAccessions(BLOCK_SIZE);
        assertEquals(0, accessions2[0]);
        assertEquals(1, accessions2[1]);
        assertEquals(3, accessions2[2]);
        assertEquals(4, accessions2[3]);
        assertEquals(5, accessions2[4]);
        assertEquals(8, accessions2[5]);
        assertEquals(9, accessions2[6]);
        assertEquals(10, accessions2[7]);
        assertEquals(1000, accessions2[8]);
        assertEquals(1092, accessions2[100]);

        // Only 998 elements have been confirmed due to 999 not being confirmed, reread the block to assert it
        generator.commit(accessions2);
        block = repository.findOne(block.getId());
        assertEquals(998, block.getLastCommitted());
        // 999 is committed and then the remaining elements get confirmed
        generator.commit(999);
        block = repository.findFirstByCategoryIdAndInstanceIdOrderByEndDesc(CATEGORY_ID, INSTANCE_ID);
        assertEquals(1991, block.getLastCommitted());
    }

    @Test
    public void assertRecoverNoPendingCommit() throws Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();
        long[] accessions1 = generator.generateAccessions(BLOCK_SIZE);
        // Now assume that the db layer has stored some elements and that the application has died and restarted.

        MonotonicAccessionGenerator generatorRecovering = new MonotonicAccessionGenerator(BLOCK_SIZE, CATEGORY_ID,
                INSTANCE_ID, repository);
        generatorRecovering.afterPropertiesSet();

        generatorRecovering.recoverState(new long[]{2, 3, 5});
        ContinuousIdBlock block = repository.findFirstByCategoryIdAndInstanceIdOrderByEndDesc(CATEGORY_ID,
                INSTANCE_ID);
        assertEquals(-1, block.getLastCommitted());
        assertFalse(generatorRecovering.getAvailableRanges().isEmpty());
        assertThat(generatorRecovering.getAvailableRanges(),
                contains(new MonotonicRange(0, 1), new MonotonicRange(4, 4), new MonotonicRange(6, BLOCK_SIZE - 1)));
    }

    @Test
    public void assertRecoverPendingCommit() throws Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();
        long[] accessions1 = generator.generateAccessions(BLOCK_SIZE);
        generator.commit(0, 1);
        // Now assume that the db layer has stored some elements and that the application has died and restarted.

        MonotonicAccessionGenerator generatorRecovering = new MonotonicAccessionGenerator(BLOCK_SIZE, CATEGORY_ID,
                INSTANCE_ID, repository);
        generatorRecovering.afterPropertiesSet();

        generatorRecovering.recoverState(new long[]{2, 3, 5});
        ContinuousIdBlock block = repository.findFirstByCategoryIdAndInstanceIdOrderByEndDesc(CATEGORY_ID,
                INSTANCE_ID);
        assertEquals(3, block.getLastCommitted());
        assertThat(generatorRecovering.getAvailableRanges(),
                contains(new MonotonicRange(4, 4), new MonotonicRange(6, BLOCK_SIZE - 1)));
    }

    @Test(expected = AccessionIsNotPending.class)
    public void assertReleaseAndCommitSameElement() throws Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();
        generator.generateAccessions(BLOCK_SIZE);
        generator.release(2);
        generator.commit(2);
    }

    @Test(expected = AccessionIsNotPending.class)
    public void assertCommitAndReleaseSameElement() throws Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();
        generator.generateAccessions(BLOCK_SIZE);
        generator.commit(2);
        generator.release(2);
    }

    @Test(expected = AccessionIsNotPending.class)
    public void releaseSomeIdsTwice() throws Exception {
        MonotonicAccessionGenerator generator = getMonotonicAccessionGenerator();
        generator.generateAccessions(TENTH_BLOCK_SIZE);
        generator.release(0, 1);
        generator.release(0, 1);
    }

    private long[] getLongArray(int start, int end) {
        final int totalElements = end - start + 1;
        long[] temp = new long[totalElements];
        for (int i = 0; i < totalElements; i++) {
            temp[i] = i + start;
        }
        return temp;
    }

}
