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
package uk.ac.ebi.ampt2d.accession;

import org.junit.Test;
import uk.ac.ebi.ampt2d.accession.file.FileMessage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BasicAccessionGeneratorTest {

    @Test
    public void testUseMessageAsAccession() throws Exception {
        BasicAccessionGenerator<FileMessage> generator = new BasicAccessionGenerator<>();
        String checksumA = "checksumA";
        String checksumB = "checksumB";
        FileMessage fileA = new FileMessage(checksumA);
        FileMessage fileB = new FileMessage(checksumB);

        Map<FileMessage, String> generatedAccessions = generator.generateAccessions(new HashSet(Arrays.asList(fileA, fileB)));
        String accession1 = generatedAccessions.get(fileA);
        String accession2 = generatedAccessions.get(fileB);
        assertTrue(accession1 != null);
        assertTrue(accession2 != null);
        assertEquals(checksumA, accession1);
        assertEquals(checksumB, accession2);
    }

}