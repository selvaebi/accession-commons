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
package uk.ac.ebi.ampt2d.commons.accession.core;

import uk.ac.ebi.ampt2d.commons.accession.persistence.AccessionStatus;
import uk.ac.ebi.ampt2d.commons.accession.persistence.DatabaseService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.HistoryOfAccessionsEntity;
import uk.ac.ebi.ampt2d.commons.accession.persistence.HistoryOfAccessionsRepository;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TrackingHistoryOfAccessionsService<ACCESSION extends Serializable> {

    private DatabaseService dbService;

    private HistoryOfAccessionsRepository historyOfAccessionsRepository;

    private Consumer deprecateOrMergeFunction;

    public TrackingHistoryOfAccessionsService(DatabaseService dbService, HistoryOfAccessionsRepository historyOfAccessionsRepository, Consumer
            deprecateOrMergeFunction) {
        this.dbService = dbService;
        this.historyOfAccessionsRepository = historyOfAccessionsRepository;
        this.deprecateOrMergeFunction = deprecateOrMergeFunction;
    }

    public void createOrUpdateAccessions(List<ACCESSION> accessions, AccessionStatus accessionStatus, String reason) {
        historyOfAccessionsRepository.save(accessions.stream().map(accession -> new
                HistoryOfAccessionsEntity(accession.toString(), accessionStatus.name(),
                Date.valueOf(LocalDate.now()), reason)).collect(Collectors.toSet()));
    }

    public void deprecateOrMergeAccessions(List<ACCESSION> accessions, AccessionStatus accessionStatus, String
            reason) {
        Map<ACCESSION, ?> mapOfAccessionToModels = dbService.findAllAccessionMappingsByAccessions(accessions);
        mapOfAccessionToModels.values().forEach(entity -> deprecateOrMergeFunction.accept(entity));
        historyOfAccessionsRepository.save(accessions.stream().map(accession -> new
                HistoryOfAccessionsEntity(accession.toString(), accessionStatus.name(), Date.valueOf(LocalDate.now()),
                reason))
                .collect(Collectors.toSet()));
    }
}
