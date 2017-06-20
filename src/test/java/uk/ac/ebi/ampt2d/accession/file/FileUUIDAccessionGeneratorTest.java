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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class FileUUIDAccessionGeneratorTest {

    public static final String TEST_NAMESPACE = "Test";

    private static Random randomGenerator;

    @BeforeClass
    public static void setUpClass() throws Exception {
        randomGenerator = new Random();
    }

    @Test
    public void differentAccessionsAreGeneratedForDifferentInputs() throws Exception {
        File file1 = new File();
        File file2 = new File();
        
        file1.setHash(randomChecksum());
        file2.setHash(randomChecksum());

        FileUUIDAccessionGenerator generator = new FileUUIDAccessionGenerator(TEST_NAMESPACE);

        Map<File, UUID> accessions = generator.get(new HashSet<>(Arrays.asList(file1, file2)));

        UUID accession1 = accessions.get(file1);
        UUID accession2 = accessions.get(file2);

        assertTrue(accession1 != null);
        assertTrue(accession2 != null);

        assertNotEquals(accession1, accession2);
    }

    @Test
    public void oneGeneratorReturnsTheSameAccessionInDifferentCallsWithTheSameInput() {
        File file = new File();
        file.setHash(randomChecksum());

        FileUUIDAccessionGenerator generator = new FileUUIDAccessionGenerator(TEST_NAMESPACE);

        Map<File, UUID> accessions = generator.get(Collections.singleton(file));
        UUID accession1 = accessions.get(file);

        accessions = generator.get(Collections.singleton(file));
        UUID accession2 = accessions.get(file);

        assertEquals(accession1, accession2);
    }

    @Test
    public void twoDifferentGeneratorInstancesForSameNamespaceReturnTheSameAccessionForTheSameInput() {
        File file = new File();
        file.setHash(randomChecksum());

        FileUUIDAccessionGenerator generator = new FileUUIDAccessionGenerator(TEST_NAMESPACE);

        Map<File, UUID> accessions = generator.get(Collections.singleton(file));
        UUID accession1 = accessions.get(file);

        FileUUIDAccessionGenerator generator2 = new FileUUIDAccessionGenerator(TEST_NAMESPACE);
        accessions = generator2.get(Collections.singleton(file));
        UUID accession2 = accessions.get(file);

        assertEquals(accession1, accession2);
    }

    @Test
    public void twoDifferentGeneratorInstancesForDiferentNamespacesReturnDifferentAccessionsForTheSameInput() {
        File file = new File();
        file.setHash(randomChecksum());

        FileUUIDAccessionGenerator generator = new FileUUIDAccessionGenerator("MD5");

        Map<File, UUID> accessions = generator.get(Collections.singleton(file));
        UUID accession1 = accessions.get(file);

        FileUUIDAccessionGenerator generator2 = new FileUUIDAccessionGenerator("SHA1");
        accessions = generator2.get(Collections.singleton(file));
        UUID accession2 = accessions.get(file);

        assertNotEquals(accession1, accession2);
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