/*
 *
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ampt2d.accession;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.accession.generator.TestAccessionGeneratorA;
import uk.ac.ebi.ampt2d.accession.generator.TestAccessionGeneratorB;
import uk.ac.ebi.ampt2d.accession.generator.TestPrefixAccessionGenerator;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class AccessioningServiceGeneratorInjectionTest {

    @Value("#{testAccessionGeneratorA}")
    AccessionGenerator<String> accessionGeneratorA;

    @Value("#{testAccessionGeneratorB}")
    AccessionGenerator<String> accessionGeneratorB;

    @Test
    public void generatorATest() {
        String object1 = "obj1";
        Map<String, String> accesions = accessionGeneratorA.get(Collections.singleton(object1));
        System.out.println("accesions.get(object1) = " + accesions.get(object1));
        assertTrue(accesions.get(object1).startsWith("A"));
    }

    @Test
    public void generatorBTest() {
        String object1 = "obj1";
        Map<String, String> accesions = accessionGeneratorB.get(Collections.singleton(object1));
        System.out.println("accesions.get(object1) = " + accesions.get(object1));
        assertTrue(accesions.get(object1).startsWith("B"));
    }

    @Configuration
    @ComponentScan(basePackages = "uk.ac.ebi.ampt2d.accession.generator")
    public static class TestConfiguration {

        @Bean
        TestPrefixAccessionGenerator testAccessionGeneratorA() {
            return new TestAccessionGeneratorA();
        }

        @Bean
        TestPrefixAccessionGenerator testAccessionGeneratorB() {
            return new TestAccessionGeneratorB();
        }
    }
}
