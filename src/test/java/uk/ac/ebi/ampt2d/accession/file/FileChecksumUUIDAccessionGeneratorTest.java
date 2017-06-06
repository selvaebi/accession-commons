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
package uk.ac.ebi.ampt2d.accession.file;

import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.ebi.ampt2d.accession.file.FileChecksumUUIDAccessionGenerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;

public class FileChecksumUUIDAccessionGeneratorTest {

    private static Random randomGenerator;

    @BeforeClass
    public static void setUpClass() throws Exception {
        randomGenerator = new Random();
    }

    @Test
    public void differentAccessionsAreGeneratedForDifferentInputs() throws Exception {
        String checksum1 = randomChecksum();
        String checksum2 = randomChecksum();

        FileChecksumUUIDAccessionGenerator generator = new FileChecksumUUIDAccessionGenerator();

        Map<String, String> accessions = generator.get(new HashSet<>(Arrays.asList(checksum1, checksum2)));

        String accession1 = accessions.get(checksum1);
        String accession2 = accessions.get(checksum2);

        assertTrue(accession1 != null);
        assertTrue(accession2 != null);

        assertNotEquals(accession1, accession2);
    }

    @Test
    public void oneGeneratorReturnsTheSameAccessionInDifferentCallsWithTheSameInput() {
        String checksum = randomChecksum();

        FileChecksumUUIDAccessionGenerator generator = new FileChecksumUUIDAccessionGenerator();

        Map<String, String> accessions = generator.get(Collections.singleton(checksum));
        String accession1 = accessions.get(checksum);

        accessions = generator.get(Collections.singleton(checksum));
        String accession2 = accessions.get(checksum);

        assertEquals(accession1, accession2);
    }


    @Test
    public void twoDifferentGeneratorInstancesReturnTheSameAccessionForTheSameInput() {
        String checksum = randomChecksum();

        FileChecksumUUIDAccessionGenerator generator = new FileChecksumUUIDAccessionGenerator();

        Map<String, String> accessions = generator.get(Collections.singleton(checksum));
        String accession1 = accessions.get(checksum);

        FileChecksumUUIDAccessionGenerator generator2 = new FileChecksumUUIDAccessionGenerator();
        accessions = generator2.get(Collections.singleton(checksum));
        String accession2 = accessions.get(checksum);

        assertEquals(accession1, accession2);
    }

    private String randomChecksum() {
        String hexDigits = "0123456789abcdef";

        char[] text = new char[32];
        for(int i=0; i < text.length; i++) {
            text[i] = hexDigits.charAt(randomGenerator.nextInt(hexDigits.length()));
        }

        return new String(text);
    }
}