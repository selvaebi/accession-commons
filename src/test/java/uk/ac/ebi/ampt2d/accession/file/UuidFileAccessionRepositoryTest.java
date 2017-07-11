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

import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
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

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = FileTestConfiguration.class)
@DataJpaTest
public class UuidFileAccessionRepositoryTest {

    @Autowired
    private UuidFileAccessionRepository accessionRepository;

    private Collection<UuidFile> files;

    private AccessionGenerator<UuidFile, UUID> generator;

    private AccessionGenerator<UuidFile, UUID> alternativeGenerator;

    @Before
    public void setUp() throws Exception {
        UuidFile fileA = new UuidFile("checksumA");
        UuidFile fileB = new UuidFile("checksumB");
        files = Arrays.asList(fileA, fileB);

        generator = new UuidAccessionGenerator<>("ACC");
        alternativeGenerator = new UuidAccessionGenerator<>("ALT");
    }

    @After
    public void tearDown() throws Exception {
        accessionRepository.deleteAll();
    }

    @Test
    public void addShouldStoreTheFilesInTheRepository() throws Exception {
        Map<UuidFile, UUID> accessionedFiles = generator.get(new HashSet<>(files));
        accessionRepository.save(accessionedFiles.keySet());

        assertEquals(2, accessionRepository.count());

        Collection<String> checksums = new ArrayList<>();
        checksums.add("checksumA");
        checksums.add("checksumB");
        Collection<UuidFile> accessionsFromRepository = accessionRepository.findByHashIn(checksums);
        assertEquals(accessionedFiles.keySet(), accessionsFromRepository);
    }
//
//    @Test
//    public void addingTheSameFilesWillReplaceTheAccessionsInTheRepository() throws Exception {
//        HashSet<UuidFile> fileSet = new HashSet<>(files);
//
//        // Store the files with the initial accessions
//        Map<UuidFile, UUID> accessionedFiles = generator.get(fileSet);
//        accessionRepository.save(accessionedFiles.keySet());
//        assertEquals(2, accessionRepository.count());
//
//        // Trying to store again the files with alternative accessions should fail due to the unique constraint
//        Map<UuidFile, UUID> alternativeAccesionedFiles = alternativeGenerator.get(fileSet);
//        accessionRepository.save(alternativeAccesionedFiles.keySet());
//        assertEquals(2, accessionRepository.count());
//    }
}