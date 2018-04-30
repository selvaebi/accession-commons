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

import uk.ac.ebi.ampt2d.commons.accession.core.AccessionWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.AccessioningService;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionCouldNotBeGeneratedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDoesNotExistException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionIsNotPendingException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.HashAlreadyExistsException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.MissingUnsavedAccessionsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mock service, generates accessions using in-memory data structures
 */
public class MockTestAccessioningService implements AccessioningService<BasicRestModel, String, String> {

    private HashMap<String, AccessionWrapper<BasicRestModel, String, String>> hashToObject;
    private HashMap<String, List<AccessionWrapper<BasicRestModel, String, String>>> accessionIndex;

    public MockTestAccessioningService() {
        this.hashToObject = new HashMap<>();
        this.accessionIndex = new HashMap<>();
    }

    @Override
    public List<AccessionWrapper<BasicRestModel, String, String>> getOrCreateAccessions(
            List<? extends BasicRestModel> messages)
            throws AccessionCouldNotBeGeneratedException {
        for (BasicRestModel message : messages) {
            generateAccession(message);
        }
        return getAccessions(messages);
    }

    private synchronized void generateAccession(BasicRestModel model) throws AccessionCouldNotBeGeneratedException {
        if (model.getValue().contains("MissingUnsavedAccessionsException")) {
            throw new MissingUnsavedAccessionsException(new ArrayList<>(), new ArrayList<>());
        }
        if (model.getValue().contains("AccessionIsNotPendingException")) {
            throw new AccessionIsNotPendingException(-1);
        }
        if (model.getValue().contains("AccessionCouldNotBeGeneratedException")) {
            throw new AccessionCouldNotBeGeneratedException("Test");
        }
        String hash = getHash(model);
        String accession = "Accession-" + accessionIndex.size();
        if (!hashToObject.containsKey(hash)) {
            AccessionWrapper<BasicRestModel, String, String> wrappedObject =
                    new AccessionWrapper<>(accession, hash, model);
            put(wrappedObject);
        }
    }

    private void put(AccessionWrapper<BasicRestModel, String, String> object) {
        hashToObject.put(object.getHash(), object);
        if (!accessionIndex.containsKey(object.getAccession())) {
            accessionIndex.put(object.getAccession(), new ArrayList<>());
        }
        accessionIndex.get(object.getAccession()).add(object);
    }

    private String getHash(BasicRestModel model) {
        return "hash-" + model.getValue();
    }

    @Override
    public List<AccessionWrapper<BasicRestModel, String, String>> getAccessions(
            List<? extends BasicRestModel> objects) {
        return objects.stream()
                .map(this::getHash)
                .filter(hashToObject::containsKey)
                .map(hashToObject::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccessionWrapper<BasicRestModel, String, String>> getByAccessions(List<String> accessions,
                                                                                  boolean hideDeprecated) {
        return accessions.stream()
                .filter(accessionIndex::containsKey)
                .map(accessionIndex::get)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public AccessionWrapper<BasicRestModel, String, String> update(String accession, BasicRestModel message)
            throws AccessionDoesNotExistException, HashAlreadyExistsException {
        if (!accessionIndex.containsKey(accession)) {
            throw new AccessionDoesNotExistException(accession);
        }

        final String hash = getHash(message);
        if (hashToObject.containsKey(hash)) {
            throw new HashAlreadyExistsException(message.getValue(), BasicRestModel.class);
        }

        int version = accessionIndex.get(accession).size() + 1;

        AccessionWrapper<BasicRestModel, String, String> wrappedObject =
                new AccessionWrapper<>(accession, hash, message, version, true);
        put(wrappedObject);

        return wrappedObject;
    }

    @Override
    public List<AccessionWrapper<BasicRestModel, String, String>> getByAccessionAndVersion(String accessions,
                                                                                           int version) {
        if (accessionIndex.containsKey(accessions)) {
            return accessionIndex.get(accessions).stream()
                    .filter(wrapper -> wrapper.getVersion() == version)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

}
