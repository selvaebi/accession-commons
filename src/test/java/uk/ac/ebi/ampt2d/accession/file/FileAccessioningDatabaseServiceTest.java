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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.accession.AccessionGenerator;
import uk.ac.ebi.ampt2d.accession.DatabaseService;
import uk.ac.ebi.ampt2d.accession.sha1.SHA1AccessionGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(properties = {"services=file-accession", "accessionBy=sha1"})
@ComponentScan(basePackages = {"uk.ac.ebi.ampt2d.accession.file", "uk.ac.ebi.ampt2d.accession.sha1"})
public class FileAccessioningDatabaseServiceTest {

    @Autowired
    private DatabaseService fileDatabaseService;

    private AccessionGenerator<FileMessage, String> generator;

    private AccessionGenerator<FileMessage, String> alternativeGenerator;

    @Before
    public void setUp() throws Exception {
        generator = new SHA1AccessionGenerator<>();
        alternativeGenerator = new SHA1AccessionGenerator<>();

    }

    @Test
    public void testFilesAreStoredInTheRepository() throws Exception {
        List<FileMessage> files = Arrays.asList(new FileMessage("checksumA"), new FileMessage("checksumB"));
        Map<FileMessage, String> accessionedFiles = generator.generateAccessions(new HashSet(files));
        for (Map.Entry<FileMessage, String> entry : accessionedFiles.entrySet()) {
            entry.getKey().setAccession(entry.getValue());
        }
        fileDatabaseService.save(accessionedFiles.keySet());

        assertEquals(2, fileDatabaseService.count());

        Collection<String> checksums = new ArrayList<>();
        checksums.add("checksumA");
        checksums.add("checksumB");
        Collection<FileMessage> accessionsFromRepository = fileDatabaseService.findObjectsInDB(files);
        assertEquals(accessionedFiles.keySet(), accessionsFromRepository);
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void addingTheSameFilesWithSameAccessionsTwiceGivesConstraintViolationException() throws Exception {
        List<FileMessage> files = Arrays.asList(new FileMessage("checksumA"), new FileMessage("checksumB"));
        HashSet<FileMessage> fileSet = new HashSet(files);

        // Store the files with the initial accessions
        Map<FileMessage, String> accessionedFiles = generator.generateAccessions(fileSet);
        for (Map.Entry<FileMessage, String> entry : accessionedFiles.entrySet()) {
            entry.getKey().setAccession(entry.getValue());
        }
        fileDatabaseService.save(accessionedFiles.keySet());
        assertEquals(2, fileDatabaseService.count());

        // Storing again the same files with new accessions overwrites the existing ones
        Map<FileMessage, String> alternativeAccesionedFiles = alternativeGenerator.generateAccessions(fileSet);
        for (Map.Entry<FileMessage, String> entry : alternativeAccesionedFiles.entrySet()) {
            entry.getKey().setAccession(entry.getValue());
        }
        fileDatabaseService.save(alternativeAccesionedFiles.keySet());
        assertEquals(2, fileDatabaseService.count());
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void cantStoreFileWithoutAccession() {
        FileMessage file = new FileMessage("cantStoreFileWithoutAccession");
        fileDatabaseService.save(file);
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void cantStoreMultipleFilesWithSameHash() {
        FileMessage originalFile = new FileMessage("cantStoreMultipleFilesWithSameHash");
        originalFile.setAccession("randomString");
        fileDatabaseService.save(originalFile);
        assertEquals(1, fileDatabaseService.count());

        FileMessage newFile = new FileMessage(originalFile.getHash());
        newFile.setAccession("anotherRandomString");
        fileDatabaseService.save(newFile);
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void cantStoreMultipleFilesWithSameAccession() {
        FileMessage originalFile = new FileMessage("cantStoreMultipleFilesWithSameAccession1");
        originalFile.setAccession("randomString");
        fileDatabaseService.save(originalFile);
        assertEquals(1, fileDatabaseService.count());

        FileMessage newFile = new FileMessage("cantStoreMultipleFilesWithSameAccession2");
        newFile.setAccession(originalFile.getAccession());
        fileDatabaseService.save(newFile);
    }
}