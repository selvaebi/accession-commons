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
package uk.ac.ebi.ampt2d.accession.sample;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.ampt2d.accession.AccessionGenerator;
import uk.ac.ebi.ampt2d.accession.DatabaseService;
import uk.ac.ebi.ampt2d.accession.WebConfiguration;
import uk.ac.ebi.ampt2d.accession.sha1.SHA1AccessionGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@DataJpaTest
@TestPropertySource(properties = "services=sample-accession")
@ContextConfiguration(classes = WebConfiguration.class)
public class SampleAccessioningDatabaseServiceTest {

    @Autowired
    private DatabaseService sampleDatabaseService;

    private AccessionGenerator<SampleMessage, String> generator;

    private AccessionGenerator<SampleMessage, String> alternativeGenerator;

    private Map<String, String> sampleMap1;
    private Map<String, String> sampleMap2;
    private SampleMessage accessionObject1;
    private SampleMessage accessionObject2;

    @Before
    public void setUp() throws Exception {
        generator = new SHA1AccessionGenerator();
        alternativeGenerator = new SHA1AccessionGenerator();
        sampleMap1 = new HashMap<>();
        sampleMap1.put("title", "Title1");
        sampleMap1.put("type", "Type1");
        sampleMap1.put("submitterEmail", "Email1");
        sampleMap2 = new HashMap<>();
        sampleMap2.put("title", "Title2");
        sampleMap2.put("type", "Type2");
        sampleMap2.put("submitterEmail", "Email2");
        accessionObject1 = new SampleMessage(sampleMap1);
        accessionObject2 = new SampleMessage(sampleMap2);
    }

    @Test
    public void testSamplesAreStoredInTheRepositoryAndRetrived() throws Exception {
        Map<SampleMessage, String> accessionedSamples = generator.generateAccessions(new HashSet<>(Arrays.asList
                (accessionObject1, accessionObject2)));
        sampleDatabaseService.save(accessionedSamples);
        Collection<String> hashes = new ArrayList<>();
        hashes.add(accessionObject1.getHashableMessage());
        hashes.add(accessionObject2.getHashableMessage());
        Map<SampleMessage, String> accessionsFromRepository = sampleDatabaseService.findObjectsInDB(Arrays.asList
                (accessionObject1, accessionObject2));
        assertEquals(2, accessionsFromRepository.size());
    }

    //JpaSystemException is due to the id of entity being null
    @Test(expected = org.springframework.orm.jpa.JpaSystemException.class)
    public void cantStoreObjectsWithoutAccession() {
        Map<SampleMessage, String> accessionedSamples = new HashMap<>();
        accessionedSamples.put(accessionObject1, null);
        sampleDatabaseService.save(accessionedSamples);
    }
}
