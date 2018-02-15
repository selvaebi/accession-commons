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
package uk.ac.ebi.ampt2d.accession.sample;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.ampt2d.accession.common.generators.ModelHashAccession;
import uk.ac.ebi.ampt2d.accession.sample.persistence.SampleAccessioningDatabaseService;
import uk.ac.ebi.ampt2d.test.configurationaccession.SampleAccessioningDatabaseServiceTestConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.ampt2d.test.utils.TestHelper.generateSampleDTO;
import static uk.ac.ebi.ampt2d.test.utils.TestHelper.generateSampleModelHashAccessions;

@RunWith(SpringJUnit4ClassRunner.class)
@DataJpaTest
@TestPropertySource(properties = "services=sample-accession")
@Import(SampleAccessioningDatabaseServiceTestConfiguration.class)
public class SampleAccessioningDatabaseServiceTest {

    private static final String HASH_1 = "hash1";
    private static final String HASH_2 = "hash2";
    private static final String ACCESSION_2 = "accession2";

    @Autowired
    private SampleAccessioningDatabaseService databaseService;

    @Test
    public void testSamplesAreStoredInTheRepositoryAndRetrived() throws Exception {
        List<ModelHashAccession<SampleModel, String, String>> samples = generateSampleModelHashAccessions(1, 2);
        List<String> hashes = samples.stream().map(ModelHashAccession::hash).collect(Collectors.toList());

        databaseService.save(samples);

        Map<String, ? extends SampleModel> accessionsFromRepository =
                databaseService.findAllAccessionByMessageHash(hashes);
        assertEquals(2, accessionsFromRepository.size());
    }

    //JpaSystemException is due to the id of entity being null
    @Test(expected = org.springframework.orm.jpa.JpaSystemException.class)
    public void cantStoreObjectsWithoutAccession() {
        List<ModelHashAccession<SampleModel, String, String>> sampleAccessionsToStore = Arrays.asList(
                ModelHashAccession.of((SampleModel) generateSampleDTO(1), HASH_1, null),
                ModelHashAccession.of((SampleModel) generateSampleDTO(2), HASH_2, ACCESSION_2)
        );
        databaseService.save(sampleAccessionsToStore);
    }
}
