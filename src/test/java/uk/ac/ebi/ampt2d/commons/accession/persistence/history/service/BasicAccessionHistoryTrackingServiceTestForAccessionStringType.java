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
package uk.ac.ebi.ampt2d.commons.accession.persistence.history.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.commons.accession.core.AccessionStatus;
import uk.ac.ebi.ampt2d.test.configuration.TestStringAccessionHistoryConfiguration;
import uk.ac.ebi.ampt2d.test.persistence.TestAccessionHistoryStringEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestAccessionHistoryStringRepository;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = TestStringAccessionHistoryConfiguration.class)
public class BasicAccessionHistoryTrackingServiceTestForAccessionStringType {

    @Autowired
    private TestAccessionHistoryStringRepository stringAccessionRepository;

    @Autowired
    private BasicAccessionHistoryTrackingService<TestAccessionHistoryStringEntity, String>
            historyTrackingServiceForStringEntity;

    @Test
    public void merge() throws Exception {
        historyTrackingServiceForStringEntity.merge(Arrays.asList("Accession1", "Accession2", "Accession3"),
                "MergeReason");

        assertEquals(3, stringAccessionRepository.findAllByAccessionStatus(AccessionStatus.MERGED).size());
    }

    @Test
    public void update() throws Exception {
        historyTrackingServiceForStringEntity.update(Arrays.asList("Accession1", "Accession2", "Accession3"),
                "UpdateReason");

        assertEquals(3, stringAccessionRepository.findAllByAccessionStatus(AccessionStatus.UPDATED).size());
    }

    @Test
    public void deprecate() throws Exception {
        historyTrackingServiceForStringEntity.deprecate(Arrays.asList("Accession1", "Accession2", "Accession3"),
                "DeprecateReason");

        assertEquals(3, stringAccessionRepository.findAllByAccessionStatus(AccessionStatus.DEPRECATED).size());
    }

}