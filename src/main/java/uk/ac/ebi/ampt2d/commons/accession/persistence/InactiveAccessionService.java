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
package uk.ac.ebi.ampt2d.commons.accession.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.ampt2d.commons.accession.core.AccessionVersionsWrapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface InactiveAccessionService<
        MODEL,
        HASH,
        ACCESSION extends Serializable,
        ACCESSION_ENTITY extends IAccessionedObject<ACCESSION>> {

    @Transactional
    void update(ACCESSION_ENTITY entity, String reason);

    @Transactional
    void deprecate(ACCESSION accession, Collection<ACCESSION_ENTITY> entities, String reason);

    AccessionVersionsWrapper<MODEL, HASH, ACCESSION> findByAccessionAndVersion(ACCESSION accession, int version);

    InactiveOperation<ACCESSION> getLastOperation(ACCESSION accession);

    @Transactional
    void archiveMerge(ACCESSION accessionOrigin, ACCESSION accessionDestiny, List<ACCESSION_ENTITY> entities,
                      HASH reason);

}
