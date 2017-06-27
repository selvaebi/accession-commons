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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.accession.AccessionGenerator;
import uk.ac.ebi.ampt2d.accession.UuidAccessionGenerator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = FileTestConfiguration.class)
@DataJpaTest
public class FileAccessionRepositoryTest {

    @Autowired
    FileAccessionRepository accessionRepository;

    List<File> files;

    private AccessionGenerator<File, UUID> generator;

    private AccessionGenerator<File, UUID> alternativeGenerator;

    @Before
    public void setUp() throws Exception {
        File fileA = new File("checksumA");
        File fileB = new File("checksumB");
        files = Arrays.asList(fileA, fileB);

        generator = new UuidAccessionGenerator<>("ACC");
        alternativeGenerator = new UuidAccessionGenerator<>("ALT");
    }

    @After
    public void tearDown() throws Exception {
        accessionRepository.getFileJpaRepository().deleteAll();
    }

    @Test
    public void repositoryIsInitiallyEmpty() throws Exception {
        Map<File, UUID> accessions = accessionRepository.get(files);
        assertEquals(0, accessions.size());
    }

    @Test
    public void addShouldStoreTheFilesInTheRepository() throws Exception {
        Map<File, UUID> accessionedFiles = generator.get(new HashSet<>(files));
        accessionRepository.add(accessionedFiles);

        assertEquals(2, accessionRepository.getFileJpaRepository().count());

        Map<File, UUID> accessionsFromRepository = accessionRepository.get(files);

        assertEquals(accessionedFiles, accessionsFromRepository);
    }

    @Test
    public void addingTheSameFilesWillReplaceTheAccessionsInTheRepository() throws Exception {
        HashSet<File> fileSet = new HashSet<>(files);

        // store the files with the initial accessions
        Map<File, UUID> accessionedFiles = generator.get(fileSet);
        accessionRepository.add(accessionedFiles);
        assertEquals(2, accessionRepository.getFileJpaRepository().count());

        // store again the files with alternative accessions
        Map<File, UUID> alternativeAccesionedFiles = alternativeGenerator.get(fileSet);
        accessionRepository.add(alternativeAccesionedFiles);
        assertEquals(2, accessionRepository.getFileJpaRepository().count());

        // the stored accessions should be the alternative ones
        Map<File, UUID> accessionsFromRepository = accessionRepository.get(files);
        assertNotEquals(accessionedFiles, accessionsFromRepository);
        assertEquals(alternativeAccesionedFiles, accessionsFromRepository);
    }
}