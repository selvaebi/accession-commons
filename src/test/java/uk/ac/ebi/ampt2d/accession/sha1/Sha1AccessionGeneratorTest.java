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
import uk.ac.ebi.ampt2d.accession.study.StudyMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class Sha1AccessionGeneratorTest {

    private Map<String, String> studyMap1;
    private Map<String, String> studyMap2;
    private StudyMessage accessionObject1;
    private StudyMessage accessionObject2;

    @Before
    public void setUp() throws Exception {
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
    public void differentAccessionsAreGeneratedForDifferentInputs() throws Exception {
        SHA1AccessionGenerator<StudyMessage> generator = new SHA1AccessionGenerator<>();
        Map<StudyMessage, String> accessions = generator.generateAccessions(new HashSet<>(Arrays.asList
                (accessionObject1, accessionObject2)));
        String accession1 = accessions.get(accessionObject1);
        String accession2 = accessions.get(accessionObject2);
        assertTrue(accession1 != null);
        assertTrue(accession2 != null);
        assertNotEquals(accession1, accession2);
    }

    @Test
    public void oneGeneratorReturnsTheSameAccessionInDifferentCallsWithTheSameInput() {
        SHA1AccessionGenerator<StudyMessage> generator = new SHA1AccessionGenerator<>();
        Map<StudyMessage, String> accessions = generator.generateAccessions(Collections.singleton(accessionObject1));
        String accession1 = accessions.get(accessionObject1);
        accessions = generator.generateAccessions(Collections.singleton(accessionObject1));
        String anotherAccession1 = accessions.get(accessionObject1);
        assertEquals(accession1, anotherAccession1);
    }

    @Test
    public void twoDifferentGeneratorInstancesReturnTheSameAccessionForTheSameInput() {
        SHA1AccessionGenerator<StudyMessage> generator = new SHA1AccessionGenerator<>();
        Map<StudyMessage, String> accessions = generator.generateAccessions(Collections.singleton(accessionObject1));
        String accession1 = accessions.get(accessionObject1);
        SHA1AccessionGenerator<StudyMessage> generator2 = new SHA1AccessionGenerator<>();
        accessions = generator2.generateAccessions(Collections.singleton(accessionObject1));
        String anotherAccession1 = accessions.get(accessionObject1);
        assertEquals(accession1, anotherAccession1);
    }
}