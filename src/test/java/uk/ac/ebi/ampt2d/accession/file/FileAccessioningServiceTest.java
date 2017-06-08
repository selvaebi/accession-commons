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
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FileAccessioningServiceTest {

    @Autowired
    private FileAccessionRepository fileAccessionRepository;

    private FileAccessioningService service;

    @Before
    public void setUp() throws Exception {
        service = new FileAccessioningService(fileAccessionRepository, "FileAccessioningServiceTest");
    }

    @After
    public void tearDown() throws Exception {
        fileAccessionRepository.getFileJpaRepository().deleteAll();
    }

    @Test
    public void sameAccessionsAreReturnedForIdenticalFiles() throws Exception {
        String checksumA = "checksumA";
        String checksumB = "checksumB";
        File fileA = new File();
        File fileB = new File();
        fileA.setChecksum(checksumA);
        fileB.setChecksum(checksumB);

        Map<File, String> generatedAccessions = service.getAccessions(Arrays.asList(fileA, fileB));

        fileA = new File();
        fileB = new File();
        fileA.setChecksum(checksumA);
        fileB.setChecksum(checksumB);

        Map<File, String> retrievedAccessions = service.getAccessions(Arrays.asList(fileA, fileB));

        assertEquals(generatedAccessions.get(fileA), retrievedAccessions.get(fileA));
        assertEquals(generatedAccessions.get(fileB), retrievedAccessions.get(fileB));
    }

}