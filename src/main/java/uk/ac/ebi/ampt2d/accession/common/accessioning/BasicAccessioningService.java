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

import uk.ac.ebi.ampt2d.accession.common.generators.AccessionGenerator;
import uk.ac.ebi.ampt2d.accession.common.persistence.DatabaseService;

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
    }

    /**
     * Get accessions for a list of messages. It looks for the object's accessions in a repository, and if they don't
     * exist, generate new ones, storing them in the repository
     *
     * @param messages
     * @return
     */
    @Override
    public Map<ACCESSION, MODEL> getOrCreateAccessions(List<? extends MODEL> messages) {
        Map<HASH, MODEL> hashToMessages = mapHashOfMessages(messages);
        Map<HASH, ACCESSION> existingAccessions = dbService.getExistingAccessions(hashToMessages.keySet());
        Map<HASH, MODEL> newMessages = filterNotExistingAccessions(hashToMessages, existingAccessions);

        Map<ACCESSION, MODEL> accessions = joinExistingAccessionsWithMessages(existingAccessions, hashToMessages);
        if (!newMessages.isEmpty()) {
            accessions.putAll(generateAccessions(newMessages));
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
        return messages.stream().collect(Collectors.toMap(summaryFunction.andThen(hashingFunction), e -> e, (r, o) -> r));
    }

    private Map<HASH, MODEL> filterNotExistingAccessions(Map<HASH, MODEL> hashToMessages,
                                                         Map<HASH, ACCESSION> existingAccessions) {
        return hashToMessages.entrySet().stream()
                .filter(entry -> !existingAccessions.containsKey(entry.getKey()))
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
    }

    private Map<ACCESSION, MODEL> joinExistingAccessionsWithMessages(Map<HASH, ACCESSION> existingAccessions,
                                                                     Map<HASH, MODEL> hashToMessages) {
        return existingAccessions.values().stream().collect(Collectors.toMap(e -> e, e -> hashToMessages.get(e)));
    }

    private Map<ACCESSION, MODEL> generateAccessions(Map<HASH, MODEL> accessions) {
        SaveResponse<ACCESSION, MODEL> response = dbService.save(accessionGenerator.generateAccessions(accessions));
        accessionGenerator.postSave(response);
        return response.getAllAccessionToMessage();
    }

    @Override
    public Map<ACCESSION, MODEL> getAccessions(List<? extends MODEL> accessionedObjects) {
        return dbService.findAllAccessionsByHash(getHashes(accessionedObjects));
    }

    @Override
    public Map<ACCESSION, ? extends MODEL> getByAccessions(List<ACCESSION> accessions) {
        return dbService.findAllAccessionByAccessions(accessions);
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
