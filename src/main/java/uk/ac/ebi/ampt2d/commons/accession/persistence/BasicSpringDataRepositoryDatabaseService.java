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
package uk.ac.ebi.ampt2d.commons.accession.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.ampt2d.commons.accession.core.AccessioningRepository;
import uk.ac.ebi.ampt2d.commons.accession.generators.ModelHashAccession;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Basic implementation of {@link DatabaseService} that requires a Spring Data repository that extends
 * {@link AccessioningRepository}, a function to generate the entities from the triple model/hash/accession, a function
 * to get the accession from the entity and a function to get the hashed representation of the message from the entity.
 *
 * @param <MODEL>
 * @param <ENTITY>
 * @param <HASH>
 * @param <ACCESSION>
 */
public class BasicSpringDataRepositoryDatabaseService<MODEL, ENTITY extends MODEL, HASH, ACCESSION extends Serializable>
        implements DatabaseService<MODEL, HASH, ACCESSION> {

    private AccessioningRepository<ENTITY, HASH, ACCESSION> repository;

    private final Function<ModelHashAccession<MODEL, HASH, ACCESSION>, ENTITY> toEntityFunction;

    private final Function<ENTITY, ACCESSION> getAccessionFunction;

    private final Function<ENTITY, HASH> getHashedMessageFunction;


    public BasicSpringDataRepositoryDatabaseService(AccessioningRepository<ENTITY, HASH, ACCESSION> repository,
                                                    Function<ModelHashAccession<MODEL, HASH, ACCESSION>, ENTITY> toEntityFunction,
                                                    Function<ENTITY, ACCESSION> getAccessionFunction,
                                                    Function<ENTITY, HASH> getHashedMessageFunction) {
        this.repository = repository;
        this.toEntityFunction = toEntityFunction;
        this.getAccessionFunction = getAccessionFunction;
        this.getHashedMessageFunction = getHashedMessageFunction;
    }

    @Override
    public Map<ACCESSION, MODEL> findAllAccessionsByHash(Collection<HASH> hashes) {
        return repository.findByHashedMessageIn(hashes).stream()
                .collect(Collectors.toMap(getAccessionFunction, e -> e));
    }

    @Override
    public Map<HASH, ACCESSION> getExistingAccessions(Collection<HASH> hashes) {
        return repository.findByHashedMessageIn(hashes).stream()
                .collect(Collectors.toMap(getHashedMessageFunction, getAccessionFunction));
    }

    @Override
    @Transactional
    public void save(List<ModelHashAccession<MODEL, HASH, ACCESSION>> objects) {
        HashMap<ACCESSION, MODEL> savedAccessions = new HashMap<>();
        HashMap<ACCESSION, MODEL> unsavedAccessions = new HashMap<>();

        Set<ENTITY> entitySet = objects.stream()
                .map(toEntityFunction).collect(Collectors.toSet());
        repository.save(entitySet);
    }

    @Override
    public Map<ACCESSION, ? extends MODEL> findAllAccessionByAccessions(List<ACCESSION> accessions) {
        Map<ACCESSION, MODEL> result = new HashMap<>();
        repository.findAll(accessions).iterator().forEachRemaining(entity -> result.put(getAccessionFunction
                .apply(entity), entity));
        return result;
    }

}
