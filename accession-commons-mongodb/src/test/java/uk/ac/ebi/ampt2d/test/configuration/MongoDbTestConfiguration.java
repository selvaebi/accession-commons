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
package uk.ac.ebi.ampt2d.test.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import uk.ac.ebi.ampt2d.test.persistence.document.TestInactiveSubDocument;
import uk.ac.ebi.ampt2d.test.persistence.document.TestOperationDocument;
import uk.ac.ebi.ampt2d.test.persistence.repository.TestOperationRepository;
import uk.ac.ebi.ampt2d.test.persistence.service.TestMongoDbInactiveAccessionService;

@Configuration
@EntityScan(basePackages = {"uk.ac.ebi.ampt2d.test.persistence.document"})
@EnableMongoRepositories(basePackages = {"uk.ac.ebi.ampt2d.test.persistence.repository",
        "uk.ac.ebi.ampt2d.commons.accession.persistence.mongodb.repository"})
@EnableMongoAuditing
@AutoConfigureDataMongo
public class MongoDbTestConfiguration {

    @Autowired
    private TestOperationRepository testOperationRepository;

    @Bean
    public TestMongoDbInactiveAccessionService testMongoDbInactiveAccessionService() {
        return new TestMongoDbInactiveAccessionService(
                testOperationRepository,
                TestInactiveSubDocument::new,
                TestOperationDocument::new
        );
    }

}
