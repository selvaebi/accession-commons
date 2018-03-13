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

import uk.ac.ebi.ampt2d.accession.commons.core.AccessioningRepository;
import uk.ac.ebi.ampt2d.accession.commons.generators.ModelHashAccession;
import uk.ac.ebi.ampt2d.accession.commons.generators.monotonic.MonotonicRange;
import uk.ac.ebi.ampt2d.accession.commons.persistence.BasicSpringDataRepositoryDatabaseService;
import uk.ac.ebi.ampt2d.accession.commons.persistence.MonotonicDatabaseService;
import uk.ac.ebi.ampt2d.test.TestModel;
import uk.ac.ebi.ampt2d.test.persistence.TestMonotonicEntity;

import java.util.Collection;
import java.util.function.Function;

public class TestMonotonicDatabaseService
        extends BasicSpringDataRepositoryDatabaseService<TestModel, TestMonotonicEntity, String, Long>
        implements MonotonicDatabaseService<TestModel, String> {

    public TestMonotonicDatabaseService(AccessioningRepository<TestMonotonicEntity, String, Long> repository, Function<ModelHashAccession<TestModel, String, Long>, TestMonotonicEntity> toEntityFunction, Function<TestMonotonicEntity, Long> getAccessionFunction, Function<TestMonotonicEntity, String> getHashedMessageFunction) {
        super(repository, toEntityFunction, getAccessionFunction, getHashedMessageFunction);
    }

    @Override
    public long[] getAccessionsInRanges(Collection<MonotonicRange> ranges) {
        //No need for test
        return new long[0];
    }

}
