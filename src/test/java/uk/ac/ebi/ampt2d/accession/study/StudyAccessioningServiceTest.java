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
package uk.ac.ebi.ampt2d.accession.study;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.accession.AccessioningService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(properties = "services=study-uuid")
@ComponentScan(basePackages = "uk.ac.ebi.ampt2d.accession.study")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class StudyAccessioningServiceTest {

    @Autowired
    private AccessioningService service;

    @Test
    public void sameAccessionsAreReturnedForIdenticalFiles() throws Exception {

        Study study1 = new Study("Title1", "Type1", "Email1");
        Study study2 = new Study("Title2", "Type2", "Email2");

        Map<Study, UUID> generatedAccessions = service.getAccessions(Arrays.asList(study1, study2));

        study1 = new Study("Title1", "Type1", "Email1");
        study2 = new Study("Title2", "Type2", "Email2");

        Map<Study, UUID> retrievedAccessions = service.getAccessions(Arrays.asList(study1, study2));

        assertEquals(generatedAccessions.get(study1), retrievedAccessions.get(study1));
        assertEquals(generatedAccessions.get(study2), retrievedAccessions.get(study2));
    }

    @Test
    public void everyNewObjectReceiveOneAccession() throws Exception {
        List<Study> newObjects = Arrays.asList(new Study("Title1", "Type1", "Email1"),
                new Study("Title1", "Type2", "Email1"), new Study("Title1", "Type1", "Email2"));
        Map<Study, UUID> accessions = service.getAccessions(newObjects);

        for (Study object : newObjects) {
            assertNotNull(accessions.get(object));
        }
    }

    @Test
    public void sameObjectsGetSameAccession() throws Exception {

        Study study1 = new Study("Title1", "Type1", "Email1");
        Study study2 = new Study("Title1", "Type1", "Email1");

        List<Study> newObjects = Arrays.asList(study1, study2);
        Map<Study, UUID> accessions = service.getAccessions(newObjects);

        UUID accession1 = accessions.get(study1);
        UUID anotherAccession1 = accessions.get(study2);

        assertEquals(accession1, anotherAccession1);
    }

    @Test
    public void differentObjectsGetDifferentAccessions() throws Exception {
        Study study1 = new Study("Title1", "Type1", "Email1");
        Study study2 = new Study("Title2", "Type2", "Email2");

        List<Study> newObjects = Arrays.asList(study1, study2);
        Map<Study, UUID> accessions = service.getAccessions(newObjects);

        UUID accession1 = accessions.get(study1);
        UUID anotherAccession1 = accessions.get(study2);

        assertNotEquals(accession1, anotherAccession1);
    }

    @Test
    public void differentCallsToTheServiceUsingSameObjectsWillReturnSameAccessions() throws Exception {
        Study study1 = new Study("Title1", "Type1", "Email1");
        Study study2 = new Study("Title2", "Type2", "Email2");

        List<Study> newObjects = Arrays.asList(study1, study2);
        Map<Study, UUID> accessions = service.getAccessions(newObjects);

        UUID accession1 = accessions.get(study1);
        UUID anotherAccession1 = accessions.get(study2);

        Map<Study, UUID> accessionsFromSecondServiceCall = service.getAccessions(Arrays.asList(study1, study2));

        assertEquals(accession1, accessionsFromSecondServiceCall.get(study1));
        assertEquals(anotherAccession1, accessionsFromSecondServiceCall.get(study2));
    }

    @Test
    public void mixingAlreadyAccessionedAndNewObjectsIsAllowed() throws Exception {
        Study study1 = new Study("Title1", "Type1", "Email1");
        Study study2 = new Study("Title2", "Type2", "Email2");

        Map<Study, UUID> accessions = service.getAccessions(Arrays.asList(study1, study2));

        UUID accession1 = accessions.get(study1);
        UUID anotherAccession1 = accessions.get(study2);

        Study study3 = new Study("Title3", "Type3", "Email4");
        Study study4 = new Study("Title4", "Type1", "Email4");

        List<Study> objectsToAccession = Arrays.asList(study1, study2, study3, study4);
        Map<Study, UUID> accessionsFromSecondServiceCall = service
                .getAccessions(objectsToAccession);

        assertEquals(accession1, accessionsFromSecondServiceCall.get(study1));
        assertEquals(anotherAccession1, accessionsFromSecondServiceCall.get(study2));
        assertNotNull(accessionsFromSecondServiceCall.get(study3));
        assertNotNull(accessionsFromSecondServiceCall.get(study4));

        assertEquals(objectsToAccession.size(),
                new HashSet<>(accessionsFromSecondServiceCall.values()).size());
    }

    @Test
    public void sameStudySubmittedByDifferentSubmitterGetsDifferentAccession() throws Exception {
        Study study1 = new Study("Title1", "Type1", "Email1");
        Study study2 = new Study("Title1", "Type1", "Email2");

        Map<Study, UUID> accessions = service.getAccessions(Arrays.asList(study1, study2));

        UUID accession1 = accessions.get(study1);
        UUID anotherAccession1 = accessions.get(study2);

        assertNotEquals(accession1, anotherAccession1);
    }
}
