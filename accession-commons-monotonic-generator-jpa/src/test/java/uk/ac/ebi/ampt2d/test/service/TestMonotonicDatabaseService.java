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
package uk.ac.ebi.ampt2d.test.service;

import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionWrapper;
import uk.ac.ebi.ampt2d.commons.accession.generators.monotonic.MonotonicRange;
import uk.ac.ebi.ampt2d.commons.accession.persistence.services.BasicSpringDataRepositoryDatabaseService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.repositories.IAccessionedObjectRepository;
import uk.ac.ebi.ampt2d.commons.accession.persistence.services.InactiveAccessionService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.service.MonotonicDatabaseService;
import uk.ac.ebi.ampt2d.test.models.TestModel;
import uk.ac.ebi.ampt2d.test.persistence.TestMonotonicEntity;

import java.util.Collection;
import java.util.function.Function;

public class TestMonotonicDatabaseService
        extends BasicSpringDataRepositoryDatabaseService<TestModel, Long, TestMonotonicEntity>
        implements MonotonicDatabaseService {

    public TestMonotonicDatabaseService(
            IAccessionedObjectRepository<TestMonotonicEntity, Long> repository,
            Function<AccessionWrapper<TestModel, String, Long>, TestMonotonicEntity> toEntityFunction,
            InactiveAccessionService<TestModel, Long, TestMonotonicEntity> inactiveAccessionService) {
        super(repository, toEntityFunction, inactiveAccessionService);
    }

    @Override
    public long[] getAccessionsInRanges(Collection<MonotonicRange> ranges) {
        //No need for test
        return new long[0];
    }

}
