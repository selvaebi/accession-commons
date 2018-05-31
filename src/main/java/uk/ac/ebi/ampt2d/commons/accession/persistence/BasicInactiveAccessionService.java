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

import uk.ac.ebi.ampt2d.commons.accession.core.AccessionVersionsWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.AccessionWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.OperationType;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.accession.entities.OperationEntity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BasicInactiveAccessionService<
        MODEL,
        ACCESSION extends Serializable,
        ACCESSION_ENTITY extends IAccessionedObject<ACCESSION>,
        ACCESSION_INACTIVE_ENTITY extends IInactiveAccessionedObject<ACCESSION>,
        OPERATION_ENTITY extends IOperation<ACCESSION>>
        implements InactiveAccessionService<MODEL, String, ACCESSION, ACCESSION_ENTITY> {

    private final InactiveAccessionRepository<ACCESSION, ACCESSION_INACTIVE_ENTITY> inactiveAccessionRepository;

    private final Function<ACCESSION_ENTITY, ACCESSION_INACTIVE_ENTITY> toInactiveEntity;

    private final IHistoryRepository<ACCESSION, OPERATION_ENTITY, Long> historyRepository;

    private final Supplier<OPERATION_ENTITY> historyEntitySupplier;

    private final Function<ACCESSION_INACTIVE_ENTITY, MODEL> toModelFunction;

    public BasicInactiveAccessionService(InactiveAccessionRepository<ACCESSION, ACCESSION_INACTIVE_ENTITY>
                                                 inactiveAccessionRepository,
                                         Function<ACCESSION_ENTITY, ACCESSION_INACTIVE_ENTITY> toInactiveEntity,
                                         IHistoryRepository<ACCESSION, OPERATION_ENTITY, Long> historyRepository,
                                         Supplier<OPERATION_ENTITY> historyEntitySupplier,
                                         Function<ACCESSION_INACTIVE_ENTITY, MODEL> toModelFunction) {
        this.inactiveAccessionRepository = inactiveAccessionRepository;
        this.toInactiveEntity = toInactiveEntity;
        this.historyRepository = historyRepository;
        this.historyEntitySupplier = historyEntitySupplier;
        this.toModelFunction = toModelFunction;
    }

    @Override
    public void update(ACCESSION_ENTITY entity, String reason) {
        OPERATION_ENTITY operation = generateUpdateOperation(entity.getAccession(), reason);
        storeInactive(Arrays.asList(entity), operation);
    }

    private OPERATION_ENTITY generateUpdateOperation(ACCESSION accession, String reason) {
        return generateOperation(OperationType.UPDATED, accession, accession, reason);
    }

    private OPERATION_ENTITY generateOperation(OperationType status, ACCESSION origin, ACCESSION destiny,
                                               String reason) {
        OPERATION_ENTITY operation = historyEntitySupplier.get();
        operation.setOperationType(status);
        operation.setAccessionIdOrigin(origin);
        operation.setAccessionIdDestiny(destiny);
        operation.setReason(reason);
        return operation;
    }


    private void storeInactive(Collection<ACCESSION_ENTITY> accessionedElements, OPERATION_ENTITY operation) {
        historyRepository.save(operation);
        final List<ACCESSION_INACTIVE_ENTITY> inactiveEntities = accessionedElements.stream().map(toInactiveEntity)
                .collect(Collectors.toList());
        inactiveEntities.forEach(entity -> entity.setHistoryId(operation.getId()));
        inactiveAccessionRepository.save(inactiveEntities);
    }

    @Override
    public void deprecate(ACCESSION accession, Collection<ACCESSION_ENTITY> entities, String reason) {
        OPERATION_ENTITY operation = generateDeprecationOperation(accession, reason);
        storeInactive(entities, operation);
    }

    private OPERATION_ENTITY generateDeprecationOperation(ACCESSION accession, String reason) {
        return generateOperation(OperationType.DEPRECATED, accession, null, reason);
    }

    @Override
    public void merge(ACCESSION accessionOrigin, ACCESSION accessionDestination,
                      List<ACCESSION_ENTITY> entities, String reason) {
        OPERATION_ENTITY operation = generateMergeOperation(accessionOrigin, accessionDestination, reason);
        storeInactive(entities, operation);
    }

    private OPERATION_ENTITY generateMergeOperation(ACCESSION accessionOrigin, ACCESSION accessionDestination,
                                                    String reason) {
        return generateOperation(OperationType.MERGED_INTO, accessionOrigin, accessionDestination, reason);
    }

    @Override
    public AccessionVersionsWrapper<MODEL, String, ACCESSION> findByAccessionAndVersion(ACCESSION accession, int version) {
        final List<AccessionWrapper<MODEL, String, ACCESSION>> result =
                inactiveAccessionRepository.findAllByAccessionAndVersion(accession, version).stream()
                        .map(this::toModelWrapper)
                        .collect(Collectors.toList());
        if (result.isEmpty()) {
            return null;
        }
        return new AccessionVersionsWrapper<>(result);
    }

    @Override
    public IOperation<ACCESSION> getLastOperation(ACCESSION accession) {
        return historyRepository.findByAccessionIdOriginOrderByCreatedDateDesc(accession);
    }

    private AccessionWrapper<MODEL, String, ACCESSION> toModelWrapper(ACCESSION_INACTIVE_ENTITY entity) {
        return new AccessionWrapper<>(entity.getAccession(), entity.getHashedMessage(), toModelFunction.apply(entity),
                entity.getVersion());
    }

}
