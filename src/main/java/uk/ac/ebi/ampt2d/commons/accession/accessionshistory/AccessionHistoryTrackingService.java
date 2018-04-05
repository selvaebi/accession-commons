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
package uk.ac.ebi.ampt2d.commons.accession.accessionshistory;

import uk.ac.ebi.ampt2d.commons.accession.accessionshistory.persistence.AccessionHistoryRepository;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AccessionHistoryTrackingService<ENTITY extends AccessionHistoryModel, ACCESSION extends Serializable> {

    private AccessionHistoryRepository<ENTITY, ACCESSION> accessionHistoryRepository;

    private Function<AccessionStatusReason<ACCESSION>, ENTITY> toAccessionHistoryEntity;

    public AccessionHistoryTrackingService(AccessionHistoryRepository<ENTITY, ACCESSION> accessionHistoryRepository, Function<AccessionStatusReason<ACCESSION>, ENTITY> toAccessionHistoryEntity) {
        this.accessionHistoryRepository = accessionHistoryRepository;
        this.toAccessionHistoryEntity = toAccessionHistoryEntity;
    }

    public void trackAccessionHistory(List<ACCESSION> accessions, AccessionStatus accessionStatus, String
            reason) {
        accessionHistoryRepository.save(accessions.stream().map(accession -> toAccessionHistoryEntity.apply
                (AccessionStatusReason.of(accession,
                        accessionStatus, reason))).collect(Collectors.toSet()));
    }
}
