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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.accession.AccessioningService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DataJpaTest
@ComponentScan(basePackages = {"uk.ac.ebi.ampt2d.accession.file", "uk.ac.ebi.ampt2d.accession.sha1"})
@TestPropertySource(properties = {"services=file-accession", "accessionBy=sha1"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class FileSha1AccessioningServiceTest {

    @Autowired
    private AccessioningService accessioningService;

    @Test
    public void sameAccessionsAreReturnedForIdenticalFiles() throws Exception {
        String checksumA = "checksumA";
        String checksumB = "checksumB";
        File fileA = new File(checksumA);
        File fileB = new File(checksumB);

        Map<File, String> generatedAccessions = accessioningService.getAccessions(Arrays.asList(fileA, fileB));

        fileA = new File(checksumA);
        fileB = new File(checksumB);

        Map<File, String> retrievedAccessions = accessioningService.getAccessions(Arrays.asList(fileA, fileB));

        assertEquals(generatedAccessions.get(fileA), retrievedAccessions.get(fileA));
        assertEquals(generatedAccessions.get(fileB), retrievedAccessions.get(fileB));
    }

    @Test
    public void everyNewObjectReceiveOneAccession() throws Exception {
        List<File> newObjects = Arrays.asList(new File("checksumA"), new File("checksumB"), new File("checksumC"));
        Map<File, String> accessions = accessioningService.getAccessions(newObjects);

        for (File object : newObjects) {
            assertNotNull(accessions.get(object));
        }
    }

    @Test
    public void sameObjectsGetSameAccession() throws Exception {

        File fileA = new File("checksumA");
        File fileB = new File("checksumA");

        List<File> newObjects = Arrays.asList(fileA, fileB);
        Map<File, String> accessions = accessioningService.getAccessions(newObjects);

        String accession1 = accessions.get(fileA);
        String anotherAccession1 = accessions.get(fileB);

        assertEquals(accession1, anotherAccession1);
    }

    @Test
    public void differentObjectsGetDifferentAccessions() throws Exception {
        File fileA = new File("checksumA");
        File fileB = new File("checksumB");

        List<File> newObjects = Arrays.asList(fileA, fileB);
        Map<File, String> accessions = accessioningService.getAccessions(newObjects);

        String accession1 = accessions.get(fileA);
        String anotherAccession1 = accessions.get(fileB);

        assertNotEquals(accession1, anotherAccession1);
    }

    @Test
    public void differentCallsToTheServiceUsingSameObjectsWillReturnSameAccessions() throws Exception {
        File fileA = new File("checksumA");
        File fileB = new File("checksumB");

        List<File> newObjects = Arrays.asList(fileA, fileB);
        Map<File, String> accessions = accessioningService.getAccessions(newObjects);

        String accession1 = accessions.get(fileA);
        String anotherAccession1 = accessions.get(fileB);

        Map<File, String> accessionsFromSecondServiceCall = accessioningService.getAccessions(Arrays.asList(fileA, fileB));

        assertEquals(accession1, accessionsFromSecondServiceCall.get(fileA));
        assertEquals(anotherAccession1, accessionsFromSecondServiceCall.get(fileB));
    }

    @Test
    public void mixingAlreadyAccessionedAndNewObjectsIsAllowed() throws Exception {
        File fileA = new File("checksumA");
        File fileB = new File("checksumB");

        Map<File, String> accessions = accessioningService.getAccessions(Arrays.asList(fileA, fileB));

        String accession1 = accessions.get(fileA);
        String accession2 = accessions.get(fileB);

        File fileC = new File("checksumC");
        File fileD = new File("checksumD");

        List<File> objectsToAccession = Arrays.asList(fileA, fileB, fileC, fileD);
        Map<File, String> accessionsFromSecondServiceCall = accessioningService
                .getAccessions(objectsToAccession);

        assertEquals(accession1, accessionsFromSecondServiceCall.get(fileA));
        assertEquals(accession2, accessionsFromSecondServiceCall.get(fileB));
        assertNotNull(accessionsFromSecondServiceCall.get(fileC));
        assertNotNull(accessionsFromSecondServiceCall.get(fileD));

        assertEquals(objectsToAccession.size(),
                new HashSet<>(accessionsFromSecondServiceCall.values()).size());
    }

}