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

import uk.ac.ebi.ampt2d.commons.accession.persistence.DatabaseService;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DeprecatingAccessionService<MODEL, HASH, ACCESSION> {

    private DatabaseService<MODEL, HASH, ACCESSION> dbService;

    private Consumer<MODEL> deprecateAccessionFunction;

    public DeprecatingAccessionService(DatabaseService<MODEL, HASH, ACCESSION> dbService, Consumer<MODEL>
            deprecateAccessionFunction) {
        this.dbService = dbService;
        this.deprecateAccessionFunction = deprecateAccessionFunction;
    }

    public void deprecateAccessions(List<ACCESSION> accesions) {
        Map<ACCESSION, MODEL> mapOfAccessionToModels = dbService.findAllAccessionMappingsByAccessions(accesions);
        mapOfAccessionToModels.values().forEach(entity -> deprecateAccessionFunction.accept(entity));
    }
}
