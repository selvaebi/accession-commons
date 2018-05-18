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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ampt2d.commons.accession.core.AccessionWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.ModelWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDeprecatedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDoesNotExistException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionMergedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.HashAlreadyExistsException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
 * @param <ACCESSION_ENTITY>
 * @param <ACCESSION>
 */
public class BasicSpringDataRepositoryDatabaseService<
        MODEL,
        ACCESSION extends Serializable,
        ACCESSION_ENTITY extends IAccessionedObject<ACCESSION>>
        implements DatabaseService<MODEL, String, ACCESSION> {

    private final static Logger logger = LoggerFactory.getLogger(BasicSpringDataRepositoryDatabaseService.class);

    private final IAccessionedObjectRepository<ACCESSION_ENTITY, ACCESSION> repository;

    private final Function<ModelWrapper<MODEL, String, ACCESSION>, ACCESSION_ENTITY> toEntityFunction;

    private final Function<ACCESSION_ENTITY, MODEL> toModelFunction;

    private final ArchiveService<MODEL, String, ACCESSION, ACCESSION_ENTITY> archiveService;

    public BasicSpringDataRepositoryDatabaseService(
            IAccessionedObjectRepository<ACCESSION_ENTITY, ACCESSION> repository,
            Function<ModelWrapper<MODEL, String, ACCESSION>, ACCESSION_ENTITY> toEntityFunction,
            Function<ACCESSION_ENTITY, MODEL> toModelFunction,
            ArchiveService<MODEL, String, ACCESSION, ACCESSION_ENTITY> archiveService) {
        this.repository = repository;
        this.toEntityFunction = toEntityFunction;
        this.toModelFunction = toModelFunction;
        this.archiveService = archiveService;
    }

    @Override
    public List<ModelWrapper<MODEL, String, ACCESSION>> findAllModelByHash(Collection<String> hashes) {
        List<ModelWrapper<MODEL, String, ACCESSION>> wrappedAccessions = new ArrayList<>();
        repository.findAll(hashes).iterator().forEachRemaining(
                entity -> wrappedAccessions.add(toModelWrapper(entity)));
        return wrappedAccessions;
    }

    private ModelWrapper<MODEL, String, ACCESSION> toModelWrapper(ACCESSION_ENTITY entity) {
        return new ModelWrapper<>(entity.getAccession(), entity.getHashedMessage(), toModelFunction.apply(entity),
                entity.getVersion());
    }

    @Override
    public AccessionWrapper<MODEL, String, ACCESSION> findAccession(ACCESSION accession)
            throws AccessionDoesNotExistException, AccessionMergedException, AccessionDeprecatedException {
        List<ACCESSION_ENTITY> entities = repository.findByAccession(accession);
        checkAccessionIsActive(entities, accession);
        return toAccessionWrapper(entities);
    }

    private void checkAccessionIsActive(List<ACCESSION_ENTITY> entities, ACCESSION accession)
            throws AccessionDoesNotExistException, AccessionMergedException, AccessionDeprecatedException {
        if (entities == null || entities.isEmpty()) {
            checkAccessionMergedOrDeprecated(accession);
            throw new AccessionDoesNotExistException(accession.toString());
        }
    }

    private void checkAccessionMergedOrDeprecated(ACCESSION accession) throws AccessionDoesNotExistException,
            AccessionMergedException, AccessionDeprecatedException {
        IArchiveOperation<ACCESSION> operation = archiveService.getLastOperation(accession);
        if (operation != null) {
            switch (operation.getOperationType()) {
                case MERGED_INTO:
                    throw new AccessionMergedException(operation.getAccessionIdDestiny());
                case DEPRECATED:
                    throw new AccessionDeprecatedException();
            }
        }
    }

    private AccessionWrapper<MODEL, String, ACCESSION> toAccessionWrapper(List<ACCESSION_ENTITY> entities) {
        final List<ModelWrapper<MODEL, String, ACCESSION>> models = entities.stream().map(this::toModelWrapper)
                .collect(Collectors.toList());
        return new AccessionWrapper<>(models);
    }

    @Override
    public List<ModelWrapper<MODEL, String, ACCESSION>> findAllAccessions(List<ACCESSION> accessions) {
        HashMap<ACCESSION, List<ACCESSION_ENTITY>> modelsByAccession = new HashMap<>();
        repository.findByAccessionIn(accessions).iterator().forEachRemaining(
                entity -> {
                    modelsByAccession.putIfAbsent(entity.getAccession(), new ArrayList<>());
                    modelsByAccession.get(entity.getAccession()).add(entity);
                });

        return modelsByAccession.values().stream().map(this::filterOldVersions).map(this::toModelWrapper)
                .collect(Collectors.toList());
    }

    private ACCESSION_ENTITY filterOldVersions(List<ACCESSION_ENTITY> accessionedElements) {
        int maxVersion = 1;
        ACCESSION_ENTITY lastVersionEntity = null;
        for (ACCESSION_ENTITY element : accessionedElements) {
            if (element.getVersion() >= maxVersion) {
                maxVersion = element.getVersion();
                lastVersionEntity = element;
            }
        }

        return lastVersionEntity;
    }

    @Override
    public ModelWrapper<MODEL, String, ACCESSION> findAccessionVersion(ACCESSION accession, int version)
            throws AccessionDoesNotExistException, AccessionDeprecatedException, AccessionMergedException {
        ACCESSION_ENTITY result = doFindAccessionVersion(accession, version);
        return toModelWrapper(result);
    }

    private ACCESSION_ENTITY doFindAccessionVersion(ACCESSION accession, int version) throws
            AccessionDoesNotExistException, AccessionMergedException, AccessionDeprecatedException {
        ACCESSION_ENTITY result = repository.findByAccessionAndVersion(accession, version);
        if (result == null) {
            checkAccessionMergedOrDeprecated(accession);
            throw new AccessionDoesNotExistException(accession.toString(), version);
        }
        return result;
    }

    @Override
    public void insert(List<ModelWrapper<MODEL, String, ACCESSION>> objects) {
        Set<ACCESSION_ENTITY> entitySet = objects.stream().map(toEntityFunction).collect(Collectors.toSet());
        repository.insert(entitySet);
    }

    @Override
    public AccessionWrapper<MODEL, String, ACCESSION> patch(ModelWrapper<MODEL, String, ACCESSION> accession)
            throws AccessionDoesNotExistException, HashAlreadyExistsException, AccessionDeprecatedException,
            AccessionMergedException {
        List<ACCESSION_ENTITY> entities = getAccession(accession.getAccession());
        checkHashDoesNotExist(accession);
        int maxVersion = 1;
        for (ACCESSION_ENTITY entity : entities) {
            if (entity.getVersion() >= maxVersion) {
                maxVersion = entity.getVersion();
            }
        }
        accession.setVersion(maxVersion + 1);
        try {
            repository.insert(Arrays.asList(toEntityFunction.apply(accession)));
        } catch (RuntimeException e) {
            checkHashDoesNotExist(accession);
            throw e;
        }
        return findAccession(accession.getAccession());
    }

    private void checkHashDoesNotExist(ModelWrapper<MODEL, String, ACCESSION> accession)
            throws HashAlreadyExistsException {
        final ACCESSION_ENTITY dbAccession = repository.findOne(accession.getHash());
        if (dbAccession != null) {
            throw new HashAlreadyExistsException(dbAccession.getHashedMessage(), dbAccession.getAccession());
        }
    }

    /**
     * @param accessionId
     * @return All entries of an accession. It is never empty
     * @throws AccessionDoesNotExistException If no accession with accessionId found
     */
    private List<ACCESSION_ENTITY> getAccession(ACCESSION accessionId)
            throws AccessionDoesNotExistException, AccessionDeprecatedException, AccessionMergedException {
        List<ACCESSION_ENTITY> accessionedElements = repository.findByAccession(accessionId);
        checkAccessionIsActive(accessionedElements, accessionId);

        return accessionedElements;
    }

    @Override
    public AccessionWrapper<MODEL, String, ACCESSION> update(ModelWrapper<MODEL, String, ACCESSION> accession)
            throws AccessionDoesNotExistException, HashAlreadyExistsException, AccessionMergedException,
            AccessionDeprecatedException {
        ACCESSION_ENTITY oldVersion = doFindAccessionVersion(accession.getAccession(), accession.getVersion());
        checkHashDoesNotExist(accession);

        archiveService.archiveVersion(oldVersion, "Version update");
        repository.delete(oldVersion);
        try {
            insert(Arrays.asList(accession));
        }catch (RuntimeException e){
            checkHashDoesNotExist(accession);
            throw e;
        }
        return findAccession(accession.getAccession());
    }

    @Override
    public void deprecate(ACCESSION accession, String reason) throws AccessionDoesNotExistException,
            AccessionMergedException, AccessionDeprecatedException {
        List<ACCESSION_ENTITY> accessionedElements = getAccession(accession);
        archiveService.archiveDeprecation(accession, accessionedElements, reason);
        repository.delete(accessionedElements);
    }

}
