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
package uk.ac.ebi.ampt2d.test.rest;

import uk.ac.ebi.ampt2d.commons.accession.core.AccessioningService;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionCouldNotBeGeneratedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionIsNotPending;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.MissingUnsavedAccessions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mock service, generates accessions and maintains accessions in memory.
 */
public class MockTestAccessioningService implements AccessioningService<BasicRestModel, String> {

    private HashMap<String, BasicRestModel> accessionToObject;
    private HashMap<String, String> valueToAccession;

    public MockTestAccessioningService() {
        this.accessionToObject = new HashMap<>();
        this.valueToAccession = new HashMap<>();
    }

    @Override
    public Map<String, BasicRestModel> getOrCreateAccessions(List<? extends BasicRestModel> messages)
            throws AccessionCouldNotBeGeneratedException {
        for (BasicRestModel message : messages) {
            generateAccession(message);
        }
        return messages.stream().collect(Collectors.toMap(
                o -> valueToAccession.get(o.getValue()),
                o -> o
        ));
    }

    private synchronized void generateAccession(BasicRestModel model) throws AccessionCouldNotBeGeneratedException {
        if (model.getValue().contains("MissingUnsavedAccessions")) {
            throw new MissingUnsavedAccessions(new HashMap<>(), new ArrayList<>());
        }
        if (model.getValue().contains("AccessionIsNotPending")) {
            throw new AccessionIsNotPending(-1);
        }
        if (model.getValue().contains("AccessionCouldNotBeGeneratedException")) {
            throw new AccessionCouldNotBeGeneratedException("Test");
        }
        String accession = "Accession-" + accessionToObject.size();
        accessionToObject.put(accession, model);
        valueToAccession.put(model.getValue(), accession);
    }

    @Override
    public Map<String, BasicRestModel> getAccessions(List<? extends BasicRestModel> accessionedObjects) {
        return accessionedObjects.stream().filter(o -> valueToAccession.containsKey(o.getValue()))
                .collect(Collectors.toMap(o -> valueToAccession.get(o.getValue()), o -> o));
    }

    @Override
    public Map<String, ? extends BasicRestModel> getByAccessions(List<String> strings) {
        return strings.stream().filter(accessionToObject::containsKey)
                .collect(Collectors.toMap(o -> o, o -> accessionToObject.get(o)));
    }

}
