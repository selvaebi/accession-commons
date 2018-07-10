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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.ac.ebi.ampt2d.commons.accession.rest.BasicRestControllerAdvice;
import uk.ac.ebi.ampt2d.commons.accession.rest.validation.CollectionValidator;

/**
 * Basic configuration to inject a {@link BasicRestControllerAdvice} to return the appropriate response to the
 * exceptions thrown by the generator.
 */
@Configuration
@ComponentScan(
        basePackageClasses = {BasicRestControllerAdvice.class},
        useDefaultFilters = false,
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {BasicRestControllerAdvice.class})
        }
)
public class BasicRestControllerAdviceConfiguration {

    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public CollectionValidator collectionValidatorBean() {
        return new CollectionValidator(localValidatorFactoryBean());
    }

}
