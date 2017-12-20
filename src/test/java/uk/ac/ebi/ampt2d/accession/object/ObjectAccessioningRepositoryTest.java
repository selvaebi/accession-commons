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
package uk.ac.ebi.ampt2d.accession.object;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.ampt2d.accession.AccessionGenerator;
import uk.ac.ebi.ampt2d.accession.SHA1AccessionGenerator;

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
@TestPropertySource(properties = "services=object-accession")
public class ObjectAccessioningRepositoryTest {

    @Autowired
    private ObjectAccessioningRepository objectAccessioningRepository;

    private AccessionGenerator<AccessionObject, String> generator;

    private AccessionGenerator<AccessionObject, String> alternativeGenerator;

    private Map<String, String> studyMap1;
    private Map<String, String> studyMap2;
    private AccessionObject accessionObject1;
    private AccessionObject accessionObject2;

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
        accessionObject1 = new AccessionObject(studyMap1);
        accessionObject2 = new AccessionObject(studyMap2);

    }

    @Test
    public void testStudiesAreStoredInTheRepository() throws Exception {

        Map<AccessionObject, String> accessionedStudies = generator.generateAccessions(new HashSet<>(Arrays.asList(accessionObject1, accessionObject2)));

        for (Map.Entry<AccessionObject, String> entry : accessionedStudies.entrySet()) {
            entry.getKey().setAccession(entry.getValue());
        }
        objectAccessioningRepository.save(accessionedStudies.keySet());

        assertEquals(2, objectAccessioningRepository.count());

        Collection<String> hashes = new ArrayList<>();
        hashes.add(accessionObject1.getHash());
        hashes.add(accessionObject2.getHash());
        Collection<AccessionObject> accessionsFromRepository = objectAccessioningRepository.findByHashIn(hashes);
        assertEquals(accessionsFromRepository.stream().sorted((e1, e2) -> e1.getHash().compareTo(e2.getHash())).collect(Collectors.toList()),
                new ArrayList<>(accessionedStudies.keySet()).stream().sorted((e1, e2) -> e1.getHash().compareTo(e2.getHash())).collect(Collectors.toList()));
    }

    @Test
    public void addingTheSameStudiesWithDifferentAccessionsOverwritesInTheRepository() throws Exception {

        HashSet<AccessionObject> fileSet = new HashSet<>(Arrays.asList(accessionObject1, accessionObject2));

        // Store the studies with the initial accessions
        Map<AccessionObject, String> accessionedStudies = generator.generateAccessions(fileSet);
        for (Map.Entry<AccessionObject, String> entry : accessionedStudies.entrySet()) {
            entry.getKey().setAccession(entry.getValue());
        }
        objectAccessioningRepository.save(accessionedStudies.keySet());
        assertEquals(2, objectAccessioningRepository.count());

        // Storing again the same studies with new accessions overwrites the existing ones
        Map<AccessionObject, String> alternativeAccesionedStudies = alternativeGenerator.generateAccessions(fileSet);
        for (Map.Entry<AccessionObject, String> entry : alternativeAccesionedStudies.entrySet()) {
            entry.getKey().setAccession(entry.getValue());
        }
        objectAccessioningRepository.save(alternativeAccesionedStudies.keySet());
        assertEquals(2, objectAccessioningRepository.count());
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void cantStoreFileWithoutAccession() {
        objectAccessioningRepository.save(accessionObject1);
    }

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void cantStoreMultipleStudiesWithSameAccession() {

        accessionObject1.setAccession("randomString");
        objectAccessioningRepository.save(accessionObject1);
        assertEquals(1, objectAccessioningRepository.count());

        accessionObject2.setAccession(accessionObject1.getAccession());
        objectAccessioningRepository.save(accessionObject2);
    }
}
