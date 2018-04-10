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
package uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.accession.repositories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.commons.accession.generators.ModelHashAccession;
import uk.ac.ebi.ampt2d.test.TestModel;
import uk.ac.ebi.ampt2d.test.configuration.TestJpaDatabaseServiceTestConfiguration;
import uk.ac.ebi.ampt2d.test.persistence.TestEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestRepository;

import java.time.LocalDateTime;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {TestJpaDatabaseServiceTestConfiguration.class})
public class JpaAccessionedObjectRepositoryTest {

    @Autowired
    private TestRepository repository;

    @Test
    public void testSaveInitializesActiveFlag() {
        TestEntity savedEntity = repository.save(new TestEntity(ModelHashAccession.of(TestModel.of("something1"),
                "h1", "a1")));
        assertTrue(savedEntity.isActive());
    }

    @Test
    public void testSaveInitializesCreatedDate() {
        LocalDateTime beforeSave = LocalDateTime.now();
        TestEntity savedEntity = repository.save(new TestEntity(ModelHashAccession.of(TestModel.of("something1"),
                "h1", "a1")));
        assertTrue(beforeSave.isBefore(savedEntity.getCreatedDate()));
    }

    @Test
    public void testSave() {
        TestEntity savedEntity = repository.save(new TestEntity(ModelHashAccession.of(TestModel.of("something1"),
                "h1", "a1")));
        assertEquals("a1",savedEntity.getAccession());
        assertEquals("h1",savedEntity.getHashedMessage());
        assertEquals("something1",savedEntity.getSomething());
    }

}
