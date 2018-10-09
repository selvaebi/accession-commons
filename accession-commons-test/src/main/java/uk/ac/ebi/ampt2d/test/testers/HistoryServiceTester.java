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
package uk.ac.ebi.ampt2d.test.testers;

import uk.ac.ebi.ampt2d.commons.accession.core.HistoryService;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDoesNotExistException;
import uk.ac.ebi.ampt2d.commons.accession.core.models.EventType;
import uk.ac.ebi.ampt2d.commons.accession.core.models.HistoryEvent;
import uk.ac.ebi.ampt2d.test.models.TestModel;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class HistoryServiceTester {

    private final HistoryService<TestModel, String> historyService;

    private List<HistoryEvent<TestModel, String>> historyEvents;

    private IMethodTester lastMethodResponse;

    public HistoryServiceTester(HistoryService<TestModel, String> historyService) {
        this.historyService = historyService;
    }

    public HistoryServiceTester getHistory(String accession) {
        lastMethodResponse = new MethodTester(() -> this.historyEvents = historyService.getHistory(accession));
        return this;
    }

    public IMethodTester getLastMethodResponse() {
        return lastMethodResponse;
    }

    public void assertSearchReturnEvents(int noOfEvents) throws AccessionDoesNotExistException {
        assertEquals(noOfEvents, historyEvents.size());
    }

    public void assertEventsInOrder(String events) throws AccessionDoesNotExistException {
        String[] eventTypes = events.split(",");
        int noOfEvents = eventTypes.length;
        for (int i = 0; i < noOfEvents; i++) {
            assertEquals(EventType.valueOf(eventTypes[i]), historyEvents.get(i).getEventType());
        }
    }
}