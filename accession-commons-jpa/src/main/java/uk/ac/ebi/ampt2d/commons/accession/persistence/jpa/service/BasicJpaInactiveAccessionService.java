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
package uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.service;

import uk.ac.ebi.ampt2d.commons.accession.core.OperationType;
import uk.ac.ebi.ampt2d.commons.accession.persistence.BasicInactiveAccessionService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.IAccessionedObject;
import uk.ac.ebi.ampt2d.commons.accession.persistence.IHistoryRepository;
import uk.ac.ebi.ampt2d.commons.accession.persistence.InactiveAccessionRepository;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.entities.InactiveAccessionEntity;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.entities.OperationEntity;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class BasicJpaInactiveAccessionService<
        ACCESSION extends Serializable,
        ACCESSION_ENTITY extends IAccessionedObject<ACCESSION>,
        ACCESSION_INACTIVE_ENTITY extends InactiveAccessionEntity<ACCESSION>,
        OPERATION_ENTITY extends OperationEntity<ACCESSION>>
        extends
        BasicInactiveAccessionService<ACCESSION, ACCESSION_ENTITY, ACCESSION_INACTIVE_ENTITY, OPERATION_ENTITY> {

    private final InactiveAccessionRepository<ACCESSION, ACCESSION_INACTIVE_ENTITY> inactiveAccessionRepository;

    private final Supplier<OPERATION_ENTITY> historyEntitySupplier;


    public BasicJpaInactiveAccessionService(IHistoryRepository<ACCESSION, OPERATION_ENTITY, ?> historyRepository,
                                            Function<ACCESSION_ENTITY, ACCESSION_INACTIVE_ENTITY> toInactiveEntity,
                                            InactiveAccessionRepository<ACCESSION, ACCESSION_INACTIVE_ENTITY>
                                                    inactiveAccessionRepository,
                                            Supplier<OPERATION_ENTITY> historyEntitySupplier) {
        super(historyRepository, toInactiveEntity);
        this.inactiveAccessionRepository = inactiveAccessionRepository;
        this.historyEntitySupplier = historyEntitySupplier;
    }

    @Override
    protected void doSaveHistory(OperationType type, ACCESSION origin, ACCESSION destination, String reason,
                                 List<ACCESSION_INACTIVE_ENTITY> accessionInactiveEntities) {
        OPERATION_ENTITY operation = historyEntitySupplier.get();
        operation.fill(type, origin, destination, reason);
        historyRepository.save(operation);
        accessionInactiveEntities.forEach(entity -> entity.setHistoryId(operation.getId()));
        inactiveAccessionRepository.save(accessionInactiveEntities);
    }

}
