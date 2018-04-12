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

import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionWasNotSaved;

import java.util.HashSet;
import java.util.Set;

public class SaveResponse<ACCESSION> {

    private final Set<ACCESSION> savedAccessions;
    private final Set<ACCESSION> saveFailedAccessions;

    public SaveResponse() {
        this(new HashSet<>(), new HashSet<>());
    }

    public SaveResponse(Set<ACCESSION> savedAccessions, Set<ACCESSION> saveFailedAccessions) {
        this.savedAccessions = savedAccessions;
        this.saveFailedAccessions = saveFailedAccessions;
    }

    public Set<ACCESSION> getSavedAccessions() {
        return savedAccessions;
    }

    public Set<ACCESSION> getSaveFailedAccessions() {
        return saveFailedAccessions;
    }

    public void addSavedAccessions(AccessionModel<?, ?, ACCESSION> accessionModel) {
        savedAccessions.add(accessionModel.getAccession());
    }

    public void addSaveFailedAccession(AccessionModel<?, ?, ACCESSION> accessionModel) {
        saveFailedAccessions.add(accessionModel.getAccession());
    }

    public boolean isSavedAccession(AccessionModel<?, ?, ACCESSION> accessionModel) {
        if(savedAccessions.contains(accessionModel.getAccession())){
            return true;
        }else{
            if(saveFailedAccessions.contains(accessionModel.getAccession())){
                return false;
            }else{
                throw new AccessionWasNotSaved(accessionModel.getAccession());
            }
        }
    }
}
