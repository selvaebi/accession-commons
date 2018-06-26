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
package uk.ac.ebi.ampt2d.test.persistence.service;

import uk.ac.ebi.ampt2d.commons.accession.persistence.repositories.IHistoryRepository;
import uk.ac.ebi.ampt2d.commons.accession.persistence.mongodb.service.BasicMongoDbInactiveAccessionService;
import uk.ac.ebi.ampt2d.test.models.TestModel;
import uk.ac.ebi.ampt2d.test.persistence.document.TestDocument;
import uk.ac.ebi.ampt2d.test.persistence.document.TestInactiveSubDocument;
import uk.ac.ebi.ampt2d.test.persistence.document.TestEventDocument;

import java.util.function.Function;
import java.util.function.Supplier;

public class TestMongoDbInactiveAccessionService extends BasicMongoDbInactiveAccessionService<TestModel, String,
        TestDocument,
        TestInactiveSubDocument, TestEventDocument> {

    public TestMongoDbInactiveAccessionService(
            IHistoryRepository<String, TestEventDocument, String> historyRepository,
            Function<TestDocument, TestInactiveSubDocument> toInactiveEntity,
            Supplier<TestEventDocument> supplier) {
        super(historyRepository, toInactiveEntity, supplier);
    }

}
