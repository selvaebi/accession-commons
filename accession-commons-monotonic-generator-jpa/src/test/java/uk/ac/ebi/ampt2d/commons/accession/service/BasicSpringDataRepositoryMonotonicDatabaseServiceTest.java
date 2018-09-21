/*
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
 */
package uk.ac.ebi.ampt2d.commons.accession.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.commons.accession.generators.monotonic.MonotonicRange;
import uk.ac.ebi.ampt2d.test.configuration.TestMonotonicDatabaseServiceTestConfiguration;
import uk.ac.ebi.ampt2d.test.persistence.TestMonotonicEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestMonotonicRepository;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {TestMonotonicDatabaseServiceTestConfiguration.class})
public class BasicSpringDataRepositoryMonotonicDatabaseServiceTest {

    private static final long ACCESSION_1 = 10L;

    private static final long ACCESSION_3 = 120L;

    private static final long ACCESSION_2 = 20L;

    @Autowired
    private TestMonotonicRepository repository;

    @Autowired
    private BasicSpringDataRepositoryMonotonicDatabaseService service;

    @Test
    public void recoverUnconfirmedAccessions() {
        repository.insert(Collections.singletonList(new TestMonotonicEntity(10L, "message", 1, "value")));
        long[] accessionsInRanges = service.getAccessionsInRanges(
                Collections.singletonList(new MonotonicRange(0L, 100L)));
        assertEquals(1, accessionsInRanges.length);
        assertEquals(ACCESSION_1, accessionsInRanges[0]);
    }

    @Test
    public void recoverUnconfirmedAccessionsSeveralEntities() {
        repository.insert(Arrays.asList(
                new TestMonotonicEntity(10L, "message", 1, "value 1"),
                new TestMonotonicEntity(20L, "message", 1, "value 2")
        ));
        long[] accessionsInRanges = service.getAccessionsInRanges(
                Collections.singletonList(new MonotonicRange(0L, 100L)));
        assertEquals(2, accessionsInRanges.length);
        assertEquals(ACCESSION_1, accessionsInRanges[0]);
        assertEquals(ACCESSION_2, accessionsInRanges[1]);
    }

    @Test
    public void recoverUnconfirmedAccessionsSeveralBlocks() {
        repository.insert(Arrays.asList(
                new TestMonotonicEntity(10L, "message", 1, "value 1"),
                new TestMonotonicEntity(120L, "message", 1, "value 2")
        ));
        long[] accessionsInRanges = service.getAccessionsInRanges(
                Arrays.asList(
                        new MonotonicRange(0L, 100L),
                        new MonotonicRange(100L, 200L)
                ));
        assertEquals(2, accessionsInRanges.length);
        assertEquals(ACCESSION_1, accessionsInRanges[0]);
        assertEquals(ACCESSION_3, accessionsInRanges[1]);
    }
}