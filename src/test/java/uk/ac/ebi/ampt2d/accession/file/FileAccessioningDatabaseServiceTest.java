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
package uk.ac.ebi.ampt2d.accession.file;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.accession.AccessionGenerator;
import uk.ac.ebi.ampt2d.accession.BasicAccessionGenerator;
import uk.ac.ebi.ampt2d.accession.DatabaseService;
import uk.ac.ebi.ampt2d.accession.WebConfiguration;
import uk.ac.ebi.ampt2d.test.configurationaccession.FileAccessioningServiceTestConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(properties = "services=file-accession")
@Import(FileAccessioningServiceTestConfiguration.class)
public class FileAccessioningDatabaseServiceTest {

    @Autowired
    private DatabaseService fileDatabaseService;

    private AccessionGenerator<FileMessage, String> generator;

    private AccessionGenerator<FileMessage, String> alternativeGenerator;

    private Map<FileMessage, String> fileMessageAccessionMap;

    @Before
    public void setUp() throws Exception {
        generator = new BasicAccessionGenerator<>();
        alternativeGenerator = new BasicAccessionGenerator<>();
    }

    @Test
    public void testFilesAreStoredInTheRepositoryAndRetrived() throws Exception {
        List<FileMessage> files = Arrays.asList(new FileMessage("checksumA"), new FileMessage("checksumB"));
        Map<FileMessage, String> accessionedFiles = generator.generateAccessions(new HashSet(files));
        fileDatabaseService.save(accessionedFiles);

        Collection<String> checksums = new ArrayList<>();
        checksums.add("checksumA");
        boolean checksumB = checksums.add("checksumB");
        Map<FileMessage, String> accessionsFromRepository = fileDatabaseService.findObjectsInDB(files);
        assertEquals(accessionedFiles.keySet(), accessionsFromRepository.keySet());
    }

    @Test
    public void addingTheSameFilesWithSameAccessionsTwiceOverwritesObject() throws Exception {
        List<FileMessage> files = Arrays.asList(new FileMessage("checksumA"), new FileMessage("checksumB"));
        HashSet<FileMessage> fileSet = new HashSet(files);

        // Store the files with the initial accessions
        Map<FileMessage, String> accessionedFiles = generator.generateAccessions(fileSet);
        fileDatabaseService.save(accessionedFiles);

        // Storing again the same files with new accessions overwrites the existing ones
        Map<FileMessage, String> alternativeAccesionedFiles = alternativeGenerator.generateAccessions(fileSet);
        fileDatabaseService.save(alternativeAccesionedFiles);
        Map<FileMessage, String> accessionsFromRepository = fileDatabaseService.findObjectsInDB(files);
        assertEquals(accessionedFiles.keySet(), accessionsFromRepository.keySet());
    }

    //JpaSystemException is due to the id of entity being null
    @Test(expected = org.springframework.orm.jpa.JpaSystemException.class)
    public void cantStoreFileWithoutAccession() {
        fileMessageAccessionMap = new HashMap<>();
        fileMessageAccessionMap.put(new FileMessage("cantStoreFileWithoutAccession"), null);
        fileDatabaseService.save(fileMessageAccessionMap);
    }
}