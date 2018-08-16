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
package uk.ac.ebi.ampt2d.commons.accession.persistence.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import uk.ac.ebi.ampt2d.commons.accession.persistence.models.IAccessionedObject;
import uk.ac.ebi.ampt2d.commons.accession.persistence.repositories.IAccessionedObjectCustomRepository;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@NoRepositoryBean
public interface IAccessionedObjectRepository<
        ENTITY extends IAccessionedObject<?, String, ACCESSION>,
        ACCESSION extends Serializable> extends CrudRepository<ENTITY, String>,
        IAccessionedObjectCustomRepository<ACCESSION, ENTITY> {

    List<ENTITY> findByAccession(ACCESSION accession);

    List<ENTITY> findByAccessionIn(Collection<ACCESSION> accessions);

    ENTITY findByAccessionAndVersion(ACCESSION accession, int version);

}
