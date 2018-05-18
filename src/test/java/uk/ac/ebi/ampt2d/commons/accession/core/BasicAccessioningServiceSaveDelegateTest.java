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
package uk.ac.ebi.ampt2d.commons.accession.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionCouldNotBeGeneratedException;
import uk.ac.ebi.ampt2d.commons.accession.generators.SingleAccessionGenerator;
import uk.ac.ebi.ampt2d.commons.accession.hashing.SHA1HashingFunction;
import uk.ac.ebi.ampt2d.commons.accession.persistence.BasicSpringDataRepositoryDatabaseService;
import uk.ac.ebi.ampt2d.test.TestModel;
import uk.ac.ebi.ampt2d.test.configuration.TestJpaDatabaseServiceTestConfiguration;
import uk.ac.ebi.ampt2d.test.persistence.TestEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestRepository;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {TestJpaDatabaseServiceTestConfiguration.class})
public class BasicAccessioningServiceSaveDelegateTest {

    @Autowired
    private TestRepository repository;

    @Autowired
    private BasicSpringDataRepositoryDatabaseService<TestModel, String, TestEntity> databaseService;

    @Test
    public void testUnsaveIfExistPreviousWithSameHash() throws AccessionCouldNotBeGeneratedException {
        TestTransaction.flagForCommit();
        repository.save(new TestEntity("id-test-3", "3323D8A8F66A0602CC59372E866DD8E116DCCDB2", 1, "test-3"));
        TestTransaction.end();

        Map<String, TestModel> accessions = new HashMap<>();
        accessions.put("3323D8A8F66A0602CC59372E866DD8E116DCCDB4", TestModel.of("test-1"));
        accessions.put("3323D8A8F66A0602CC59372E866DD8E116DCCDB3", TestModel.of("test-2"));
        accessions.put("3323D8A8F66A0602CC59372E866DD8E116DCCDB2", TestModel.of("test-3"));

        BasicAccessioningServiceSaveDelegate<TestModel, String, String> delegate =
                new BasicAccessioningServiceSaveDelegate<>(databaseService);

        SaveResponse<String> generatedAccessions = delegate.doSaveAccessions(getAccessioningService()
                .getAccessionGenerator().generateAccessions(accessions));

        assertEquals(2, generatedAccessions.getSavedAccessions().size());
        assertEquals(1, generatedAccessions.getSaveFailedAccessions().size());

        TestTransaction.start();
        TestTransaction.flagForCommit();
        repository.delete("3323D8A8F66A0602CC59372E866DD8E116DCCDB2");
        repository.delete("3323D8A8F66A0602CC59372E866DD8E116DCCDB3");
        repository.delete("3323D8A8F66A0602CC59372E866DD8E116DCCDB4");
        TestTransaction.end();
    }

    private BasicAccessioningService<TestModel, String, String> getAccessioningService() {
        return new BasicAccessioningService<>(
                SingleAccessionGenerator.ofHashAccessionGenerator(
                        TestModel::getValue,
                        s -> "id-" + s
                ),
                databaseService,
                TestModel::getValue,
                new SHA1HashingFunction()
        );
    }

    @Test
    public void testCompleteSaveIfSameAccessionDifferentHash() throws AccessionCouldNotBeGeneratedException {
        TestTransaction.flagForCommit();
        repository.save(new TestEntity("id-test-3", "3323D8A8F66A0602CC59372E866DD8E116DCCDB2", 1, "test-3"));
        TestTransaction.end();

        Map<String, TestModel> accessions = new HashMap<>();
        accessions.put("3323D8A8F66A0602CC59372E866DD8E116DCCDB4", TestModel.of("test-1"));
        accessions.put("3323D8A8F66A0602CC59372E866DD8E116DCCDB3", TestModel.of("test-2"));
        accessions.put("3323D8A8F66A0602CC59372E866DD8E116DCCDB5", TestModel.of("test-3"));

        BasicAccessioningServiceSaveDelegate<TestModel, String, String> delegate =
                new BasicAccessioningServiceSaveDelegate<>(databaseService);

        SaveResponse<String> generatedAccessions = delegate.doSaveAccessions(getAccessioningService()
                .getAccessionGenerator().generateAccessions(accessions));

        assertEquals(3, generatedAccessions.getSavedAccessions().size());
        assertEquals(0, generatedAccessions.getSaveFailedAccessions().size());
        assertEquals("id-test-3", repository.findOne("3323D8A8F66A0602CC59372E866DD8E116DCCDB5").getAccession());
        assertEquals("id-test-3", repository.findOne("3323D8A8F66A0602CC59372E866DD8E116DCCDB2").getAccession());

        TestTransaction.start();
        TestTransaction.flagForCommit();
        repository.delete("3323D8A8F66A0602CC59372E866DD8E116DCCDB2");
        repository.delete("3323D8A8F66A0602CC59372E866DD8E116DCCDB3");
        repository.delete("3323D8A8F66A0602CC59372E866DD8E116DCCDB4");
        repository.delete("3323D8A8F66A0602CC59372E866DD8E116DCCDB5");
        TestTransaction.end();
    }

}
