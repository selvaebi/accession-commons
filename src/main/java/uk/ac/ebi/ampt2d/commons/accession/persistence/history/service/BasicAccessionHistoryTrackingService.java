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
package uk.ac.ebi.ampt2d.commons.accession.persistence.history.service;

import uk.ac.ebi.ampt2d.commons.accession.core.AccessionStatus;
import uk.ac.ebi.ampt2d.commons.accession.persistence.history.repositories.AccessionHistoryRepository;

import java.util.ArrayList;
import java.util.List;

public class BasicAccessionHistoryTrackingService<ENTITY, ACCESSION>
        implements IAccessionHistoryTrackingService<ACCESSION> {

    private AccessionHistoryRepository<ENTITY, ?> accessionHistoryRepository;
    private IAccessionHistoryBuilder<ENTITY, ACCESSION> builder;

    public BasicAccessionHistoryTrackingService(AccessionHistoryRepository<ENTITY, ?> accessionHistoryRepository,
                                                IAccessionHistoryBuilder<ENTITY, ACCESSION> builder) {
        this.accessionHistoryRepository = accessionHistoryRepository;
        this.builder = builder;
    }

    private void accessionStatus(List<ACCESSION> accessions, AccessionStatus accessionStatus, String reason) {
        List<ENTITY> entities = new ArrayList<>();
        accessions.stream().forEach(accession -> entities.add(builder.build(accession, accessionStatus, reason)));
        accessionHistoryRepository.save(entities);
    }

    @Override
    public void merge(List<ACCESSION> accessions, String reason) {
        accessionStatus(accessions, AccessionStatus.MERGED, reason);
    }

    @Override
    public void update(List<ACCESSION> accessions, String reason) {
        accessionStatus(accessions, AccessionStatus.UPDATED, reason);
    }

    @Override
    public void deprecate(List<ACCESSION> accessions, String reason) {
        accessionStatus(accessions, AccessionStatus.DEPRECATED, reason);
    }

}
