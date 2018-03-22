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
import uk.ac.ebi.ampt2d.commons.accession.generators.exceptions.AccessionCouldNotBeGeneratedException;
import uk.ac.ebi.ampt2d.commons.accession.persistence.AccessionStatus;
import uk.ac.ebi.ampt2d.commons.accession.persistence.BasicSpringDataRepositoryDatabaseService;
import uk.ac.ebi.ampt2d.test.TestModel;
import uk.ac.ebi.ampt2d.test.configuration.TestDatabaseServiceTestConfiguration;
import uk.ac.ebi.ampt2d.test.persistence.TestEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestRepositoryForAccessionsHistory;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {TestDatabaseServiceTestConfiguration.class})
public class TrackingHistoryOfAccessionsServiceTest {

    @Autowired
    private TestRepositoryForAccessionsHistory repositoryForAccessionsHistory;

    @Autowired
    private BasicSpringDataRepositoryDatabaseService<TestModel, TestEntity, String, String> databaseService;

    @Test
    public void testAccessionCreations() throws AccessionCouldNotBeGeneratedException {

        TrackingHistoryOfAccessionsService trackingHistoryOfAccessionsService = getTrackingDeprecationService();

        trackingHistoryOfAccessionsService.createOrUpdateAccessions(Arrays.asList("Accession-1",
                "Accession-2", "Accession-3"), AccessionStatus.CREATED, "");

        assertEquals(3, repositoryForAccessionsHistory.count());
    }

    @Test
    public void testAccessionDeprecation() throws AccessionCouldNotBeGeneratedException {
        TrackingHistoryOfAccessionsService trackingHistoryOfAccessionsService = getTrackingDeprecationService();

        List<String> listOfAccessions = Arrays.asList("Accession-1",
                "Accession-2", "Accession-3");

        trackingHistoryOfAccessionsService.createOrUpdateAccessions(listOfAccessions, AccessionStatus.CREATED, "");

        trackingHistoryOfAccessionsService.deprecateOrMergeAccessions(listOfAccessions.subList(0, 2), AccessionStatus
                        .DEPRECATED,
                "deprecating as moving to new version");

        assertEquals(2, repositoryForAccessionsHistory.findAllByStatus(AccessionStatus.DEPRECATED.name()).size());
        assertEquals(1, repositoryForAccessionsHistory.findAllByStatus(AccessionStatus.CREATED.name()).size());
    }

    @Test
    public void testAccessionMerge() throws AccessionCouldNotBeGeneratedException {
        TrackingHistoryOfAccessionsService trackingHistoryOfAccessionsService = getTrackingDeprecationService();

        List<String> listOfAccessions = Arrays.asList("Accession-1",
                "Accession-2", "Accession-3");

        trackingHistoryOfAccessionsService.createOrUpdateAccessions(listOfAccessions, AccessionStatus.CREATED, "");

        trackingHistoryOfAccessionsService.deprecateOrMergeAccessions(listOfAccessions.subList(0, 2), AccessionStatus
                        .MERGED,
                "Merging as duplicates");

        assertEquals(2, repositoryForAccessionsHistory.findAllByStatus(AccessionStatus.MERGED.name()).size());
        assertEquals(1, repositoryForAccessionsHistory.findAllByStatus(AccessionStatus.CREATED.name()).size());
    }

    @Test
    public void testAccessionUpdate() throws AccessionCouldNotBeGeneratedException {
        TrackingHistoryOfAccessionsService trackingHistoryOfAccessionsService = getTrackingDeprecationService();

        List<String> listOfAccessions = Arrays.asList("Accession-1",
                "Accession-2", "Accession-3");

        trackingHistoryOfAccessionsService.createOrUpdateAccessions(listOfAccessions, AccessionStatus.CREATED, "");

        trackingHistoryOfAccessionsService.createOrUpdateAccessions(listOfAccessions.subList(0, 2), AccessionStatus
                .UPDATED, "Updating");

        assertEquals(2, repositoryForAccessionsHistory.findAllByStatus(AccessionStatus.UPDATED.name()).size());
        assertEquals(1, repositoryForAccessionsHistory.findAllByStatus(AccessionStatus.CREATED.name()).size());
    }

    public TrackingHistoryOfAccessionsService getTrackingDeprecationService() {
        return new TrackingHistoryOfAccessionsService(databaseService, repositoryForAccessionsHistory, e -> System.out.println(""));
    }
}