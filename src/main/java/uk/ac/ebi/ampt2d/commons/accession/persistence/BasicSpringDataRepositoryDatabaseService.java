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
import uk.ac.ebi.ampt2d.commons.accession.core.AccessionWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDoesNotExistException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.HashAlreadyExistsException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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

    private IAccessionedObjectCustomRepository customMethodsRepository;

    private final Function<AccessionWrapper<MODEL, String, ACCESSION>, ENTITY> toEntityFunction;

    private final Function<ENTITY, MODEL> toModelFunction;

    public BasicSpringDataRepositoryDatabaseService(IAccessionedObjectRepository<ENTITY, ACCESSION> repository,
                                                    IAccessionedObjectCustomRepository customMethodsRepository,
                                                    Function<AccessionWrapper<MODEL, String, ACCESSION>, ENTITY> toEntityFunction,
                                                    Function<ENTITY, MODEL> toModelFunction) {
        this.repository = repository;
        this.customMethodsRepository = customMethodsRepository;
        this.toEntityFunction = toEntityFunction;
        this.toModelFunction = toModelFunction;
    }

    @Override
    public List<AccessionWrapper<MODEL, String, ACCESSION>> findAllAccessionsByHash(Collection<String> hashes) {
        List<AccessionWrapper<MODEL, String, ACCESSION>> wrappedAccessions = new ArrayList<>();
        repository.findAll(hashes).iterator().forEachRemaining(
                entity -> wrappedAccessions.add(createAccessionWrapperFromEntity(entity)));
        return wrappedAccessions;
    }

    private AccessionWrapper<MODEL, String, ACCESSION> createAccessionWrapperFromEntity(ENTITY entity) {
        return new AccessionWrapper<>(entity.getAccession(), entity.getHashedMessage(), toModelFunction.apply(entity),
                entity.getVersion(), entity.isActive());
    }

    @Override
    @Transactional
    public void insert(List<AccessionWrapper<MODEL, String, ACCESSION>> objects) {
        Set<ENTITY> entitySet = objects.stream().map(toEntityFunction).collect(Collectors.toSet());
        customMethodsRepository.insert(entitySet);
    }

    @Override
    public List<AccessionWrapper<MODEL, String, ACCESSION>> findAllAccessionMappingsByAccessions(
            List<ACCESSION> accessions) {
        List<AccessionWrapper<MODEL, String, ACCESSION>> result = new ArrayList<>();
        repository.findByAccessionIn(accessions).iterator().forEachRemaining(
                entity -> result.add(createAccessionWrapperFromEntity(entity)));
        return result;
    }

    @Override
    public void enableAccessions(List<AccessionWrapper<MODEL, String, ACCESSION>> accessionedObjects) {
        customMethodsRepository.enableByHashedMessageIn(accessionedObjects.stream().map(AccessionWrapper::getHash)
                .collect(Collectors.toSet()));
    }

    @Override
    public AccessionWrapper<MODEL, String, ACCESSION> update(AccessionWrapper<MODEL, String, ACCESSION> accession)
            throws AccessionDoesNotExistException, HashAlreadyExistsException {
        Collection<ENTITY> accessionedElements = repository.findByAccession(accession.getAccession());
        assertAccessionExists(accession, accessionedElements);
        assertHashDoesNotExist(accession);

        int version = 1;
        for (ENTITY accessionedElement : accessionedElements) {
            if (version <= accessionedElement.getVersion()) {
                version = accessionedElement.getVersion() + 1;
            }
        }

        AccessionWrapper<MODEL, String, ACCESSION> newAccessionVersion =
                new AccessionWrapper<>(accession.getAccession(), accession.getHash(), accession.getData(), version);
        insert(Arrays.asList(newAccessionVersion));
        return newAccessionVersion;
    }

    private void assertHashDoesNotExist(AccessionWrapper<MODEL, String, ACCESSION> accession)
            throws HashAlreadyExistsException {
        if (repository.findOne(accession.getHash()) != null) {
            throw new HashAlreadyExistsException(accession.getHash(), accession.getData().getClass());
        }
    }

    private void assertAccessionExists(AccessionWrapper<MODEL, String, ACCESSION> accession,
                                       Collection<ENTITY> accessionedElements) throws AccessionDoesNotExistException {
        if (accessionedElements.isEmpty()) {
            throw new AccessionDoesNotExistException(accession.toString());
        }
    }

}
