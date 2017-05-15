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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.accession.test.generator.TestAccessionGeneratorA;
import uk.ac.ebi.ampt2d.accession.test.configuration.TestsGeneratorConfiguration;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = TestsGeneratorConfiguration.class, properties = "test.generator=generatorA")
public class AccessioningServiceGeneratorAInjectionTest {

    @Autowired
    private AccessionGenerator<String> accessionGenerator;

    @Test
    public void generatorTest() {
        assertNotNull(accessionGenerator);

        String object1 = "obj1";
        Map<String, String> accesions = accessionGenerator.get(Collections.singleton(object1));
        assertTrue(accesions.get(object1).startsWith(TestAccessionGeneratorA.PREFIX));
    }
}
