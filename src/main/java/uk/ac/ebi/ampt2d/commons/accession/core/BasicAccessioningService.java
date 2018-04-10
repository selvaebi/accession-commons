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

import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionCouldNotBeGeneratedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.MissingUnsavedAccessions;
import uk.ac.ebi.ampt2d.commons.accession.generators.AccessionGenerator;
import uk.ac.ebi.ampt2d.commons.accession.generators.ModelHashAccession;
import uk.ac.ebi.ampt2d.commons.accession.persistence.DatabaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A service that provides accessions for objects
 *
 * @param <MODEL>
 * @param <ACCESSION>
 * @param <HASH>
 */
public class BasicAccessioningService<MODEL, HASH, ACCESSION> implements AccessioningService<MODEL, ACCESSION> {

    private final BasicAccessioningServiceSaveDelegate<MODEL, HASH, ACCESSION> basicAccessioningServiceSaveDelegate;

    private AccessionGenerator<MODEL, ACCESSION> accessionGenerator;

    private DatabaseService<MODEL, HASH, ACCESSION> dbService;

    private final Function<MODEL, String> summaryFunction;

    private final Function<String, HASH> hashingFunction;

    public BasicAccessioningService(AccessionGenerator<MODEL, ACCESSION> accessionGenerator,
                                    DatabaseService<MODEL, HASH, ACCESSION> dbService,
                                    Function<MODEL, String> summaryFunction,
                                    Function<String, HASH> hashingFunction) {
        this.accessionGenerator = accessionGenerator;
        this.dbService = dbService;
        this.summaryFunction = summaryFunction;
        this.hashingFunction = hashingFunction;
        this.basicAccessioningServiceSaveDelegate = new BasicAccessioningServiceSaveDelegate<>(dbService);
    }

    /**
     * Get accessions for a list of messages. It looks for the object's accessions in a repository, and if they don't
     * exist, generate new ones, storing them in the repository
     *
     * @param messages
     * @return
     */
    @Override
    public Map<ACCESSION, MODEL> getOrCreateAccessions(List<? extends MODEL> messages)
            throws AccessionCouldNotBeGeneratedException {
        Map<HASH, MODEL> hashToMessages = mapHashOfMessages(messages);
        Map<HASH, ACCESSION> existingAccessions = dbService.getExistingAccessions(hashToMessages.keySet());
        Map<HASH, MODEL> newMessages = filterNotExistingAccessions(hashToMessages, existingAccessions);

        Map<ACCESSION, MODEL> accessions = joinExistingAccessionsWithMessages(existingAccessions, hashToMessages);
        if (!newMessages.isEmpty()) {
            accessions.putAll(saveAccessions(accessionGenerator.generateAccessions(newMessages)));
        }
        return accessions;
    }

    /**
     * Digest messages, hash them and map them. If Two messages have the same hash keep the first one.
     *
     * @param messages
     * @return
     */
    private Map<HASH, MODEL> mapHashOfMessages(List<? extends MODEL> messages) {
        return messages.stream()
                .collect(Collectors.toMap(summaryFunction.andThen(hashingFunction), e -> e, (r, o) -> r));
    }

    private Map<HASH, MODEL> filterNotExistingAccessions(Map<HASH, MODEL> hashToMessages,
                                                         Map<HASH, ACCESSION> existingAccessions) {
        return hashToMessages.entrySet().stream()
                .filter(entry -> !existingAccessions.containsKey(entry.getKey()))
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
    }

    private Map<ACCESSION, MODEL> joinExistingAccessionsWithMessages(Map<HASH, ACCESSION> existingAccessions,
                                                                     Map<HASH, MODEL> hashToMessages) {
        return existingAccessions.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getValue(), e -> hashToMessages.get(e.getKey())));
    }

    /**
     * Execute {@link BasicAccessioningServiceSaveDelegate#doSaveAccessions(List)} This operation will generate two
     * lists on {@link SaveResponse} saved elements and not saved elements. Not saved elements are elements that
     * could not be stored on database due to constraint exceptions. This should only happen when elements have been
     * already stored by another application instance / thread with a different id.
     * See {@link #getUnsavedAccessions(SaveResponse)} for more details.
     *
     * @param accessions
     * @return
     */
    private Map<ACCESSION, MODEL> saveAccessions(List<ModelHashAccession<MODEL, HASH, ACCESSION>> accessions) {
        SaveResponse<ACCESSION, MODEL> response = basicAccessioningServiceSaveDelegate.doSaveAccessions(accessions);
        accessionGenerator.postSave(response);
        Map<ACCESSION, MODEL> savedAccessions = response.getSavedAccessions();
        if (!response.getUnsavedAccessions().isEmpty()) {
            savedAccessions.putAll(getUnsavedAccessions(response));
        }
        return savedAccessions;
    }

    /**
     * We try to recover all elements that could not be saved to return their accession to the user. This is only
     * expected when another application instance or thread has saved that element already with a different id. If
     * all the elements could not be retrieved from the database it is considered an unexpected event and we throw
     * {@link MissingUnsavedAccessions} to alert the system.
     *
     * @param response
     * @return
     */
    private Map<ACCESSION, MODEL> getUnsavedAccessions(SaveResponse<ACCESSION, MODEL> response) {
        List<MODEL> unsavedObjects = new ArrayList<>(response.getUnsavedAccessions().values());
        final Map<ACCESSION, MODEL> unsavedObjectAccessions = getAccessions(unsavedObjects);
        if (unsavedObjectAccessions.size() != unsavedObjects.size()) {
            throw new MissingUnsavedAccessions(unsavedObjectAccessions, unsavedObjects);
        }
        return unsavedObjectAccessions;
    }

    @Override
    public Map<ACCESSION, MODEL> getAccessions(List<? extends MODEL> accessionedObjects) {
        return dbService.findAllAccessionsByHash(getHashes(accessionedObjects));
    }

    @Override
    public Map<ACCESSION, MODEL> getByAccessions(List<ACCESSION> accessions) {
        return dbService.findAllAccessionMappingsByAccessions(accessions);
    }

    private List<HASH> getHashes(List<? extends MODEL> accessionObjects) {
        return accessionObjects.stream().map(summaryFunction.andThen(hashingFunction)).collect(Collectors.toList());
    }

    protected AccessionGenerator<MODEL, ACCESSION> getAccessionGenerator() {
        return accessionGenerator;
    }

    protected DatabaseService<MODEL, HASH, ACCESSION> getDbService() {
        return dbService;
    }

}
