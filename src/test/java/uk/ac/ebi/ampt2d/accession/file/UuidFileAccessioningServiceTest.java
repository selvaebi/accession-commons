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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = FileTestConfiguration.class)
@DataJpaTest
public class UuidFileAccessioningServiceTest {

    @Autowired
    private UuidFileAccessionRepository uuidFileAccessionRepository;

    private UuidFileAccessioningService service;

    @Before
    public void setUp() throws Exception {
        service = new UuidFileAccessioningService( "UuidFileAccessioningServiceTest");
    }

    @Test
    public void sameAccessionsAreReturnedForIdenticalFiles() throws Exception {
        String checksumA = "checksumA";
        String checksumB = "checksumB";
        UuidFile fileA = new UuidFile(checksumA);
        UuidFile fileB = new UuidFile(checksumB);

        Map<UuidFile, UUID> generatedAccessions = service.getAccessions(Arrays.asList(fileA, fileB));

        fileA = new UuidFile(checksumA);
        fileB = new UuidFile(checksumB);

        Map<UuidFile, UUID> retrievedAccessions = service.getAccessions(Arrays.asList(fileA, fileB));

        assertEquals(generatedAccessions.get(fileA), retrievedAccessions.get(fileA));
        assertEquals(generatedAccessions.get(fileB), retrievedAccessions.get(fileB));
    }

}