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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.ac.ebi.ampt2d.commons.accession.autoconfigure.EnableSpringDataContiguousIdService;
import uk.ac.ebi.ampt2d.commons.accession.autoconfigure.MonotonicGeneratorProperties;
import uk.ac.ebi.ampt2d.commons.accession.generators.monotonic.MonotonicAccessionGenerator;
import uk.ac.ebi.ampt2d.commons.accession.persistence.services.InactiveAccessionService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.service.ContiguousIdBlockService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.service.BasicJpaInactiveAccessionService;
import uk.ac.ebi.ampt2d.test.TestModel;
import uk.ac.ebi.ampt2d.test.persistence.TestLongHistoryRepository;
import uk.ac.ebi.ampt2d.test.persistence.TestLongOperationEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestMonotonicEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestMonotonicInactiveAccessionEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestMonotonicInactiveAccessionRepository;
import uk.ac.ebi.ampt2d.test.persistence.TestMonotonicRepository;
import uk.ac.ebi.ampt2d.test.service.TestMonotonicDatabaseService;

@Configuration
@EnableSpringDataContiguousIdService
@EntityScan("uk.ac.ebi.ampt2d.test.persistence")
@EnableJpaRepositories(basePackages = "uk.ac.ebi.ampt2d.test.persistence")
public class TestMonotonicDatabaseServiceTestConfiguration {

    @Autowired
    private TestMonotonicRepository repository;

    @Autowired
    private ContiguousIdBlockService contiguousIdBlockService;

    @Autowired
    private TestMonotonicInactiveAccessionRepository inactiveRepository;

    @Autowired
    private TestLongHistoryRepository historyRepository;

    @Bean
    public TestMonotonicDatabaseService getService() {
        return new TestMonotonicDatabaseService(
                repository,
                TestMonotonicEntity::new,
                inactiveService()
        );
    }

    @Bean
    @ConfigurationProperties(prefix = "accessioning.monotonic.category-id-monotonic-test")
    public MonotonicGeneratorProperties getMonotonicGeneratorProperties() {
        return new MonotonicGeneratorProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "accessioning.monotonic.eva")
    public MonotonicGeneratorProperties getMonotonicGeneratorPropertiesForEva() {
        return new MonotonicGeneratorProperties();
    }

    @Bean
    public MonotonicAccessionGenerator<TestModel> monotonicAccessionGenerator() {
        MonotonicGeneratorProperties monotonicGeneratorProperties = getMonotonicGeneratorProperties();
        return new MonotonicAccessionGenerator<>(
                monotonicGeneratorProperties.getBlockSize(),
                monotonicGeneratorProperties.getNextBlockInterval(),
                monotonicGeneratorProperties.getCategoryId(),
                monotonicGeneratorProperties.getApplicationInstanceId(),
                contiguousIdBlockService);
    }

    @Bean
    public InactiveAccessionService<TestModel, Long, TestMonotonicEntity> inactiveService() {
        return new BasicJpaInactiveAccessionService<>(
                historyRepository,
                TestMonotonicInactiveAccessionEntity::new,
                inactiveRepository,
                TestLongOperationEntity::new
        );
    }

}
