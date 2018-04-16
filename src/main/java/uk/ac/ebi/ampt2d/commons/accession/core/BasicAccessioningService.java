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
import uk.ac.ebi.ampt2d.commons.accession.persistence.DatabaseService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A service that provides accessions for objects
 *
 * @param <MODEL>
 * @param <ACCESSION>
 * @param <HASH>
 */
public class BasicAccessioningService<MODEL, HASH, ACCESSION extends Serializable>
        implements AccessioningService<MODEL, HASH, ACCESSION> {

    private final BasicAccessioningServiceSaveDelegate<MODEL, HASH, ACCESSION> basicAccessioningServiceSaveDelegate;

    private AccessionGenerator<MODEL, ACCESSION> accessionGenerator;

    private DatabaseService<MODEL, HASH, ACCESSION> dbService;

    private final Function<MODEL, HASH> hashingFunction;

    public BasicAccessioningService(AccessionGenerator<MODEL, ACCESSION> accessionGenerator,
                                    DatabaseService<MODEL, HASH, ACCESSION> dbService,
                                    Function<MODEL, String> summaryFunction,
                                    Function<String, HASH> hashingFunction) {
        this.accessionGenerator = accessionGenerator;
        this.dbService = dbService;
        this.hashingFunction = summaryFunction.andThen(hashingFunction);
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
    public List<AccessionModel<MODEL, HASH, ACCESSION>> getOrCreateAccessions(List<? extends MODEL> messages)
            throws AccessionCouldNotBeGeneratedException {
        return saveAccessions(accessionGenerator.generateAccessions(mapHashOfMessages(messages)));
    }

    /**
     * Digest messages, hash them and map them. If Two messages have the same hash keep the first one.
     *
     * @param messages
     * @return
     */
    private Map<HASH, MODEL> mapHashOfMessages(List<? extends MODEL> messages) {
        return messages.stream().collect(Collectors.toMap(hashingFunction, e -> e, (r, o) -> r));
    }

    /**
     * Execute {@link BasicAccessioningServiceSaveDelegate#doSaveAccessionedModels(List)} This operation will generate two
     * lists on {@link SaveResponse} saved elements and not saved elements. Not saved elements are elements that
     * could not be stored on database due to constraint exceptions. This should only happen when elements have been
     * already stored by another application instance / thread with a different id.
     * See {@link #getUnsavedAccessions(List)} } for more details.
     *
     * @param accessions
     * @return
     */
    private List<AccessionModel<MODEL, HASH, ACCESSION>> saveAccessions(List<AccessionModel<MODEL, HASH, ACCESSION>> accessions) {
        SaveResponse<ACCESSION> response = basicAccessioningServiceSaveDelegate.doSaveAccessionedModels(accessions);
        accessionGenerator.postSave(response);

        final List<AccessionModel<MODEL, HASH, ACCESSION>> savedAccessions = new ArrayList<>();
        final List<AccessionModel<MODEL, HASH, ACCESSION>> saveFailedAccessions = new ArrayList<>();
        accessions.stream().forEach(accessionModel -> {
            if (response.isSavedAccession(accessionModel)) {
                savedAccessions.add(accessionModel);
            } else {
                saveFailedAccessions.add(accessionModel);
            }
        });

        if (!saveFailedAccessions.isEmpty()) {
            List<AccessionModel<MODEL, HASH, ACCESSION>> unsavedAccessions = getUnsavedAccessions(saveFailedAccessions);
            savedAccessions.addAll(unsavedAccessions);
            dbService.enableAccessions(unsavedAccessions);
        }

        return savedAccessions;
    }

    /**
     * We try to recover all elements that could not be saved to return their accession to the user. This is only
     * expected when another application instance or thread has saved that element already with a different id. If
     * any element can't be retrieved from the database we throw a {@link MissingUnsavedAccessions} to alert the system.
     *
     * @param saveFailedAccessions
     * @return
     */
    private List<AccessionModel<MODEL, HASH, ACCESSION>> getUnsavedAccessions(
            List<AccessionModel<MODEL, HASH, ACCESSION>> saveFailedAccessions) {

        Set<HASH> saveFailedHashes = saveFailedAccessions.stream().map(AccessionModel::getHash)
                .collect(Collectors.toSet());
        List<AccessionModel<MODEL, HASH, ACCESSION>> dbAccessions = dbService.findAllAccessionsByHash(saveFailedHashes);
        if (dbAccessions.size() != saveFailedHashes.size()) {
            throw new MissingUnsavedAccessions(dbAccessions, saveFailedAccessions);
        }
        return dbAccessions;
    }

    @Override
    public List<AccessionModel<MODEL, HASH, ACCESSION>> getAccessions(List<? extends MODEL> accessionedObjects) {
        return dbService.findAllAccessionsByHash(getHashes(accessionedObjects));
    }

    private List<HASH> getHashes(List<? extends MODEL> accessionObjects) {
        return accessionObjects.stream().map(hashingFunction).collect(Collectors.toList());
    }

    @Override
    public List<AccessionModel<MODEL, HASH, ACCESSION>> getByAccessions(List<ACCESSION> accessions,
                                                                        boolean hideDeprecated) {
        return dbService.findAllAccessionMappingsByAccessions(accessions);
    }

    protected AccessionGenerator<MODEL, ACCESSION> getAccessionGenerator() {
        return accessionGenerator;
    }

    protected DatabaseService<MODEL, HASH, ACCESSION> getDbService() {
        return dbService;
    }

}
