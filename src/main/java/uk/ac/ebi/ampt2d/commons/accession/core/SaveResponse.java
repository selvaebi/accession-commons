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

import java.util.Map;

public class SaveResponse<ACCESSION, MODEL> {

    private final Map<ACCESSION, MODEL> savedAccessions;
    private final Map<ACCESSION, MODEL> unsavedAccessions;

    public SaveResponse(Map<ACCESSION, MODEL> savedAccessions,
                        Map<ACCESSION, MODEL> unsavedAccessions) {
        this.savedAccessions = savedAccessions;
        this.unsavedAccessions = unsavedAccessions;
    }

    public Map<ACCESSION, MODEL> getSavedAccessions() {
        return savedAccessions;
    }

    public Map<ACCESSION, MODEL> getUnsavedAccessions() {
        return unsavedAccessions;
    }

}
