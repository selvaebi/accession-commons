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
import uk.ac.ebi.ampt2d.commons.accession.core.AccessionModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Basic implementation of {@link DatabaseService} that requires a Spring Data repository that extends
 * {@link IAccessionedObjectRepository}, a function to generate the entities from the triple model/hash/accession, a function
 * to get the accession from the entity and a function to get the hashed representation of the message from the entity.
 *
 * @param <MODEL>
 * @param <ENTITY>
 * @param <ACCESSION>
 */
public class BasicSpringDataRepositoryDatabaseService<MODEL, ENTITY extends IAccessionedObject<ACCESSION>,
        ACCESSION extends Serializable> implements DatabaseService<MODEL, String, ACCESSION> {

    private IAccessionedObjectRepository<ENTITY, ACCESSION> repository;

    private final Function<AccessionModel<MODEL, String, ACCESSION>, ENTITY> toEntityFunction;

    private final Function<ENTITY, MODEL> toModelFunction;

    public BasicSpringDataRepositoryDatabaseService(IAccessionedObjectRepository<ENTITY, ACCESSION> repository,
                                                    Function<AccessionModel<MODEL, String, ACCESSION>, ENTITY> toEntityFunction,
                                                    Function<ENTITY, MODEL> toModelFunction) {
        this.repository = repository;
        this.toEntityFunction = toEntityFunction;
        this.toModelFunction = toModelFunction;
    }

    @Override
    public List<AccessionModel<MODEL, String, ACCESSION>> findAllAccessionsByHash(Collection<String> hashes) {
        return repository.findByHashedMessageIn(hashes).stream()
                .map(entity -> AccessionModel.of(entity.getAccession(), entity.getHashedMessage(),
                        toModelFunction.apply(entity)))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, ACCESSION> getExistingAccessions(Collection<String> hashes) {
        return repository.findByHashedMessageIn(hashes).stream()
                .collect(Collectors.toMap(IAccessionedObject::getHashedMessage, IAccessionedObject::getAccession));
    }

    @Override
    @Transactional
    public void save(List<AccessionModel<MODEL, String, ACCESSION>> objects) {
        Set<ENTITY> entitySet = objects.stream().map(toEntityFunction).collect(Collectors.toSet());
        repository.save(entitySet);
    }

    @Override
    public List<AccessionModel<MODEL, String, ACCESSION>> findAllAccessionMappingsByAccessions(
            List<ACCESSION> accessions) {
        List<AccessionModel<MODEL, String, ACCESSION>> result = new ArrayList<>();
        repository.findAll(accessions).iterator().forEachRemaining(
                entity -> result.add(new AccessionModel<>(entity.getAccession(), entity.getHashedMessage(),
                        entity.isActive(), toModelFunction.apply(entity))));
        return result;
    }

    @Override
    public void enableAccessions(List<AccessionModel<MODEL, String, ACCESSION>> accessionedObjects) {
        repository.enableByHashedMessageIn(accessionedObjects.stream().map(AccessionModel::getHash)
                .collect(Collectors.toSet()));
    }

}
