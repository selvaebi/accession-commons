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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.accession.AccessionGenerator;
import uk.ac.ebi.ampt2d.accession.SHA1AccessionGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(properties = "services=file-accession")
public class FileAccessioningRepositoryTest {

    @Autowired
    private FileAccessioningRepository accessionRepository;

    private AccessionGenerator<File, String> generator;

    private AccessionGenerator<File, String> alternativeGenerator;

    @Before
    public void setUp() throws Exception {
        generator = new SHA1AccessionGenerator<>("ACC");
        alternativeGenerator = new SHA1AccessionGenerator<>("ALT");

    }

    @Test
    public void testFilesAreStoredInTheRepository() throws Exception {
        List<File> files = Arrays.asList(new File("checksumA"), new File("checksumB"));
        Map<File, String> accessionedFiles = generator.generateAccessions(new HashSet<>(files));
        for (Map.Entry<File, String> entry : accessionedFiles.entrySet()) {
            entry.getKey().setAccession(entry.getValue());
        }
        accessionRepository.save(accessionedFiles.keySet());

        assertEquals(2, accessionRepository.count());

        Collection<String> checksums = new ArrayList<>();
        checksums.add("checksumA");
        checksums.add("checksumB");
        Collection<File> accessionsFromRepository = accessionRepository.findByHashIn(checksums);
        assertEquals(new ArrayList<>(accessionedFiles.keySet()), accessionsFromRepository);
    }

    @Test
    public void addingTheSameFilesWillReplaceTheAccessionsInTheRepository() throws Exception {
        List<File> files = Arrays.asList(new File("checksumA"), new File("checksumB"));
        HashSet<File> fileSet = new HashSet<>(files);

        // Store the files with the initial accessions
        Map<File, String> accessionedFiles = generator.generateAccessions(fileSet);
        for (Map.Entry<File, String> entry : accessionedFiles.entrySet()) {
            entry.getKey().setAccession(entry.getValue());
        }
        accessionRepository.save(accessionedFiles.keySet());
        assertEquals(2, accessionRepository.count());

        // Storing again the same files with new accessions overwrites the existing ones
        Map<File, String> alternativeAccesionedFiles = alternativeGenerator.generateAccessions(fileSet);
        for (Map.Entry<File, String> entry : alternativeAccesionedFiles.entrySet()) {
            entry.getKey().setAccession(entry.getValue());
        }
        accessionRepository.save(alternativeAccesionedFiles.keySet());
        assertEquals(2, accessionRepository.count());
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void cantStoreFileWithoutAccession() {
        File file = new File("cantStoreFileWithoutAccession");
        accessionRepository.save(file);
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void cantStoreMultipleFilesWithSameHash() {
        File originalFile = new File("cantStoreMultipleFilesWithSameHash");
        originalFile.setAccession("randomString");
        accessionRepository.save(originalFile);
        assertEquals(1, accessionRepository.count());

        File newFile = new File(originalFile.getHash());
        newFile.setAccession("anotherRandomString");
        accessionRepository.save(newFile);
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void cantStoreMultipleFilesWithSameAccession() {
        File originalFile = new File("cantStoreMultipleFilesWithSameAccession1");
        originalFile.setAccession("randomString");
        accessionRepository.save(originalFile);
        assertEquals(1, accessionRepository.count());

        File newFile = new File("cantStoreMultipleFilesWithSameAccession2");
        newFile.setAccession(originalFile.getAccession());
        accessionRepository.save(newFile);
    }
}