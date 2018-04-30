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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.ampt2d.commons.accession.persistence.IAccessionedObjectCustomRepository;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.accession.entities.AccessionedEntity;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collection;
import java.util.Set;

public abstract class BasicJpaAccessionedObjectCustomRepositoryImpl<ENTITY extends AccessionedEntity<?>>
        implements IAccessionedObjectCustomRepository {

    private final static Logger logger = LoggerFactory.getLogger(BasicJpaAccessionedObjectCustomRepositoryImpl.class);

    private JpaEntityInformation<ENTITY, ?> entityInformation;

    private EntityManager entityManager;

    public BasicJpaAccessionedObjectCustomRepositoryImpl(Class<ENTITY> entityClass, EntityManager entityManager) {
        entityInformation = JpaEntityInformationSupport.getEntityInformation(entityClass, entityManager);
        this.entityManager = entityManager;
    }

    @Transactional
    @Override
    public void enableByHashedMessageIn(Set<String> hashes) {
        String entity = entityInformation.getEntityName();
        Query query = entityManager.createQuery(
                "UPDATE " + entity + " SET active=true WHERE hashedMessage in :hashes ");
        query.setParameter("hashes", hashes);
        logger.info(Integer.toString(query.executeUpdate()));
        entityManager.clear();
    }

    @Override
    @Transactional
    public <ENTITY> void insert(Collection<ENTITY> entities) {
        for (ENTITY entity : entities) {
            entityManager.persist(entity);
        }
    }

}
