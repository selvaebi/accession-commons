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

import uk.ac.ebi.ampt2d.commons.accession.core.OperationType;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.ac.ebi.ampt2d.commons.accession.core.OperationType.DEPRECATED;
import static uk.ac.ebi.ampt2d.commons.accession.core.OperationType.MERGED_INTO;
import static uk.ac.ebi.ampt2d.commons.accession.core.OperationType.UPDATED;

public abstract class BasicInactiveAccessionService<
        ACCESSION extends Serializable,
        ACCESSION_ENTITY extends IAccessionedObject<ACCESSION>,
        ACCESSION_INACTIVE_ENTITY extends IAccessionedObject<ACCESSION>,
        OPERATION_ENTITY extends IOperation<ACCESSION>
        >
        implements InactiveAccessionService<String, ACCESSION, ACCESSION_ENTITY> {

    protected IHistoryRepository<ACCESSION, OPERATION_ENTITY, ?> historyRepository;
    private Function<ACCESSION_ENTITY, ACCESSION_INACTIVE_ENTITY> toInactiveEntity;

    public BasicInactiveAccessionService(
            IHistoryRepository<ACCESSION, OPERATION_ENTITY, ?> historyRepository,
            Function<ACCESSION_ENTITY, ACCESSION_INACTIVE_ENTITY> toInactiveEntity) {
        this.historyRepository = historyRepository;
        this.toInactiveEntity = toInactiveEntity;
    }

    @Override
    public void update(ACCESSION_ENTITY entity, String reason) {
        saveHistory(UPDATED, entity.getAccession(), reason, Arrays.asList(toInactiveEntity.apply(entity)));
    }

    private void saveHistory(OperationType type, ACCESSION accession, String reason,
                             List<ACCESSION_INACTIVE_ENTITY> entities) {
        saveHistory(type, accession, null, reason, entities);
    }

    @Override
    public void deprecate(ACCESSION accession, Collection<ACCESSION_ENTITY> accessionEntities, String reason) {
        saveHistory(DEPRECATED, accession, reason, toInactiveEntities(accessionEntities));
    }

    private List<ACCESSION_INACTIVE_ENTITY> toInactiveEntities(Collection<ACCESSION_ENTITY> accessionEntities) {
        return accessionEntities.stream().map(toInactiveEntity).collect(Collectors.toList());
    }

    @Override
    public IOperation<ACCESSION> getLastOperation(ACCESSION accession) {
        return historyRepository.findByAccessionIdOriginOrderByCreatedDateDesc(accession);
    }

    @Override
    public void merge(ACCESSION accessionOrigin, ACCESSION accessionDestination,
                      List<ACCESSION_ENTITY> accession_entities, String reason) {
        saveHistory(MERGED_INTO, accessionOrigin, accessionDestination, reason,
                toInactiveEntities(accession_entities));
    }

    protected abstract void saveHistory(OperationType type, ACCESSION origin, ACCESSION destination,
                                        String reason, List<ACCESSION_INACTIVE_ENTITY> entities);
}
