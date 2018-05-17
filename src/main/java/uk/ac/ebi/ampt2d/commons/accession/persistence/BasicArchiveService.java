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
import uk.ac.ebi.ampt2d.commons.accession.core.AccessionWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.ModelWrapper;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.accession.entities.AccessionedEntity;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.accession.entities.ArchivedAccessionEntity;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.accession.entities.OperationEntity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BasicArchiveService<
        MODEL,
        ACCESSION extends Serializable,
        ACCESSION_ENTITY extends AccessionedEntity<ACCESSION>,
        ACCESSION_ARCHIVE_ENTITY extends ArchivedAccessionEntity<ACCESSION>,
        OPERATION_ENTITY extends OperationEntity<ACCESSION>>
        implements ArchiveService<MODEL, String, ACCESSION, ACCESSION_ENTITY> {

    private final IAccessionArchiveRepository<ACCESSION, ACCESSION_ARCHIVE_ENTITY> accessionArchiveRepository;

    private final Function<ACCESSION_ENTITY, ACCESSION_ARCHIVE_ENTITY> toArchiveEntity;

    private final IHistoryRepository<ACCESSION, OPERATION_ENTITY, Long> historyRepository;

    private final Supplier<OPERATION_ENTITY> historyEntitySupplier;

    private final Function<ACCESSION_ARCHIVE_ENTITY, MODEL> toModelFunction;

    public BasicArchiveService(IAccessionArchiveRepository<ACCESSION, ACCESSION_ARCHIVE_ENTITY>
                                       accessionArchiveRepository,
                               Function<ACCESSION_ENTITY, ACCESSION_ARCHIVE_ENTITY> toArchiveEntity,
                               IHistoryRepository<ACCESSION, OPERATION_ENTITY, Long> historyRepository,
                               Supplier<OPERATION_ENTITY> historyEntitySupplier,
                               Function<ACCESSION_ARCHIVE_ENTITY, MODEL> toModelFunction) {
        this.accessionArchiveRepository = accessionArchiveRepository;
        this.toArchiveEntity = toArchiveEntity;
        this.historyRepository = historyRepository;
        this.historyEntitySupplier = historyEntitySupplier;
        this.toModelFunction = toModelFunction;
    }

    @Override
    public void archiveVersion(ACCESSION_ENTITY entitiy, String reason) {
        OPERATION_ENTITY operation = generateUpdateOperation(entitiy.getAccession(), reason);
        doArchive(Arrays.asList(entitiy), operation);
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


    private void doArchive(Collection<ACCESSION_ENTITY> accessionedElements, OPERATION_ENTITY operation) {
        historyRepository.save(operation);
        final List<ACCESSION_ARCHIVE_ENTITY> archiveEntities = accessionedElements.stream().map(toArchiveEntity)
                .collect(Collectors.toList());
        archiveEntities.forEach(entity -> entity.setHistoryId(operation.getId()));
        accessionArchiveRepository.save(archiveEntities);
    }

    @Override
    public void archiveDeprecation(ACCESSION accession, Collection<ACCESSION_ENTITY> entities, String reason) {
        OPERATION_ENTITY operation = generateDeprecationOperation(accession, reason);
        doArchive(entities, operation);
    }

    private OPERATION_ENTITY generateDeprecationOperation(ACCESSION accession, String reason) {
        return generateOperation(OperationType.DEPRECATED, accession, null, reason);
    }

    @Override
    public AccessionWrapper<MODEL, String, ACCESSION> findByAccessionAndVersion(ACCESSION accession, int version) {
        final List<ModelWrapper<MODEL, String, ACCESSION>> result =
                accessionArchiveRepository.findAllByAccessionAndVersion(accession, version).stream()
                        .map(this::toModelWrapper)
                        .collect(Collectors.toList());
        if(result.isEmpty()){
            return null;
        }
        return new AccessionWrapper<>(result);
    }

    @Override
    public IArchiveOperation<ACCESSION> getLastOperation(ACCESSION accession) {
        return historyRepository.findByAccessionIdOriginOrderByCreatedDateDesc(accession);
    }

    private ModelWrapper<MODEL, String, ACCESSION> toModelWrapper(ACCESSION_ARCHIVE_ENTITY entity) {
        return new ModelWrapper<>(entity.getAccession(), entity.getHashedMessage(), toModelFunction.apply(entity),
                entity.getVersion());
    }

}
