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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.accession.AccessioningRepository;
import uk.ac.ebi.ampt2d.accession.AccessioningService;
import uk.ac.ebi.ampt2d.accession.WebConfiguration;
import uk.ac.ebi.ampt2d.accession.sample.SampleMessage;

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
@TestPropertySource(properties = "services=sample-accession")
@ComponentScan(basePackageClasses = AccessioningRepository.class)
@ContextConfiguration(classes = WebConfiguration.class)
public class SampleSha1AccessioningServiceTest {

    @Autowired
    private AccessioningService service;

    private Map<String, String> sampleMap1;
    private Map<String, String> sampleMap2;

    @Before
    public void setup() {
        sampleMap1 = new HashMap<>();
        sampleMap1.put("title", "Title1");
        sampleMap1.put("type", "Type1");
        sampleMap1.put("submitterEmail", "Email1");
        sampleMap2 = new HashMap<>();
        sampleMap2.put("title", "Title2");
        sampleMap2.put("type", "Type2");
        sampleMap2.put("submitterEmail", "Email2");
    }

    @Test
    public void sameAccessionsAreReturnedForIdenticalSamples() throws Exception {
        SampleMessage sample1 = new SampleMessage(sampleMap1);
        SampleMessage anotherSample1 = new SampleMessage(sampleMap1);
        Map<SampleMessage, String> generatedAccessions = service.getAccessions(Arrays.asList(sample1, anotherSample1));
        assertEquals(generatedAccessions.get(sample1), generatedAccessions.get(anotherSample1));
    }

    @Test
    public void everyNewObjectReceiveOneAccession() throws Exception {
        Map<String, String> sampleMap3 = new HashMap<>();
        sampleMap3.put("title", "Title2");
        sampleMap3.put("type", "Type2");
        sampleMap3.put("submitterEmail", "Email1");
        List<SampleMessage> newAccessionObjects = Arrays.asList(new SampleMessage(sampleMap1),
                new SampleMessage(sampleMap2), new SampleMessage(sampleMap3));
        Map<SampleMessage, String> accessions = service.getAccessions(newAccessionObjects);
        for (SampleMessage AccessionObject : newAccessionObjects) {
            assertNotNull(accessions.get(AccessionObject));
        }
    }

    @Test
    public void sameObjectsGetSameAccession() throws Exception {
        SampleMessage sample1 = new SampleMessage(sampleMap1);
        SampleMessage anotherSample1 = new SampleMessage(sampleMap1);
        Map<SampleMessage, String> accessions = service.getAccessions(Arrays.asList(sample1, anotherSample1));
        String accession1 = accessions.get(sample1);
        String anotherAccession1 = accessions.get(anotherSample1);
        assertEquals(accession1, anotherAccession1);
    }

    @Test
    public void differentObjectsGetDifferentAccessions() throws Exception {
        SampleMessage sample1 = new SampleMessage(sampleMap1);
        SampleMessage sample2 = new SampleMessage(sampleMap2);
        Map<SampleMessage, String> accessions = service.getAccessions(Arrays.asList(sample1, sample2));
        String accession1 = accessions.get(sample1);
        String accession2 = accessions.get(sample2);
        assertNotEquals(accession1, accession2);
    }

    @Test
    public void differentCallsToTheServiceUsingSameObjectsWillReturnSameAccessions() throws Exception {
        SampleMessage sample1 = new SampleMessage(sampleMap1);
        SampleMessage sample2 = new SampleMessage(sampleMap2);
        Map<SampleMessage, String> accessions = service.getAccessions(Arrays.asList(sample1, sample2));
        String accession1 = accessions.get(sample1);
        String accession2 = accessions.get(sample2);
        Map<SampleMessage, String> accessionsFromSecondServiceCall =
                service.getAccessions(Arrays.asList(sample1, sample2));
        assertEquals(accession1, accessionsFromSecondServiceCall.get(sample1));
        assertEquals(accession2, accessionsFromSecondServiceCall.get(sample2));
    }

    @Test
    public void mixingAlreadyAccessionedAndNewObjectsIsAllowed() throws Exception {
        SampleMessage sample1 = new SampleMessage(sampleMap1);
        SampleMessage sample2 = new SampleMessage(sampleMap2);
        Map<SampleMessage, String> accessions = service.getAccessions(Arrays.asList(sample1, sample2));
        String accession1 = accessions.get(sample1);
        String accession2 = accessions.get(sample2);
        Map<String, String> sampleMap3 = new HashMap<>();
        sampleMap3.put("title", "Title2");
        sampleMap3.put("type", "Type2");
        sampleMap3.put("submitterEmail", "Email1");
        SampleMessage sample3 = new SampleMessage(sampleMap3);
        List<SampleMessage> objectsToAccession = Arrays.asList(sample1, sample2, sample3);
        Map<SampleMessage, String> accessionsFromSecondServiceCall = service.getAccessions(objectsToAccession);
        assertEquals(accession1, accessionsFromSecondServiceCall.get(sample1));
        assertEquals(accession2, accessionsFromSecondServiceCall.get(sample2));
        assertNotNull(accessionsFromSecondServiceCall.get(sample3));
        assertEquals(objectsToAccession.size(),
                new HashSet<>(accessionsFromSecondServiceCall.values()).size());
    }

    @Test
    public void sameSampleSubmittedByDifferentSubmitterGetsDifferentAccession() throws Exception {
        sampleMap1.put("title", "Title1");
        sampleMap1.put("type", "Type1");
        sampleMap1.put("submitterEmail", "Email1");
        SampleMessage sample1 = new SampleMessage(sampleMap1);
        sampleMap2.put("title", "Title1");
        sampleMap2.put("type", "Type1");
        sampleMap2.put("submitterEmail", "Email2");
        SampleMessage sample2 = new SampleMessage(sampleMap2);
        Map<SampleMessage, String> accessions = service.getAccessions(Arrays.asList(sample1, sample2));
        String accession1 = accessions.get(sample1);
        String accession2 = accessions.get(sample2);
        assertNotEquals(accession1, accession2);
    }
}
