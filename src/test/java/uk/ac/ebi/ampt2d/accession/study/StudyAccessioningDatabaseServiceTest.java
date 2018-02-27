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
package uk.ac.ebi.ampt2d.accession.study;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.ampt2d.accession.common.generators.ModelHashAccession;
import uk.ac.ebi.ampt2d.accession.study.persistence.StudyAccessioningDatabaseService;
import uk.ac.ebi.ampt2d.accession.study.rest.StudyDTO;
import uk.ac.ebi.ampt2d.test.configurationaccession.StudyAccessioningDatabaseServiceTestConfiguration;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.ampt2d.test.utils.TestHelper.generateStudyMap;
import static uk.ac.ebi.ampt2d.test.utils.TestHelper.generateStudyModelHashAccessions;

@RunWith(SpringJUnit4ClassRunner.class)
@DataJpaTest
@TestPropertySource(properties = "services=study-accession")
@Import(StudyAccessioningDatabaseServiceTestConfiguration.class)
public class StudyAccessioningDatabaseServiceTest {

    @Autowired
    private StudyAccessioningDatabaseService studyDatabaseService;

    @Test
    public void testStudiesAreStoredInTheRepositoryAndRetrived() throws Exception {
        List<ModelHashAccession<StudyModel, String, String>> studies = generateStudyModelHashAccessions(1, 2);
        studyDatabaseService.save(studies);

        Collection<String> hashes = studies.stream().map(study -> study.hash()).collect(Collectors.toList());
        Map<String, String> accessionsFromRepository = studyDatabaseService.getExistingAccessions(hashes);
        assertEquals(2, accessionsFromRepository.size());
    }

    //JpaSystemException is due to the id of entity being null
    @Test(expected = org.springframework.orm.jpa.JpaSystemException.class)
    public void cantStoreObjectsWithoutAccession() {
        studyDatabaseService.save(Arrays.asList(ModelHashAccession.of(new StudyDTO(generateStudyMap(1)),
                "hash1", null)));
    }
}
