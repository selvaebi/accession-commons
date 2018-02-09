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
package uk.ac.ebi.ampt2d.accession.sha1;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.accession.AccessioningService;
import uk.ac.ebi.ampt2d.accession.study.StudyMessage;
import uk.ac.ebi.ampt2d.test.configurationaccession.StudyAccessioningDatabaseServiceTestConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(properties = "services=study-accession")
@Import(StudyAccessioningDatabaseServiceTestConfiguration.class)
public class StudySha1AccessioningServiceTest {

    @Autowired
    private AccessioningService service;

    private Map<String, String> studyMap1;
    private Map<String, String> studyMap2;

    @Before
    public void setup() {
        studyMap1 = new HashMap<>();
        studyMap1.put("title", "Title1");
        studyMap1.put("type", "Type1");
        studyMap1.put("submitterEmail", "Email1");
        studyMap2 = new HashMap<>();
        studyMap2.put("title", "Title2");
        studyMap2.put("type", "Type2");
        studyMap2.put("submitterEmail", "Email2");
    }

    @Test
    public void sameAccessionsAreReturnedForIdenticalStudies() throws Exception {
        StudyMessage study1 = new StudyMessage(studyMap1);
        StudyMessage anotherStudy1 = new StudyMessage(studyMap1);
        Map<StudyMessage, String> generatedAccessions = service.getAccessions(Arrays.asList(study1, anotherStudy1));
        assertEquals(generatedAccessions.get(study1), generatedAccessions.get(anotherStudy1));
    }

    @Test
    public void everyNewObjectReceiveOneAccession() throws Exception {
        Map<String, String> studyMap3 = new HashMap<>();
        studyMap3.put("title", "Title2");
        studyMap3.put("type", "Type2");
        studyMap3.put("submitterEmail", "Email1");
        List<StudyMessage> newAccessionObjects = Arrays.asList(new StudyMessage(studyMap1),
                new StudyMessage(studyMap2), new StudyMessage(studyMap3));
        Map<StudyMessage, String> accessions = service.getAccessions(newAccessionObjects);
        for (StudyMessage AccessionObject : newAccessionObjects) {
            assertNotNull(accessions.get(AccessionObject));
        }
    }

    @Test
    public void sameObjectsGetSameAccession() throws Exception {
        StudyMessage study1 = new StudyMessage(studyMap1);
        StudyMessage anotherStudy1 = new StudyMessage(studyMap1);
        Map<StudyMessage, String> accessions = service.getAccessions(Arrays.asList(study1, anotherStudy1));
        String accession1 = accessions.get(study1);
        String anotherAccession1 = accessions.get(anotherStudy1);
        assertEquals(accession1, anotherAccession1);
    }

    @Test
    public void differentObjectsGetDifferentAccessions() throws Exception {
        StudyMessage study1 = new StudyMessage(studyMap1);
        StudyMessage study2 = new StudyMessage(studyMap2);
        Map<StudyMessage, String> accessions = service.getAccessions(Arrays.asList(study1, study2));
        String accession1 = accessions.get(study1);
        String accession2 = accessions.get(study2);
        assertNotEquals(accession1, accession2);
    }

    @Test
    public void differentCallsToTheServiceUsingSameObjectsWillReturnSameAccessions() throws Exception {
        StudyMessage study1 = new StudyMessage(studyMap1);
        StudyMessage study2 = new StudyMessage(studyMap2);
        Map<StudyMessage, String> accessions = service.getAccessions(Arrays.asList(study1, study2));
        String accession1 = accessions.get(study1);
        String accession2 = accessions.get(study2);
        Map<StudyMessage, String> accessionsFromSecondServiceCall =
                service.getAccessions(Arrays.asList(study1, study2));
        assertEquals(accession1, accessionsFromSecondServiceCall.get(study1));
        assertEquals(accession2, accessionsFromSecondServiceCall.get(study2));
    }

    @Test
    public void mixingAlreadyAccessionedAndNewObjectsIsAllowed() throws Exception {
        StudyMessage study1 = new StudyMessage(studyMap1);
        StudyMessage study2 = new StudyMessage(studyMap2);
        Map<StudyMessage, String> accessions = service.getAccessions(Arrays.asList(study1, study2));
        String accession1 = accessions.get(study1);
        String accession2 = accessions.get(study2);
        Map<String, String> studyMap3 = new HashMap<>();
        studyMap3.put("title", "Title2");
        studyMap3.put("type", "Type2");
        studyMap3.put("submitterEmail", "Email1");
        StudyMessage study3 = new StudyMessage(studyMap3);
        List<StudyMessage> objectsToAccession = Arrays.asList(study1, study2, study3);
        Map<StudyMessage, String> accessionsFromSecondServiceCall = service.getAccessions(objectsToAccession);
        assertEquals(accession1, accessionsFromSecondServiceCall.get(study1));
        assertEquals(accession2, accessionsFromSecondServiceCall.get(study2));
        assertNotNull(accessionsFromSecondServiceCall.get(study3));
        assertEquals(objectsToAccession.size(),
                new HashSet<>(accessionsFromSecondServiceCall.values()).size());
    }

    @Test
    public void sameStudySubmittedByDifferentSubmitterGetsDifferentAccession() throws Exception {
        studyMap1.put("title", "Title1");
        studyMap1.put("type", "Type1");
        studyMap1.put("submitterEmail", "Email1");
        StudyMessage study1 = new StudyMessage(studyMap1);
        studyMap2.put("title", "Title1");
        studyMap2.put("type", "Type1");
        studyMap2.put("submitterEmail", "Email2");
        StudyMessage study2 = new StudyMessage(studyMap2);
        Map<StudyMessage, String> accessions = service.getAccessions(Arrays.asList(study1, study2));
        String accession1 = accessions.get(study1);
        String accession2 = accessions.get(study2);
        assertNotEquals(accession1, accession2);
    }
}
