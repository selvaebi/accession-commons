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

import org.springframework.beans.factory.InitializingBean;
import uk.ac.ebi.ampt2d.accession.common.utils.DigestFunction;
import uk.ac.ebi.ampt2d.accession.common.utils.HashingFunction;
import uk.ac.ebi.ampt2d.accession.common.generators.AccessionGenerator;
import uk.ac.ebi.ampt2d.accession.common.persistence.DatabaseService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A service that provides accessions for objects
 *
 * @param <MODEL>
 * @param <ACCESSION>
 * @param <HASH>
 */
public class BasicAccessioningService<MODEL, HASH, ACCESSION> implements InitializingBean,
        AccessioningService<MODEL, ACCESSION> {

    private AccessionGenerator<MODEL, ACCESSION> accessionGenerator;

    private DatabaseService<MODEL, HASH, ACCESSION> dbService;

    private final DigestFunction<MODEL> digestFunction;

    private HashingFunction<HASH> hashingFunction;

    /**
     * Basic accessioning service constructor, requires an accession generator, a database service and a hashing
     * function, it also allows the registering of a init function.
     *
     * @param accessionGenerator
     * @param dbService
     * @param hashingFunction
     */
    public BasicAccessioningService(AccessionGenerator<MODEL, ACCESSION> accessionGenerator,
                                    DatabaseService<MODEL, HASH, ACCESSION> dbService,
                                    DigestFunction<MODEL> digestFunction,
                                    HashingFunction<HASH> hashingFunction) {
        this.accessionGenerator = accessionGenerator;
        this.dbService = dbService;
        this.digestFunction = digestFunction;
        this.hashingFunction = hashingFunction;
    }

    /**
     * Get accessions for a list of messages. It looks for the object's accessions in a repository, and it they don't
     * exist, generate new ones, storing them in the repository
     *
     * @param messages
     * @return
     */
    @Override
    public Map<ACCESSION, MODEL> getAccessions(List<? extends MODEL> messages) {
        Map<HASH, MODEL> hashToMessages = mapHashOfMessages(messages);
        Map<HASH, ACCESSION> existingAccessions = dbService.getExistingAccessions(hashToMessages.keySet());
        Map<HASH, MODEL> newMessages = filterNotExists(hashToMessages, existingAccessions);

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
        return messages.stream().collect(Collectors.toMap(digestFunction.andThen(hashingFunction), e -> e, (r, o) -> r));
    }

    private Map<HASH, MODEL> filterNotExists(Map<HASH, MODEL> hashToMessages,
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
    public Map<ACCESSION, MODEL> get(List<? extends MODEL> accessionObjects) {
        return dbService.findAllAccessionByMessageHash(getHashes(accessionObjects));
    }

    @Override
    public Map<ACCESSION, ? extends MODEL> getByAccessions(List<ACCESSION> accessions) {
        return dbService.findAllAccessionByAccessions(accessions);
    }

    private List<HASH> getHashes(List<? extends MODEL> accessionObjects) {
        return accessionObjects.stream().map(digestFunction.andThen(hashingFunction)).collect(Collectors.toList());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Do nothing
    }

    protected AccessionGenerator<MODEL, ACCESSION> getAccessionGenerator() {
        return accessionGenerator;
    }

    protected DatabaseService<MODEL, HASH, ACCESSION> getDbService() {
        return dbService;
    }

}
