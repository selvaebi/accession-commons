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
package uk.ac.ebi.ampt2d.test.configurationaccession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import uk.ac.ebi.ampt2d.accession.sample.SampleAccessioningService;
import uk.ac.ebi.ampt2d.accession.sample.persistence.SampleAccessioningDatabaseService;
import uk.ac.ebi.ampt2d.accession.sample.persistence.SampleAccessioningRepository;

@TestConfiguration
public class SampleAccessioningDatabaseServiceTestConfiguration {

    @Autowired
    private SampleAccessioningRepository repository;

    @Bean
    @ConditionalOnProperty(name = "services", havingValue = "sample-accession")
    public SampleAccessioningService sampleAccessionService() {
        return new SampleAccessioningService(sampleAccessioningDatabaseService());
    }

    @Bean
    @ConditionalOnProperty(name = "services", havingValue = "sample-accession")
    public SampleAccessioningDatabaseService sampleAccessioningDatabaseService() {
        return new SampleAccessioningDatabaseService(repository);
    }

}
