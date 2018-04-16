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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.ac.ebi.ampt2d.commons.accession.persistence.BasicSpringDataRepositoryDatabaseService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.ICustomMethodsRepository;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.accession.repositories.JpaCustomMethodsRepositoryImpl;
import uk.ac.ebi.ampt2d.test.TestModel;
import uk.ac.ebi.ampt2d.test.persistence.TestEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestRepository;

@Configuration
@ComponentScan(basePackageClasses = ICustomMethodsRepository.class)
@EnableJpaAuditing
@ComponentScan(basePackageClasses = JpaCustomMethodsRepositoryImpl.class)
@EntityScan("uk.ac.ebi.ampt2d.test.persistence")
@EnableJpaRepositories(basePackages = {"uk.ac.ebi.ampt2d.test.persistence",
        "uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.accession.repositories"
})
public class TestJpaDatabaseServiceTestConfiguration {

    @Autowired
    private TestRepository repository;

    @Bean
    public BasicSpringDataRepositoryDatabaseService<TestModel, TestEntity, String> getService() {
        return new BasicSpringDataRepositoryDatabaseService<>(
                repository,
                repository,
                TestEntity::new,
                TestModel.class::cast
        );
    }

}
