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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.accession.file.rest.FileDTO;
import uk.ac.ebi.ampt2d.test.configurationaccession.FileAccessioningServiceTestConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(FileAccessioningServiceTestConfiguration.class)
@TestPropertySource(properties = "services=file-accession")
public class FileBasicAccessioningServiceTest {

    @Autowired
    private FileAccessioningService accessioningService;

    @Test
    public void sameAccessionsAreReturnedForIdenticalFiles() throws Exception {
        String checksumA = "checksumA";
        String checksumB = "checksumB";
        FileModel fileA = new FileDTO(checksumA);
        FileModel fileB = new FileDTO(checksumB);

        Map<String, FileModel> generatedAccessions = accessioningService.getOrCreateAccessions(Arrays.asList(fileA, fileB));

        fileA = new FileDTO(checksumA);
        fileB = new FileDTO(checksumB);

        Map<String, FileModel> retrievedAccessions = accessioningService.getOrCreateAccessions(Arrays.asList(fileA, fileB));

        assertEquals(generatedAccessions.keySet(), retrievedAccessions.keySet());
    }

    @Test
    public void everyNewObjectReceiveOneAccession() throws Exception {
        List<FileDTO> newObjects = Arrays.asList(new FileDTO("checksumA"),
                new FileDTO("checksumB"), new FileDTO("checksumC"));
        Map<String, FileModel> accessions = accessioningService.getOrCreateAccessions(newObjects);

        assertFalse(accessions.containsKey(null));
    }

    @Test
    public void sameObjectsGetSameAccession() throws Exception {
        FileDTO fileA = new FileDTO("checksumA");
        FileDTO fileB = new FileDTO("checksumA");

        List<FileDTO> newObjects = Arrays.asList(fileA, fileB);
        Map<String, FileModel> accessions = accessioningService.getOrCreateAccessions(newObjects);

        assertEquals(1, accessions.size());
    }

    @Test
    public void differentObjectsGetDifferentAccessions() throws Exception {
        FileDTO fileA = new FileDTO("checksumA");
        FileDTO fileB = new FileDTO("checksumB");
        List<FileDTO> newObjects = Arrays.asList(fileA, fileB);
        Map<String, FileModel> accessions = accessioningService.getOrCreateAccessions(newObjects);

        assertEquals(2, accessions.size());
    }

    @Test
    public void mixingAlreadyAccessionedAndNewObjectsIsAllowed() throws Exception {
        FileDTO fileA = new FileDTO("checksumA");
        FileDTO fileB = new FileDTO("checksumB");
        Map<String, FileModel> accessionsFromFirstServiceCall = accessioningService.getOrCreateAccessions(Arrays.asList(fileA, fileB));
        FileDTO fileC = new FileDTO("checksumC");
        FileDTO fileD = new FileDTO("checksumD");
        List<FileDTO> objectsToAccession = Arrays.asList(fileA, fileB, fileC, fileD);
        Map<String, FileModel> accessionsFromSecondServiceCall = accessioningService
                .getOrCreateAccessions(objectsToAccession);

        assertEquals(2, accessionsFromFirstServiceCall.size());
        assertEquals(4, accessionsFromSecondServiceCall.size());
        accessionsFromFirstServiceCall.keySet().stream()
                .forEach(s -> assertTrue(accessionsFromSecondServiceCall.containsKey(s)));
    }
}