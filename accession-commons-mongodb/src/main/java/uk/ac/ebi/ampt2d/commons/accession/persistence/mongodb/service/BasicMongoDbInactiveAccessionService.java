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
package uk.ac.ebi.ampt2d.commons.accession.persistence.mongodb.service;

import uk.ac.ebi.ampt2d.commons.accession.core.OperationType;
import uk.ac.ebi.ampt2d.commons.accession.persistence.BasicInactiveAccessionService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.IAccessionedObject;
import uk.ac.ebi.ampt2d.commons.accession.persistence.IHistoryRepository;
import uk.ac.ebi.ampt2d.commons.accession.persistence.mongodb.document.InactiveSubDocument;
import uk.ac.ebi.ampt2d.commons.accession.persistence.mongodb.document.OperationDocument;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class BasicMongoDbInactiveAccessionService<
        ACCESSION extends Serializable,
        ACCESSION_ENTITY extends IAccessionedObject<ACCESSION>,
        ACCESSION_INACTIVE_ENTITY extends InactiveSubDocument<ACCESSION>,
        OPERATION_ENTITY extends OperationDocument<ACCESSION, ACCESSION_INACTIVE_ENTITY>>
        extends BasicInactiveAccessionService<ACCESSION, ACCESSION_ENTITY, ACCESSION_INACTIVE_ENTITY,
        OPERATION_ENTITY> {

    private Supplier<OPERATION_ENTITY> supplier;

    public BasicMongoDbInactiveAccessionService(
            IHistoryRepository<ACCESSION, OPERATION_ENTITY, String> historyRepository,
            Function<ACCESSION_ENTITY, ACCESSION_INACTIVE_ENTITY> toInactiveEntity,
            Supplier<OPERATION_ENTITY> supplier) {
        super(historyRepository, toInactiveEntity);
        this.supplier = supplier;
    }

    @Override
    protected void doSaveHistory(OperationType type, ACCESSION origin, ACCESSION destination, String reason,
                                 List<ACCESSION_INACTIVE_ENTITY> entities) {
        OPERATION_ENTITY operation = supplier.get();
        operation.fill(type, origin, destination, reason, entities);
        historyRepository.save(operation);
    }

}
