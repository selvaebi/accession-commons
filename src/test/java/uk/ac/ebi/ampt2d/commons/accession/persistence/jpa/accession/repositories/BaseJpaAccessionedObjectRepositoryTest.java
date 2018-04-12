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
import uk.ac.ebi.ampt2d.commons.accession.core.AccessionModel;
import uk.ac.ebi.ampt2d.test.TestModel;
import uk.ac.ebi.ampt2d.test.configuration.TestJpaDatabaseServiceTestConfiguration;
import uk.ac.ebi.ampt2d.test.persistence.TestEntity;
import uk.ac.ebi.ampt2d.test.persistence.TestRepository;

import java.time.LocalDateTime;
import java.util.HashSet;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {TestJpaDatabaseServiceTestConfiguration.class})
public class BaseJpaAccessionedObjectRepositoryTest {

    public static final TestEntity ENTITY = new TestEntity(AccessionModel.of("a1", "h1",
            TestModel.of("something1")));

    @Autowired
    private TestRepository repository;

    @Test
    public void testSaveInitializesActiveFlag() {
        TestEntity savedEntity = repository.save(ENTITY);
        assertTrue(savedEntity.isActive());
    }

    @Test
    public void testSaveInitializesCreatedDate() {
        LocalDateTime beforeSave = LocalDateTime.now();
        TestEntity savedEntity = repository.save(ENTITY);
        assertTrue(beforeSave.isBefore(savedEntity.getCreatedDate()));
    }

    @Test
    public void testSave() {
        TestEntity savedEntity = repository.save(ENTITY);
        assertEquals("a1", savedEntity.getAccession());
        assertEquals("h1", savedEntity.getHashedMessage());
        assertEquals("something1", savedEntity.getSomething());
    }

    @Test
    public void testEnableEntitiesByHash() {
        TestEntity savedEntity = repository.save(new TestEntity("a1", "h1", false, "something1"));
        assertFalse(savedEntity.isActive());
        HashSet<String> hashes = new HashSet<>();
        hashes.add("h1");
        repository.enableByHashedMessageIn(hashes);
        long count = repository.count();
        TestEntity dbEntity = repository.findOne("a1");
        assertTrue(dbEntity.isActive());
    }

}
