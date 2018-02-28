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
package uk.ac.ebi.ampt2d.accessioning.commons.accessioning;

import org.springframework.beans.factory.InitializingBean;
import uk.ac.ebi.ampt2d.accessioning.commons.generators.monotonic.MonotonicAccessionGenerator;
import uk.ac.ebi.ampt2d.accessioning.commons.generators.monotonic.MonotonicRange;
import uk.ac.ebi.ampt2d.accessioning.commons.persistence.MonotonicDatabaseService;

import java.util.Collection;
import java.util.function.Function;

public class BasicMonotonicAccessioningService<MODEL, HASH> extends BasicAccessioningService<MODEL, HASH, Long>
        implements InitializingBean {

    public BasicMonotonicAccessioningService(MonotonicAccessionGenerator<MODEL> accessionGenerator,
                                             MonotonicDatabaseService<MODEL, HASH> dbService,
                                             Function<MODEL, String> summaryFunction,
                                             Function<String, HASH> hashingFunction) {
        super(accessionGenerator, dbService, summaryFunction, hashingFunction);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Collection<MonotonicRange> availableRanges = getAccessionGenerator().getAvailableRanges();
        getAccessionGenerator().recoverState(getDbService().getAccessionsInRanges(availableRanges));
    }

    @Override
    protected MonotonicAccessionGenerator<MODEL> getAccessionGenerator() {
        return (MonotonicAccessionGenerator<MODEL>) super.getAccessionGenerator();
    }

    @Override
    protected MonotonicDatabaseService<MODEL, HASH> getDbService() {
        return (MonotonicDatabaseService<MODEL, HASH>) super.getDbService();
    }

}
