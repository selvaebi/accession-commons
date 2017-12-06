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
import uk.ac.ebi.ampt2d.accession.UuidAccessionGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@DataJpaTest
@TestPropertySource(properties = "services=study-uuid")
public class StudyAccessioningRepositoryTest {

    @Autowired
    private StudyAccessioningRepository accessionRepository;

    private AccessionGenerator<Study, UUID> generator;

    private AccessionGenerator<Study, UUID> alternativeGenerator;

    @Before
    public void setUp() throws Exception {
        generator = new UuidAccessionGenerator<>("ACC");
        alternativeGenerator = new UuidAccessionGenerator<>("ALT");

    }

    @Test
    public void testStudiesAreStoredInTheRepository() throws Exception {

        Study study1 = new Study("Title1", "Type1", "Email1");
        Study study2 = new Study("Title2", "Type2", "Email2");
        List<Study> studies = Arrays.asList(study1, study2);
        Map<Study, UUID> accessionedStudies = generator.generateAccessions(new HashSet<>(studies));
        for (Map.Entry<Study, UUID> entry : accessionedStudies.entrySet()) {
            entry.getKey().setAccession(entry.getValue());
        }
        accessionRepository.save(accessionedStudies.keySet());

        assertEquals(2, accessionRepository.count());

        Collection<String> checksums = new ArrayList<>();
        checksums.add(study1.getHash());
        checksums.add(study2.getHash());
        Collection<Study> accessionsFromRepository = accessionRepository.findByHashIn(checksums);
        assertEquals(accessionsFromRepository.stream().sorted((e1, e2) -> e1.getHash().compareTo(e2.getHash())).collect(Collectors.toList()),
                new ArrayList<>(accessionedStudies.keySet()).stream().sorted((e1, e2) -> e1.getHash().compareTo(e2.getHash())).collect(Collectors.toList()));
    }

    @Test
    public void addingTheSameStudiesWithDifferentAccessionsOverwritesInTheRepository() throws Exception {
        List<Study> studies = Arrays.asList(new Study("Title1", "Type1", "Email1"),
                new Study("Title2", "Type2", "Email2"));
        HashSet<Study> fileSet = new HashSet<>(studies);

        // Store the studies with the initial accessions
        Map<Study, UUID> accessionedStudies = generator.generateAccessions(fileSet);
        for (Map.Entry<Study, UUID> entry : accessionedStudies.entrySet()) {
            entry.getKey().setAccession(entry.getValue());
        }
        accessionRepository.save(accessionedStudies.keySet());
        assertEquals(2, accessionRepository.count());

        // Storing again the same studies with new accessions overwrites the existing ones
        Map<Study, UUID> alternativeAccesionedStudies = alternativeGenerator.generateAccessions(fileSet);
        for (Map.Entry<Study, UUID> entry : alternativeAccesionedStudies.entrySet()) {
            entry.getKey().setAccession(entry.getValue());
        }
        accessionRepository.save(alternativeAccesionedStudies.keySet());
        assertEquals(2, accessionRepository.count());
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void cantStoreFileWithoutAccession() {
        Study file = new Study("Title1", "Type1", "Email1");
        accessionRepository.save(file);
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void cantStoreMultipleStudiesWithSameAccession() {
        Study originalFile = new Study("Title1", "Type1", "Email1");
        originalFile.setAccession(UUID.randomUUID());
        accessionRepository.save(originalFile);
        assertEquals(1, accessionRepository.count());

        Study newFile = new Study("Title2", "Type2", "Email2");
        newFile.setAccession(originalFile.getAccession());
        accessionRepository.save(newFile);
    }
}
