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
import uk.ac.ebi.ampt2d.commons.accession.core.AccessionVersionsWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.AccessionWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.OperationType;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDeprecatedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDoesNotExistException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionMergedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.HashAlreadyExistsException;
import uk.ac.ebi.ampt2d.commons.accession.persistence.DatabaseService;
import uk.ac.ebi.ampt2d.test.TestModel;
import uk.ac.ebi.ampt2d.test.configuration.TestJpaDatabaseServiceTestConfiguration;
import uk.ac.ebi.ampt2d.test.persistence.TestArchivedAccessionEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestArchivedAccessionRepository;
import uk.ac.ebi.ampt2d.test.persistence.TestStringHistoryRepository;
import uk.ac.ebi.ampt2d.test.persistence.TestStringOperationEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
    private DatabaseService<TestModel, String, String> service;

    @Autowired
    private TestStringHistoryRepository historyRepository;

    @Autowired
    private TestArchivedAccessionRepository accessionArchive;

    @Test
    public void testFindInEmptyRepository() {
        assertEquals(0, service.findAllAccessions(Arrays.asList("a1", "a2")).size());
        assertEquals(0, service.findAccessionsByHash(Arrays.asList("h1", "h2")).size());
    }

    @Test
    public void saveUniqueElementsAndFindByAccessionReturnsEachAccession() {
        service.insert(Arrays.asList(TEST_MODEL_1, TEST_MODEL_2, TEST_MODEL_3));

        List<AccessionWrapper<TestModel, String, String>> result = service.findAllAccessions(
                Arrays.asList("a1", "a2", "a3"));
        assertEquals(3, result.size());
        List<AccessionWrapper<TestModel, String, String>> resultAccession1 = service.findAllAccessions(Arrays.asList("a1"));
        assertEquals(1, resultAccession1.size());
        assertEquals("something1", resultAccession1.get(0).getData().getValue());
        List<AccessionWrapper<TestModel, String, String>> resultAccession2 = service.findAllAccessions(
                Arrays.asList("a2"));
        assertEquals(1, resultAccession2.size());
        assertEquals("something2", resultAccession2.get(0).getData().getValue());
    }

    @Test
    public void saveUniqueElementsAndFindByAccessionThatDoesNotExistReturnsNothing() {
        service.insert(Arrays.asList(TEST_MODEL_1, TEST_MODEL_2, TEST_MODEL_3));

        List<AccessionWrapper<TestModel, String, String>> result = service.findAllAccessions(Arrays.asList("a0"));
        assertEquals(0, result.size());
    }

    @Test
    public void saveUniqueElementsAndFindByHashReturnsEachAccession() {
        service.insert(Arrays.asList(TEST_MODEL_1, TEST_MODEL_2, TEST_MODEL_3));

        List<AccessionWrapper<TestModel, String, String>> results = service.findAccessionsByHash(
                Arrays.asList("h1", "h2"));
        assertEquals(2, results.size());
    }

    @Test
    public void saveUniqueElementsAndFindByHashThatDoesNotExistReturnsNothing() {
        service.insert(Arrays.asList(TEST_MODEL_1, TEST_MODEL_2, TEST_MODEL_3));

        List<AccessionWrapper<TestModel, String, String>> results = service.findAccessionsByHash(
                Arrays.asList("h0"));
        assertEquals(0, results.size());
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
            HashAlreadyExistsException, AccessionDeprecatedException, AccessionMergedException {
        service.update("a2", "h1", TestModel.of("something2"), 1);
    }

    @Test(expected = HashAlreadyExistsException.class)
    public void updateWithExistingObjectFails() throws AccessionDoesNotExistException,
            HashAlreadyExistsException, AccessionDeprecatedException, AccessionMergedException {
        service.insert(Arrays.asList(new AccessionWrapper<>("a2", "h1", TestModel.of("something2"))));
        service.update("a2", "h1", TestModel.of("something2"), 1);
    }

    @Test
    public void updateDoesNotCreateNewVersion() throws AccessionDoesNotExistException,
            HashAlreadyExistsException, AccessionDeprecatedException, AccessionMergedException {
        service.insert(Arrays.asList(new AccessionWrapper<>("a2", "h1", TestModel.of("something2"))));
        final AccessionVersionsWrapper<TestModel, String, String> update =
                service.update("a2", "h2", TestModel.of("something2b"), 1);
        assertEquals(1, update.getModelWrappers().size());
        assertEquals(1, update.getModelWrappers().get(0).getVersion());
    }

    @Test
    public void patchCreatesNewVersion() throws AccessionDoesNotExistException,
            HashAlreadyExistsException, AccessionDeprecatedException, AccessionMergedException {
        service.insert(Arrays.asList(new AccessionWrapper<>("a2", "h1", TestModel.of("something2"))));
        final AccessionVersionsWrapper<TestModel, String, String> patch =
                service.patch("a2", "h2", TestModel.of("something2b"));
        assertEquals(2, patch.getModelWrappers().size());
        assertEquals(1, patch.getModelWrappers().get(0).getVersion());
        assertEquals(2, patch.getModelWrappers().get(1).getVersion());
    }

    @Test
    public void findAllAccessionReturnsOnlyLastPatch() throws AccessionDoesNotExistException,
            HashAlreadyExistsException, AccessionDeprecatedException, AccessionMergedException {
        service.insert(Arrays.asList(new AccessionWrapper<>("a2", "h1", TestModel.of("something2"))));
        service.patch("a2", "h2", TestModel.of("something2b"));
        assertEquals(2, service.findAllAccessions(Arrays.asList("a2")).get(0).getVersion());
    }

    @Test
    public void findAccessionShowsAllPatches() throws AccessionDoesNotExistException,
            HashAlreadyExistsException, AccessionDeprecatedException, AccessionMergedException {
        service.insert(Arrays.asList(new AccessionWrapper<>("a2", "h1", TestModel.of("something2"))));
        service.patch("a2", "h2", TestModel.of("something2b"));
        assertEquals(2, service.findAccession("a2").getModelWrappers().size());
    }

    @Test
    public void findAccessionVersion() throws AccessionDoesNotExistException,
            HashAlreadyExistsException, AccessionDeprecatedException, AccessionMergedException {
        service.insert(Arrays.asList(new AccessionWrapper<>("a2", "h1", TestModel.of("something2"))));
        service.patch("a2", "h2", TestModel.of("something2b"));
        assertEquals(1, service.findAccessionVersion("a2", 1).getVersion());
        assertEquals(2, service.findAccessionVersion("a2", 2).getVersion());
    }

    @Test
    public void multipleUpdates() throws AccessionDoesNotExistException,
            HashAlreadyExistsException, AccessionDeprecatedException, AccessionMergedException {
        service.insert(Arrays.asList(new AccessionWrapper<>("a2", "h1", TestModel.of("something2"))));
        service.update("a2", "h2", TestModel.of("something2b"), 1);
        service.update("a2", "h3", TestModel.of("something2c"), 1);

        List<AccessionWrapper<TestModel, String, String>> accessions = service.findAllAccessions(Arrays.asList("a2"));
        assertEquals(1, accessions.size());
    }

    @Test
    public void multiplePatches() throws AccessionDoesNotExistException,
            HashAlreadyExistsException, AccessionDeprecatedException, AccessionMergedException {
        service.insert(Arrays.asList(new AccessionWrapper<>("a2", "h1", TestModel.of("something2"))));
        service.patch("a2", "h2", TestModel.of("something2b"));
        service.patch("a2", "h3", TestModel.of("something2c"));

        assertEquals(1, service.findAccessionVersion("a2", 1).getVersion());
        assertEquals(2, service.findAccessionVersion("a2", 2).getVersion());
        assertEquals(3, service.findAccessionVersion("a2", 3).getVersion());

        assertEquals(3, service.findAccession("a2").getModelWrappers().size());
    }

    @Test
    public void multiplePatchesFindAllReturnsLastVersion() throws AccessionDoesNotExistException,
            HashAlreadyExistsException, AccessionDeprecatedException, AccessionMergedException {
        service.insert(Arrays.asList(new AccessionWrapper<>("a2", "h1", TestModel.of("something2"))));
        service.patch("a2", "h2", TestModel.of("something2b"));
        service.patch("a2", "h3", TestModel.of("something2c"));

        List<AccessionWrapper<TestModel, String, String>> accessions = service.findAllAccessions(Arrays.asList("a2"));
        assertEquals(1, accessions.size());
        assertEquals(3, accessions.get(0).getVersion());
    }

    @Test
    public void testFindByAccessionAndVersion() throws AccessionDoesNotExistException,
            HashAlreadyExistsException, AccessionMergedException, AccessionDeprecatedException {
        service.insert(Arrays.asList(
                new AccessionWrapper("a2", "h1", TestModel.of("something2"), 1),
                new AccessionWrapper("a2", "h2", TestModel.of("something2b"), 2)));

        List<AccessionWrapper<TestModel, String, String>> accessions = service.findAllAccessions(Arrays.asList("a2"));
        assertEquals(1, accessions.size());

        AccessionWrapper<TestModel, String, String> accessionOfVersion1 = service.findAccessionVersion("a2", 1);
        assertEquals(1, accessionOfVersion1.getVersion());
        AccessionWrapper<TestModel, String, String> accessionsOfVersion2 = service.findAccessionVersion("a2", 2);
        assertEquals(2, accessionsOfVersion2.getVersion());
    }

    @Test(expected = AccessionDoesNotExistException.class)
    public void testDeprecateNotExisting() throws AccessionDoesNotExistException, AccessionDeprecatedException, AccessionMergedException {
        assertEquals(0, service.findAllAccessions(Arrays.asList("a1")).size());
        service.deprecate("a1", "reasons");
    }

    @Test
    public void testDeprecateOneVersion() throws AccessionDoesNotExistException, AccessionDeprecatedException, AccessionMergedException {
        service.insert(Arrays.asList(new AccessionWrapper("a1", "h1", TestModel.of("something2"), 1)));
        assertEquals(1, service.findAllAccessions(Arrays.asList("a1")).size());
        service.deprecate("a1", "reasons");
        assertEquals(0, service.findAllAccessions(Arrays.asList("a1")).size());

        final TestStringOperationEntity historyEntity = historyRepository.findAll().iterator().next();
        assertEquals(OperationType.DEPRECATED, historyEntity.getOperationType());
        assertEquals("a1", historyEntity.getAccessionIdOrigin());
        assertEquals("reasons", historyEntity.getReason());

        final List<TestArchivedAccessionEntity> deprecated = accessionArchive.findAllByHistoryId(historyEntity.getId());
        assertEquals(1, deprecated.size());
        assertEquals("something2", deprecated.get(0).getValue());
    }

    @Test
    public void testDeprecateMultipleVersion() throws AccessionDoesNotExistException, AccessionMergedException, AccessionDeprecatedException, HashAlreadyExistsException {
        service.insert(Arrays.asList(new AccessionWrapper("a1", "h1", TestModel.of("something2"), 1)));
        service.patch("a1", "h2", TestModel.of("something2b"));
        assertEquals(1, service.findAllAccessions(Arrays.asList("a1")).size());
        assertEquals(2, service.findAllAccessions(Arrays.asList("a1")).get(0).getVersion());
        service.deprecate("a1", "reasons");
        assertEquals(0, service.findAllAccessions(Arrays.asList("a1")).size());

        final TestStringOperationEntity historyEntity = historyRepository.findAll().iterator().next();
        assertEquals(OperationType.DEPRECATED, historyEntity.getOperationType());
        assertEquals("a1", historyEntity.getAccessionIdOrigin());
        assertEquals("reasons", historyEntity.getReason());

        final List<TestArchivedAccessionEntity> deprecated = accessionArchive.findAllByHistoryId(historyEntity.getId());
        assertEquals(2, deprecated.size());
    }

    @Test
    public void testDeprecateAndAccessionSameObjectMultipleTimes() throws AccessionDoesNotExistException,
            AccessionDeprecatedException, AccessionMergedException {
        service.insert(Arrays.asList(new AccessionWrapper("a1", "h1", TestModel.of("something2"), 1)));
        assertEquals(1, service.findAllAccessions(Arrays.asList("a1")).size());
        service.deprecate("a1", "reasons");
        assertEquals(0, service.findAllAccessions(Arrays.asList("a1")).size());
        service.insert(Arrays.asList(new AccessionWrapper("a1", "h1", TestModel.of("something2"), 1)));
        assertEquals(1, service.findAllAccessions(Arrays.asList("a1")).size());
        service.deprecate("a1", "reasons");
        assertEquals(0, service.findAllAccessions(Arrays.asList("a1")).size());

        assertEquals(2, historyRepository.count());
    }

}
