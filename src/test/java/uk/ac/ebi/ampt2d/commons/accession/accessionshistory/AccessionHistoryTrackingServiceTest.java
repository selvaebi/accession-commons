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
package uk.ac.ebi.ampt2d.commons.accession.accessionshistory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.commons.accession.generators.exceptions.AccessionCouldNotBeGeneratedException;
import uk.ac.ebi.ampt2d.test.configuration.TestDatabaseServiceTestConfiguration;
import uk.ac.ebi.ampt2d.test.persistence.TestAccessionHistoryEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestAccessionHistoryRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {TestDatabaseServiceTestConfiguration.class})
public class AccessionHistoryTrackingServiceTest {

    @Autowired
    private TestAccessionHistoryRepository repositoryForAccessionsHistory;

    @Test
    public void testAccessionCreations() throws AccessionCouldNotBeGeneratedException {

        AccessionHistoryTrackingService<TestAccessionHistoryEntity, String>
                accessionHistoryTrackingService =
                getTrackingDeprecationService();

        accessionHistoryTrackingService.trackAccessionHistory(Arrays.asList("Accession-1",
                "Accession-2", "Accession-3"), AccessionStatus.CREATED, "");

        assertEquals(3, repositoryForAccessionsHistory.count());
    }

    @Test
    public void testAccessionDeprecation() throws AccessionCouldNotBeGeneratedException {
        AccessionHistoryTrackingService<TestAccessionHistoryEntity, String> accessionHistoryTrackingService = getTrackingDeprecationService();

        List<String> listOfAccessions = Arrays.asList("Accession-1",
                "Accession-2", "Accession-3");

        accessionHistoryTrackingService.trackAccessionHistory(listOfAccessions, AccessionStatus.CREATED, "");

        accessionHistoryTrackingService.trackAccessionHistory(listOfAccessions.subList(0, 2), AccessionStatus
                        .DEPRECATED,
                "deprecating as moving to new version");

        assertEquals(2, repositoryForAccessionsHistory.findAllByStatus(AccessionStatus.DEPRECATED).size());
        assertEquals(1, repositoryForAccessionsHistory.findAllByStatus(AccessionStatus.CREATED).size());
    }

    @Test
    public void testAccessionMerge() throws AccessionCouldNotBeGeneratedException {
        AccessionHistoryTrackingService<TestAccessionHistoryEntity, String> accessionHistoryTrackingService = getTrackingDeprecationService();

        List<String> listOfAccessions = Arrays.asList("Accession-1",
                "Accession-2", "Accession-3");

        accessionHistoryTrackingService.trackAccessionHistory(listOfAccessions, AccessionStatus.CREATED, "");

        accessionHistoryTrackingService.trackAccessionHistory(listOfAccessions.subList(0, 2), AccessionStatus
                        .MERGED,
                "Merging as duplicates");

        assertEquals(2, repositoryForAccessionsHistory.findAllByStatus(AccessionStatus.MERGED).size());
        assertEquals(1, repositoryForAccessionsHistory.findAllByStatus(AccessionStatus.CREATED).size());
    }

    @Test
    public void testAccessionUpdate() throws AccessionCouldNotBeGeneratedException {
        AccessionHistoryTrackingService<TestAccessionHistoryEntity, String> accessionHistoryTrackingService = getTrackingDeprecationService();

        List<String> listOfAccessions = Arrays.asList("Accession-1",
                "Accession-2", "Accession-3");

        accessionHistoryTrackingService.trackAccessionHistory(listOfAccessions, AccessionStatus.CREATED, "");

        accessionHistoryTrackingService.trackAccessionHistory(listOfAccessions.subList(0, 2), AccessionStatus
                .UPDATED, "Updating");

        assertEquals(2, repositoryForAccessionsHistory.findAllByStatus(AccessionStatus.UPDATED).size());
        assertEquals(1, repositoryForAccessionsHistory.findAllByStatus(AccessionStatus.CREATED).size());
    }

    public AccessionHistoryTrackingService getTrackingDeprecationService() {
        return new AccessionHistoryTrackingService<>(repositoryForAccessionsHistory,
                TestAccessionHistoryEntity::new);
    }
}