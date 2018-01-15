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
package uk.ac.ebi.ampt2d.accession.study;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.ampt2d.accession.AccessionGenerator;
import uk.ac.ebi.ampt2d.accession.sha1.SHA1AccessionGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@DataJpaTest
@TestPropertySource(properties = {"services=study-accession", "accessionBy=sha1"})
public class StudyAccessioningRepositoryTest {

    @Autowired
    private StudyAccessioningRepository StudyRepository;

    private AccessionGenerator<Study, String> generator;

    private AccessionGenerator<Study, String> alternativeGenerator;

    private Map<String, String> studyMap1;
    private Map<String, String> studyMap2;
    private Study accessionObject1;
    private Study accessionObject2;

    @Before
    public void setUp() throws Exception {
        generator = new SHA1AccessionGenerator();
        alternativeGenerator = new SHA1AccessionGenerator();

        studyMap1 = new HashMap<>();
        studyMap1.put("title", "Title1");
        studyMap1.put("type", "Type1");
        studyMap1.put("submitterEmail", "Email1");
        studyMap2 = new HashMap<>();
        studyMap2.put("title", "Title2");
        studyMap2.put("type", "Type2");
        studyMap2.put("submitterEmail", "Email2");
        accessionObject1 = new Study(studyMap1);
        accessionObject2 = new Study(studyMap2);

    }

    @Test
    public void testStudiesAreStoredInTheRepository() throws Exception {

        Map<Study, String> accessionedStudies = generator.generateAccessions(new HashSet<>(Arrays.asList(accessionObject1, accessionObject2)));

        for (Map.Entry<Study, String> entry : accessionedStudies.entrySet()) {
            entry.getKey().setAccession(entry.getValue());
        }
        StudyRepository.save(accessionedStudies.keySet());

        assertEquals(2, StudyRepository.count());

        Collection<String> hashes = new ArrayList<>();
        hashes.add(accessionObject1.getHash());
        hashes.add(accessionObject2.getHash());
        Collection<Study> accessionsFromRepository = StudyRepository.findByHashIn(hashes);
        assertEquals(accessionsFromRepository.stream().sorted((e1, e2) -> e1.getHash().compareTo(e2.getHash())).collect(Collectors.toList()),
                new ArrayList<>(accessionedStudies.keySet()).stream().sorted((e1, e2) -> e1.getHash().compareTo(e2.getHash())).collect(Collectors.toList()));
    }

    @Test
    public void addingTheSameStudiesWithDifferentAccessionsOverwritesInTheRepository() throws Exception {

        HashSet<Study> studies = new HashSet<>(Arrays.asList(accessionObject1, accessionObject2));

        // Store the studies with the initial accessions
        Map<Study, String> accessionedStudies = generator.generateAccessions(studies);
        for (Map.Entry<Study, String> entry : accessionedStudies.entrySet()) {
            entry.getKey().setAccession(entry.getValue());
        }
        StudyRepository.save(accessionedStudies.keySet());
        assertEquals(2, StudyRepository.count());

        // Storing again the same studies with new accessions overwrites the existing ones
        Map<Study, String> alternativeAccesionedStudies = alternativeGenerator.generateAccessions(studies);
        for (Map.Entry<Study, String> entry : alternativeAccesionedStudies.entrySet()) {
            entry.getKey().setAccession(entry.getValue());
        }
        StudyRepository.save(alternativeAccesionedStudies.keySet());
        assertEquals(2, StudyRepository.count());
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void cantStoreOnjectsWithoutAccession() {
        StudyRepository.save(accessionObject1);
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void cantStoreMultipleStudiesWithSameAccession() {

        accessionObject1.setAccession("randomString");
        StudyRepository.save(accessionObject1);
        assertEquals(1, StudyRepository.count());

        accessionObject2.setAccession(accessionObject1.getAccession());
        StudyRepository.save(accessionObject2);
    }
}
