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
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import uk.ac.ebi.ampt2d.commons.accession.autoconfigure.EnableBasicRestControllerAdvice;
import uk.ac.ebi.ampt2d.commons.accession.persistence.ArchiveService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.BasicArchiveService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.BasicSpringDataRepositoryDatabaseService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.DatabaseService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.IAccessionedObjectCustomRepository;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.accession.repositories.BasicJpaAccessionedObjectCustomRepositoryImpl;
import uk.ac.ebi.ampt2d.test.TestModel;
import uk.ac.ebi.ampt2d.test.persistence.TestArchivedAccessionEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestArchivedAccessionRepository;
import uk.ac.ebi.ampt2d.test.persistence.TestEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestRepository;
import uk.ac.ebi.ampt2d.test.persistence.TestStringHistoryRepository;
import uk.ac.ebi.ampt2d.test.persistence.TestStringOperationEntity;
import uk.ac.ebi.ampt2d.test.rest.TestController;

@Configuration
@EnableWebMvc
@EnableBasicRestControllerAdvice
@ComponentScan(basePackageClasses = IAccessionedObjectCustomRepository.class)
@EnableJpaAuditing
@ComponentScan(basePackageClasses = BasicJpaAccessionedObjectCustomRepositoryImpl.class)
@EntityScan("uk.ac.ebi.ampt2d.test.persistence")
@EnableJpaRepositories(basePackages = {"uk.ac.ebi.ampt2d.test.persistence",
        "uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.accession.repositories"
})
@AutoConfigureCache
@AutoConfigureDataJpa
@AutoConfigureTestEntityManager
public class BasicRestControllerTestConfiguration {

    @Autowired
    private TestRepository repository;

    @Autowired
    private TestStringHistoryRepository historyRepository;

    @Autowired
    private TestArchivedAccessionRepository testArchivedAccessionRepository;

    @Bean
    public TestController testController() {
        return new TestController(databaseService());
    }

    @Bean
    public DatabaseService<TestModel, String, String> databaseService() {
        return new BasicSpringDataRepositoryDatabaseService<>(
                repository,
                TestEntity::new,
                TestModel.class::cast,
                archiveService()
        );
    }

    @Bean
    public ArchiveService<TestModel, String, String, TestEntity> archiveService() {
        return new BasicArchiveService<>(
                testArchivedAccessionRepository,
                TestArchivedAccessionEntity::new,
                historyRepository,
                TestStringOperationEntity::new,
                TestModel.class::cast
        );
    }

}
