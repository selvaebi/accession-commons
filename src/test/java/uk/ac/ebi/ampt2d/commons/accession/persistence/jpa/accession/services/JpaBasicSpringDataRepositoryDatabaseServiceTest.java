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
package uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.accession.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.commons.accession.core.AccessionWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDoesNotExistException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.HashAlreadyExistsException;
import uk.ac.ebi.ampt2d.commons.accession.persistence.BasicSpringDataRepositoryDatabaseService;
import uk.ac.ebi.ampt2d.test.TestModel;
import uk.ac.ebi.ampt2d.test.configuration.TestJpaDatabaseServiceTestConfiguration;
import uk.ac.ebi.ampt2d.test.persistence.TestEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {TestJpaDatabaseServiceTestConfiguration.class})
public class JpaBasicSpringDataRepositoryDatabaseServiceTest {

    public static final AccessionWrapper<TestModel, String, String> TEST_MODEL_1 =
            new AccessionWrapper("a1", "h1", TestModel.of("something1"));
    public static final AccessionWrapper<TestModel, String, String> TEST_MODEL_2 =
            new AccessionWrapper("a2", "h2", TestModel.of("something2"));
    public static final AccessionWrapper<TestModel, String, String> TEST_MODEL_3 =
            new AccessionWrapper("a3", "h3", TestModel.of("something3"));

    @Autowired
    private BasicSpringDataRepositoryDatabaseService<TestModel, TestEntity, String> service;

    @Test
    public void testFindInEmptyRepository() {
        assertEquals(0, service.findAllAccessionMappingsByAccessions(Arrays.asList("a1", "a2")).size());
        assertEquals(0, service.findAllAccessionsByHash(Arrays.asList("h1", "h2")).size());
    }

    @Autowired
    private TestRepository repository;

    @Test
    public void saveUniqueElements() {
        service.insert(Arrays.asList(TEST_MODEL_1, TEST_MODEL_2, TEST_MODEL_3));

        List<AccessionWrapper<TestModel, String, String>> result = service.findAllAccessionMappingsByAccessions(
                Arrays.asList("a1", "a2"));
        assertEquals(2, result.size());
        assertTrue(result.contains(TEST_MODEL_1));
        assertTrue(result.contains(TEST_MODEL_2));

        List<AccessionWrapper<TestModel, String, String>> results2 = service.findAllAccessionsByHash(
                Arrays.asList("h1", "h2"));
        assertEquals(2, results2.size());
        assertTrue(result.contains(TEST_MODEL_1));
        assertTrue(result.contains(TEST_MODEL_2));

    }

    @Test(expected = DataIntegrityViolationException.class)
    public void saveNonUniqueElements() {
        service.insert(Arrays.asList(
                TEST_MODEL_1,
                new AccessionWrapper("a2", "h1", TestModel.of("something2")),
                TEST_MODEL_3
        ));
    }

    @Test(expected = AccessionDoesNotExistException.class)
    public void updateWithoutExistingAccessionFails() throws AccessionDoesNotExistException,
            HashAlreadyExistsException {
        service.update(new AccessionWrapper("a2", "h1", TestModel.of("something2")));
    }

    @Test(expected = HashAlreadyExistsException.class)
    public void updateWithExistingObjectFails() throws AccessionDoesNotExistException,
            HashAlreadyExistsException {
        service.insert(Arrays.asList(new AccessionWrapper("a2", "h1", TestModel.of("something2"))));
        service.update(new AccessionWrapper("a2", "h1", TestModel.of("something2")));
    }

    @Test
    public void update() throws AccessionDoesNotExistException,
            HashAlreadyExistsException {
        service.insert(Arrays.asList(new AccessionWrapper("a2", "h1", TestModel.of("something2"))));
        service.update(new AccessionWrapper("a2", "h2", TestModel.of("something2b")));
        List<AccessionWrapper<TestModel, String, String>> accessions =
                service.findAllAccessionMappingsByAccessions(Arrays.asList("a2"));
        assertEquals(2, accessions.size());
        assertFalse(accessions.get(0).getVersion() == accessions.get(1).getVersion());
    }

    @Test
    public void multipleUpdates() throws AccessionDoesNotExistException,
            HashAlreadyExistsException {
        service.insert(Arrays.asList(new AccessionWrapper("a2", "h1", TestModel.of("something2"))));
        service.update(new AccessionWrapper("a2", "h2", TestModel.of("something2b")));
        service.update(new AccessionWrapper("a2", "h3", TestModel.of("something2c")));

        List<AccessionWrapper<TestModel, String, String>> accessions =
                service.findAllAccessionMappingsByAccessions(Arrays.asList("a2"));
        assertEquals(3, accessions.size());

        Set<Integer> versions = accessions.stream().map(AccessionWrapper::getVersion).collect(Collectors.toSet());
        assertTrue(versions.contains(1));
        assertTrue(versions.contains(2));
        assertTrue(versions.contains(3));
    }

    @Test
    public void testFindByAccessionAndVersion() throws AccessionDoesNotExistException,
            HashAlreadyExistsException {
        service.insert(Arrays.asList(
                new AccessionWrapper("a2", "h1", TestModel.of("something2"), 1),
                new AccessionWrapper("a2", "h2", TestModel.of("something2b"), 2),
                new AccessionWrapper("a2", "h3", TestModel.of("something2c"), 2)));

        List<AccessionWrapper<TestModel, String, String>> accessions =
                service.findAllAccessionMappingsByAccessions(Arrays.asList("a2"));
        assertEquals(3, accessions.size());

        List<AccessionWrapper<TestModel, String, String>> accessionOfVersion1 =
                service.findAllAccessionMappingsByAccessionAndVersion("a2",1);
        assertEquals(1, accessionOfVersion1.size());
        List<AccessionWrapper<TestModel, String, String>> accessionsOfVersion2 =
                service.findAllAccessionMappingsByAccessionAndVersion("a2",2);
        assertEquals(2, accessionsOfVersion2.size());
    }

}
