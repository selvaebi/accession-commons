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
package uk.ac.ebi.ampt2d.commons.accession.service;

import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionWrapper;
import uk.ac.ebi.ampt2d.commons.accession.generators.monotonic.MonotonicRange;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.service.MonotonicDatabaseService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.models.IAccessionedObject;
import uk.ac.ebi.ampt2d.commons.accession.persistence.repositories.IAccessionedObjectRepository;
import uk.ac.ebi.ampt2d.commons.accession.persistence.services.BasicSpringDataRepositoryDatabaseService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.services.InactiveAccessionService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Basic implementation of {@link MonotonicDatabaseService} that requires a Spring Data repository that extends
 * {@link IAccessionedObjectRepository}, a function to generate the entities from the triple model/hash/accession, a function
 * to get the accession from the entity and a function to get the hashed representation of the message from the entity.
 *
 * Reuses implementation for most methods from {@link BasicSpringDataRepositoryDatabaseService}.
 *
 * @param <MODEL>
 * @param <ACCESSION_ENTITY>
 */
public class BasicSpringDataRepositoryMonotonicDatabaseService<
        MODEL,
        ACCESSION_ENTITY extends IAccessionedObject<MODEL, String, Long>>
        extends BasicSpringDataRepositoryDatabaseService<MODEL, Long, ACCESSION_ENTITY>
        implements MonotonicDatabaseService<MODEL, String> {

    private final IAccessionedObjectRepository<ACCESSION_ENTITY, Long> repository;

    private final Function<AccessionWrapper<MODEL, String, Long>, ACCESSION_ENTITY> toEntityFunction;

    private final InactiveAccessionService<MODEL, Long, ACCESSION_ENTITY> inactiveAccessionService;

    public BasicSpringDataRepositoryMonotonicDatabaseService(
            IAccessionedObjectRepository<ACCESSION_ENTITY, Long> repository,
            Function<AccessionWrapper<MODEL, String, Long>, ACCESSION_ENTITY> toEntityFunction,
            InactiveAccessionService<MODEL, Long, ACCESSION_ENTITY> inactiveAccessionService) {
        super(repository, toEntityFunction, inactiveAccessionService);
        this.repository = repository;
        this.toEntityFunction = toEntityFunction;
        this.inactiveAccessionService = inactiveAccessionService;
    }

    @Override
    public long[] getAccessionsInRanges(Collection<MonotonicRange> ranges) {
        List<Long> accessionsInRanges = new ArrayList<>();
        for (MonotonicRange range : ranges) {
            repository
                    .findByAccessionGreaterThanEqualAndAccessionLessThanEqual(range.getStart(), range.getEnd())
                    .stream()
                    .map(IAccessionedObject::getAccession)
                    .forEach(accessionsInRanges::add);
        }
        long[] accessionArray = new long[accessionsInRanges.size()];
        for (int i = 0; i < accessionsInRanges.size(); i++) {
            accessionArray[i] = accessionsInRanges.get(i);
        }
        return accessionArray;
    }
}
