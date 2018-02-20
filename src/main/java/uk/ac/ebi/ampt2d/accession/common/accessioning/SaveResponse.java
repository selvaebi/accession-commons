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
package uk.ac.ebi.ampt2d.accession.common.accessioning;

import java.util.HashMap;
import java.util.Map;

public class SaveResponse<ACCESSION, MODEL> {

    private final Map<ACCESSION, MODEL> savedAccessions;
    private final Map<ACCESSION, MODEL> unsavedAccessions;
    private final Map<ACCESSION, MODEL> accessionOfUnsavedMessages;

    public SaveResponse(Map<ACCESSION, MODEL> savedAccessions,
                        Map<ACCESSION, MODEL> unsavedAccessions,
                        Map<ACCESSION, MODEL> accessionOfUnsavedMessages) {
        this.savedAccessions = savedAccessions;
        this.unsavedAccessions = unsavedAccessions;
        this.accessionOfUnsavedMessages = accessionOfUnsavedMessages;
    }

    public Map<ACCESSION, MODEL> getSavedAccessions() {
        return savedAccessions;
    }

    public Map<ACCESSION, MODEL> getUnsavedAccessions() {
        return unsavedAccessions;
    }

    public Map<ACCESSION, MODEL> getAccessionOfUnsavedMessages() {
        return accessionOfUnsavedMessages;
    }

    public Map<ACCESSION, MODEL> getAllAccessionToMessage() {
        Map<ACCESSION, MODEL> allAccessionToMessage = new HashMap<>(savedAccessions);
        allAccessionToMessage.putAll(accessionOfUnsavedMessages);
        return allAccessionToMessage;
    }
}
