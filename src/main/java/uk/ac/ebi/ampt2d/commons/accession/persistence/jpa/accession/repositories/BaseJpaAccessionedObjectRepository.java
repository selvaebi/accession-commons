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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.ampt2d.commons.accession.persistence.IAccessionedObjectRepository;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.accession.entities.AccessionedEntity;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

public class BaseJpaAccessionedObjectRepository<ENTITY extends AccessionedEntity<ACCESSION>,
        ACCESSION extends Serializable> extends SimpleJpaRepository<ENTITY, ACCESSION>
        implements IAccessionedObjectRepository<ENTITY, ACCESSION> {

    private final static Logger logger = LoggerFactory.getLogger(BaseJpaAccessionedObjectRepository.class);

    private final JpaEntityInformation entityInformation;

    private final EntityManager entityManager;

    BaseJpaAccessionedObjectRepository(JpaEntityInformation entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityInformation = entityInformation;
        this.entityManager = entityManager;
    }

    @Override
    public Collection<ENTITY> findByHashedMessageIn(Collection<String> hashes) {
        final Class javaType = entityInformation.getJavaType();
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ENTITY> criteriaQuery = builder.createQuery(javaType);
        Root<ENTITY> root = criteriaQuery.from(javaType);
        ParameterExpression<Collection> parameter = builder.parameter(Collection.class);
        criteriaQuery.where(root.<String>get("hashedMessage").in(parameter));
        TypedQuery<ENTITY> query = entityManager.createQuery(criteriaQuery);
        query.setParameter(parameter, hashes);
        return query.getResultList();
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

}
