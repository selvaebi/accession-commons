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
package uk.ac.ebi.ampt2d.accession.study;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.ampt2d.accession.AccessionGenerator;
import uk.ac.ebi.ampt2d.accession.DatabaseService;
import uk.ac.ebi.ampt2d.accession.sha1.SHA1AccessionGenerator;
import uk.ac.ebi.ampt2d.test.configurationaccession.StudyAccessioningDatabaseServiceTestConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@DataJpaTest
@TestPropertySource(properties = "services=study-accession")
@Import(StudyAccessioningDatabaseServiceTestConfiguration.class)
public class StudyAccessioningDatabaseServiceTest {

    @Autowired
    private DatabaseService studyDatabaseService;

    private AccessionGenerator<StudyMessage, String> generator;

    private AccessionGenerator<StudyMessage, String> alternativeGenerator;

    private Map<String, String> studyMap1;
    private Map<String, String> studyMap2;
    private StudyMessage accessionObject1;
    private StudyMessage accessionObject2;

    @Before
    public void setUp() throws Exception {
        generator = new SHA1AccessionGenerator();
        alternativeGenerator = new SHA1AccessionGenerator();
        studyMap1 = new HashMap<>();
        studyMap1.put("title", "Title1");
        studyMap1.put("type", "Type1");
        studyMap1.put("submitterEmail", "Email1");
        studyMap2 = new HashMap<>();
        studyMap2.put("title", "Title2");
        studyMap2.put("type", "Type2");
        studyMap2.put("submitterEmail", "Email2");
        accessionObject1 = new StudyMessage(studyMap1);
        accessionObject2 = new StudyMessage(studyMap2);
    }

    @Test
    public void testStudiesAreStoredInTheRepositoryAndRetrived() throws Exception {
        Map<StudyMessage, String> accessionedStudies = generator.generateAccessions(new HashSet<>(Arrays.asList
                (accessionObject1, accessionObject2)));
        studyDatabaseService.save(accessionedStudies);
        Collection<String> hashes = new ArrayList<>();
        hashes.add(accessionObject1.getHashableMessage());
        hashes.add(accessionObject2.getHashableMessage());
        Map<StudyMessage, String> accessionsFromRepository = studyDatabaseService.findObjectsInDB(Arrays.asList
                (accessionObject1, accessionObject2));
        assertEquals(2, accessionsFromRepository.size());
    }

    //JpaSystemException is due to the id of entity being null
    @Test(expected = org.springframework.orm.jpa.JpaSystemException.class)
    public void cantStoreObjectsWithoutAccession() {
        Map<StudyMessage, String> accessionedStudies = new HashMap<>();
        accessionedStudies.put(accessionObject1, null);
        studyDatabaseService.save(accessionedStudies);
    }
}
