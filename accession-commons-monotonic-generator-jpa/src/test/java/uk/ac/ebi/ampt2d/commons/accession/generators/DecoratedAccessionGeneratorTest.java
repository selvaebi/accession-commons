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
package uk.ac.ebi.ampt2d.commons.accession.generators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.models.SaveResponse;
import uk.ac.ebi.ampt2d.commons.accession.generators.monotonic.MonotonicAccessionGenerator;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.repositories.ContiguousIdBlockRepository;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.service.ContiguousIdBlockService;
import uk.ac.ebi.ampt2d.test.configuration.MonotonicAccessionGeneratorTestConfiguration;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {MonotonicAccessionGeneratorTestConfiguration.class})
public class DecoratedAccessionGeneratorTest {

    private static final int BLOCK_SIZE = 1000;
    private static final long NEXT_BLOCK_INTERVAL = 0L;
    private static final String CATEGORY_ID = "DECORATOR_TEST";
    private static final String INSTANCE_ID = "decorator-inst-01";

    @Autowired
    private ContiguousIdBlockRepository repository;

    @Autowired
    private ContiguousIdBlockService service;

    @Test
    public void testGeneratePrefixSuffix() throws Exception {
        Map<String, String> objects = new LinkedHashMap<>();
        objects.put("hash1", "string1");
        objects.put("hash2", "string2");
        objects.put("hash3", "string3");

        DecoratedAccessionGenerator<String, Long> generator = DecoratedAccessionGenerator
                .buildPrefixSuffixAccessionGenerator(getGenerator(), "prefix-", "-suffix", Long::parseLong);

        List<AccessionWrapper<String, String, String>> generated = generator.generateAccessions(objects);
        assertEquals(3, generated.size());
        assertEquals("prefix-0-suffix", generated.get(0).getAccession());
        assertEquals("prefix-1-suffix", generated.get(1).getAccession());
        assertEquals("prefix-2-suffix", generated.get(2).getAccession());

        Set<String> savedAccessions = new HashSet<>();
        savedAccessions.add("prefix-0-suffix");
        savedAccessions.add("prefix-1-suffix");
        savedAccessions.add("prefix-2-suffix");

        generator.postSave(new SaveResponse<>(savedAccessions, new HashSet<>()));
        assertEquals(2, repository.findFirstByCategoryIdAndApplicationInstanceIdOrderByLastValueDesc(
                CATEGORY_ID, INSTANCE_ID).getLastCommitted());
    }

    @Test
    public void testGeneratePrefix() throws Exception {
        Map<String, String> objects = new LinkedHashMap<>();
        objects.put("hash1", "string1");
        objects.put("hash2", "string2");
        objects.put("hash3", "string3");

        DecoratedAccessionGenerator<String, Long> generator = DecoratedAccessionGenerator
                .buildPrefixSuffixAccessionGenerator(getGenerator(), "prefix-", null, Long::parseLong);

        List<AccessionWrapper<String, String, String>> generated = generator.generateAccessions(objects);
        assertEquals(3, generated.size());
        assertEquals("prefix-0", generated.get(0).getAccession());
        assertEquals("prefix-1", generated.get(1).getAccession());
        assertEquals("prefix-2", generated.get(2).getAccession());

        Set<String> savedAccessions = new HashSet<>();
        savedAccessions.add("prefix-0");
        savedAccessions.add("prefix-1");
        savedAccessions.add("prefix-2");

        generator.postSave(new SaveResponse<>(savedAccessions, new HashSet<>()));
        assertEquals(2, repository.findFirstByCategoryIdAndApplicationInstanceIdOrderByLastValueDesc(
                CATEGORY_ID, INSTANCE_ID).getLastCommitted());
    }

    @Test
    public void testGenerateSuffix() throws Exception {
        Map<String, String> objects = new LinkedHashMap<>();
        objects.put("hash1", "string1");
        objects.put("hash2", "string2");
        objects.put("hash3", "string3");

        DecoratedAccessionGenerator<String, Long> generator = DecoratedAccessionGenerator
                .buildPrefixSuffixAccessionGenerator(getGenerator(), null, "-suffix", Long::parseLong);

        List<AccessionWrapper<String, String, String>> generated = generator.generateAccessions(objects);
        assertEquals(3, generated.size());
        assertEquals("0-suffix", generated.get(0).getAccession());
        assertEquals("1-suffix", generated.get(1).getAccession());
        assertEquals("2-suffix", generated.get(2).getAccession());

        Set<String> savedAccessions = new HashSet<>();
        savedAccessions.add("0-suffix");
        savedAccessions.add("1-suffix");
        savedAccessions.add("2-suffix");

        generator.postSave(new SaveResponse<>(savedAccessions, new HashSet<>()));
        assertEquals(2, repository.findFirstByCategoryIdAndApplicationInstanceIdOrderByLastValueDesc(
                CATEGORY_ID, INSTANCE_ID).getLastCommitted());
    }

    private MonotonicAccessionGenerator<String> getGenerator() throws Exception {
        assertEquals(0, repository.count());

        MonotonicAccessionGenerator<String> generator = new MonotonicAccessionGenerator<>(BLOCK_SIZE, NEXT_BLOCK_INTERVAL,
                INSTANCE_ID, CATEGORY_ID,service);
        return generator;
    }

}
