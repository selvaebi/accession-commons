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
package uk.ac.ebi.ampt2d.commons.accession.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.ac.ebi.ampt2d.commons.accession.block.initialization.BlockParameters;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.repositories.ContiguousIdBlockRepository;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.service.ContiguousIdBlockService;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Basic configuration to inject a {@link ContiguousIdBlockService} and configure the appropriate spring data jpa
 * repository using the default datasource injected by spring-boot property configuration.
 */
@Configuration
@EntityScan("uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.entities")
@EnableJpaRepositories(basePackages = "uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.repositories")
@ComponentScan(basePackages = "uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.service")
public class SpringDataContiguousIdServiceConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "accessioning.monotonic")
    public HashMap<String, HashMap<String, String>> contiguousBlockInitializations() {
        return new HashMap<>();
    }

    @Bean
    public ContiguousIdBlockService contiguousIdBlockService(@Autowired ContiguousIdBlockRepository
                                                                     contiguousIdBlockRepository) {
        return new ContiguousIdBlockService(contiguousIdBlockRepository, contiguousBlockInitializations().entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> new BlockParameters(entry.getKey(), entry.getValue()))));
    }

}
