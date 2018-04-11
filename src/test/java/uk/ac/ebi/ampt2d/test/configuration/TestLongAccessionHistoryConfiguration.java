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
import uk.ac.ebi.ampt2d.commons.accession.persistence.history.service.BasicAccessionHistoryTrackingService;
import uk.ac.ebi.ampt2d.test.persistence.TestAccessionHistoryLongEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestAccessionHistoryLongRepository;

@Configuration
@EntityScan(value = {"uk.ac.ebi.ampt2d.test.persistence"})
@EnableJpaRepositories(basePackages = "uk.ac.ebi.ampt2d.test.persistence")
public class TestLongAccessionHistoryConfiguration {

    @Autowired
    private TestAccessionHistoryLongRepository longAccessionRepository;

    @Bean
    public BasicAccessionHistoryTrackingService<TestAccessionHistoryLongEntity, Long> getAccessionHistoryTrackingService() {
        return new BasicAccessionHistoryTrackingService<>(longAccessionRepository,
                TestAccessionHistoryLongEntity::new);
    }

}
