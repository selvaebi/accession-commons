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
package uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.ampt2d.commons.accession.persistence.IAccessionedObjectCustomRepository;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.entities.AccessionedEntity;

import javax.persistence.EntityManager;
import java.util.Collection;

public abstract class BasicJpaAccessionedObjectCustomRepositoryImpl<ENTITY extends AccessionedEntity<?>>
        implements IAccessionedObjectCustomRepository<ENTITY> {

    private JpaEntityInformation<ENTITY, ?> entityInformation;

    private EntityManager entityManager;

    public BasicJpaAccessionedObjectCustomRepositoryImpl(Class<ENTITY> entityClass, EntityManager entityManager) {
        entityInformation = JpaEntityInformationSupport.getEntityInformation(entityClass, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void insert(Collection<ENTITY> entities) {
        for (ENTITY entity : entities) {
            entityManager.persist(entity);
        }
    }

}
