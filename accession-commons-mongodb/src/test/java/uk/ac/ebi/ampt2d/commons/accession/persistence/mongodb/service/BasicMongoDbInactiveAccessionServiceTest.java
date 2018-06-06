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
package uk.ac.ebi.ampt2d.commons.accession.persistence.mongodb.service;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbConfigurationBuilder;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.commons.accession.core.OperationType;
import uk.ac.ebi.ampt2d.commons.accession.persistence.IOperation;
import uk.ac.ebi.ampt2d.test.configuration.MongoDbTestConfiguration;
import uk.ac.ebi.ampt2d.test.persistence.document.TestDocument;
import uk.ac.ebi.ampt2d.test.persistence.document.TestOperationDocument;
import uk.ac.ebi.ampt2d.test.persistence.repository.TestRepository;
import uk.ac.ebi.ampt2d.test.persistence.service.TestMongoDbInactiveAccessionService;
import uk.ac.ebi.ampt2d.test.rule.FixSpringMongoDbRule;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.ac.ebi.ampt2d.commons.accession.core.OperationType.DEPRECATED;
import static uk.ac.ebi.ampt2d.commons.accession.core.OperationType.MERGED_INTO;
import static uk.ac.ebi.ampt2d.commons.accession.core.OperationType.UPDATED;
import static uk.ac.ebi.ampt2d.test.persistence.document.TestDocument.document;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MongoDbTestConfiguration.class})
public class BasicMongoDbInactiveAccessionServiceTest {

    private static final String DEFAULT_REASON = "default-test-reason";
    @Rule
    public MongoDbRule mongoDbRule = new FixSpringMongoDbRule(MongoDbConfigurationBuilder.mongoDb()
            .databaseName("accession-test").build());

    //Required for nosql unit
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TestRepository repository;

    @Autowired
    private TestMongoDbInactiveAccessionService service;

    @UsingDataSet(loadStrategy = LoadStrategyEnum.DELETE_ALL)
    @Test
    public void testLastOperationDoesNotExistBehaviour() {
        new LastOperationAsserts("notExist").doesNotExist();
    }

    @UsingDataSet(loadStrategy = LoadStrategyEnum.DELETE_ALL)
    @Test
    public void testUpdate() {
        update(document(1, "test-update-1")).exists().isUpdate();
    }

    @UsingDataSet(loadStrategy = LoadStrategyEnum.DELETE_ALL)
    @Test
    public void testDeprecate() {
        deprecate(document(1, "test-deprecate-1")).exists().isDeprecate();
    }

    @UsingDataSet(loadStrategy = LoadStrategyEnum.DELETE_ALL)
    @Test
    public void testMerge() {
        merge(document(1, "test-deprecate-1"), "a2").exists().isMerge("a1", "a2", 1);
    }

    private LastOperationAsserts merge(TestDocument document, String accession) {
        service.merge(document.getAccession(), accession, Arrays.asList(document), DEFAULT_REASON);
        return new LastOperationAsserts(document.getAccession());
    }

    private LastOperationAsserts update(TestDocument document) {
        repository.insert(Arrays.asList(document));
        service.update(document, DEFAULT_REASON);
        return new LastOperationAsserts(document.getAccession());
    }

    private LastOperationAsserts deprecate(TestDocument document) {
        repository.insert(Arrays.asList(document));
        service.deprecate(document.getAccession(), Arrays.asList(document), DEFAULT_REASON);
        return new LastOperationAsserts(document.getAccession());
    }

    private class LastOperationAsserts {

        private final IOperation<String> lastOperation;

        public LastOperationAsserts(String accession) {
            this.lastOperation = service.getLastOperation(accession);
        }

        public LastOperationAsserts doesNotExist() {
            assertNull(lastOperation);
            return this;
        }

        public LastOperationAsserts exists() {
            assertNotNull(lastOperation);
            return this;
        }

        public LastOperationAsserts isUpdate() {
            return isOfType(UPDATED);
        }

        private LastOperationAsserts isOfType(OperationType type) {
            assertEquals(type, lastOperation.getOperationType());
            return this;
        }

        public LastOperationAsserts isDeprecate() {
            return isOfType(DEPRECATED);
        }

        public LastOperationAsserts isMerge(String origin, String destination, int totalElements) {
            return isOfType(MERGED_INTO).isOrigin(origin).isDestination(destination).totalStoredElements(totalElements);
        }

        private LastOperationAsserts isOrigin(String origin) {
            assertEquals(origin, lastOperation.getAccessionIdOrigin());
            return this;
        }

        private LastOperationAsserts isDestination(String destination) {
            assertEquals(destination, lastOperation.getAccessionIdDestination());
            return this;
        }

        private LastOperationAsserts totalStoredElements(int totalElements) {
            assertEquals(totalElements, ((TestOperationDocument) lastOperation).getInactiveObjects().size());
            return this;
        }

    }
}
