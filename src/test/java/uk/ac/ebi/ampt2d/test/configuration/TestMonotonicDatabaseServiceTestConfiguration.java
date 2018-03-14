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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.ac.ebi.ampt2d.commons.autoconfigure.EnableSpringDataContiguousIdService;
import uk.ac.ebi.ampt2d.commons.generators.monotonic.MonotonicAccessionGenerator;
import uk.ac.ebi.ampt2d.commons.generators.monotonic.persistence.service.ContiguousIdBlockService;
import uk.ac.ebi.ampt2d.test.TestModel;
import uk.ac.ebi.ampt2d.test.persistence.TestMonotonicEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestMonotonicRepository;
import uk.ac.ebi.ampt2d.test.service.TestMonotonicDatabaseService;

@Configuration
@EnableSpringDataContiguousIdService
@EntityScan("uk.ac.ebi.ampt2d.test.persistence")
@EnableJpaRepositories(basePackages = "uk.ac.ebi.ampt2d.test.persistence")
public class TestMonotonicDatabaseServiceTestConfiguration {

    private static final int BLOCK_SIZE = 1000;
    private static final String CATEGORY_ID = "monotonic-accession-service-test";
    private static final String INSTANCE_ID = "monotonic-accession-service-test";

    @Autowired
    private TestMonotonicRepository repository;

    @Autowired
    private ContiguousIdBlockService contiguousIdBlockService;

    @Bean
    public TestMonotonicDatabaseService getService() {
        return new TestMonotonicDatabaseService(
                repository,
                TestMonotonicEntity::new,
                TestMonotonicEntity::getAccession,
                TestMonotonicEntity::getHashedMessage
        );
    }

    @Bean
    public MonotonicAccessionGenerator<TestModel> monotonicAccessionGenerator() {
        return new MonotonicAccessionGenerator<>(
                BLOCK_SIZE,
                CATEGORY_ID,
                INSTANCE_ID,
                contiguousIdBlockService);
    }

}
