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
package uk.ac.ebi.ampt2d.accession.object;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.accession.AccessioningObject;
import uk.ac.ebi.ampt2d.accession.AccessioningService;

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
@TestPropertySource(properties = "services=object-accession")
@ComponentScan(basePackages = "uk.ac.ebi.ampt2d.accession.object")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ObjectAccessioningServiceTest {

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

        AccessioningObject study1 = new AccessionObject(studyMap1);
        AccessioningObject study2 = new AccessionObject(studyMap2);

        Map<AccessionObject, String> generatedAccessions = service.getAccessions(Arrays.asList(study1, study2));

        study1 = new AccessionObject(studyMap1);
        study2 = new AccessionObject(studyMap2);

        Map<AccessionObject, String> retrievedAccessions = service.getAccessions(Arrays.asList(study1, study2));

        assertEquals(generatedAccessions.get(study1), retrievedAccessions.get(study1));
        assertEquals(generatedAccessions.get(study2), retrievedAccessions.get(study2));
    }

    @Test
    public void everyNewObjectReceiveOneAccession() throws Exception {

        Map<String, String> studyMap3 = new HashMap<>();
        studyMap3.put("title", "Title2");
        studyMap3.put("type", "Type2");
        studyMap3.put("submitterEmail", "Email1");

        List<AccessionObject> newAccessionObjects = Arrays.asList(new AccessionObject(studyMap1),
                new AccessionObject(studyMap2), new AccessionObject(studyMap3));
        Map<AccessionObject, String> accessions = service.getAccessions(newAccessionObjects);

        for (AccessionObject AccessionObject : newAccessionObjects) {
            assertNotNull(accessions.get(AccessionObject));
        }
    }

    @Test
    public void sameObjectsGetSameAccession() throws Exception {

        AccessioningObject study1 = new AccessionObject(studyMap1);
        AccessioningObject study2 = new AccessionObject(studyMap1);

        Map<AccessionObject, String> accessions = service.getAccessions(Arrays.asList(study1, study2));

        String accession1 = accessions.get(study1);
        String anotherAccession1 = accessions.get(study2);

        assertEquals(accession1, anotherAccession1);
    }

    @Test
    public void differentObjectsGetDifferentAccessions() throws Exception {
        AccessioningObject study1 = new AccessionObject(studyMap1);
        AccessioningObject study2 = new AccessionObject(studyMap2);

        Map<AccessionObject, String> accessions = service.getAccessions(Arrays.asList(study1, study2));

        String accession1 = accessions.get(study1);
        String anotherAccession1 = accessions.get(study2);

        assertNotEquals(accession1, anotherAccession1);
    }

    @Test
    public void differentCallsToTheServiceUsingSameObjectsWillReturnSameAccessions() throws Exception {
        AccessioningObject study1 = new AccessionObject(studyMap1);
        AccessioningObject study2 = new AccessionObject(studyMap2);

        Map<AccessionObject, String> accessions = service.getAccessions(Arrays.asList(study1, study2));

        String accession1 = accessions.get(study1);
        String anotherAccession1 = accessions.get(study2);

        Map<AccessionObject, String> accessionsFromSecondServiceCall = service.getAccessions(Arrays.asList(study1, study2));

        assertEquals(accession1, accessionsFromSecondServiceCall.get(study1));
        assertEquals(anotherAccession1, accessionsFromSecondServiceCall.get(study2));
    }

    @Test
    public void mixingAlreadyAccessionedAndNewObjectsIsAllowed() throws Exception {
        AccessioningObject study1 = new AccessionObject(studyMap1);
        AccessioningObject study2 = new AccessionObject(studyMap2);

        Map<AccessionObject, String> accessions = service.getAccessions(Arrays.asList(study1, study2));

        String accession1 = accessions.get(study1);
        String anotherAccession1 = accessions.get(study2);

        Map<String, String> studyMap3 = new HashMap<>();
        studyMap3.put("title", "Title2");
        studyMap3.put("type", "Type2");
        studyMap3.put("submitterEmail", "Email1");
        AccessioningObject study3 = new AccessionObject(studyMap3);

        List<AccessioningObject> objectsToAccession = Arrays.asList(study1, study2, study3);
        Map<AccessionObject, String> accessionsFromSecondServiceCall = service
                .getAccessions(objectsToAccession);

        assertEquals(accession1, accessionsFromSecondServiceCall.get(study1));
        assertEquals(anotherAccession1, accessionsFromSecondServiceCall.get(study2));
        assertNotNull(accessionsFromSecondServiceCall.get(study3));

        assertEquals(objectsToAccession.size(),
                new HashSet<>(accessionsFromSecondServiceCall.values()).size());
    }

    @Test
    public void sameStudySubmittedByDifferentSubmitterGetsDifferentAccession() throws Exception {

        studyMap1.put("title", "Title1");
        studyMap1.put("type", "Type1");
        studyMap1.put("submitterEmail", "Email1");
        AccessioningObject study1 = new AccessionObject(studyMap1);
        studyMap2.put("title", "Title1");
        studyMap2.put("type", "Type1");
        studyMap2.put("submitterEmail", "Email2");
        AccessioningObject study2 = new AccessionObject(studyMap2);

        Map<AccessionObject, String> accessions = service.getAccessions(Arrays.asList(study1, study2));

        String accession1 = accessions.get(study1);
        String anotherAccession1 = accessions.get(study2);

        assertNotEquals(accession1, anotherAccession1);
    }
}
