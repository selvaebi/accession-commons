/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
 */
package uk.ac.ebi.ampt2d.accession;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.ampt2d.accession.test.TestMapAccessionRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class AccessioningServiceTest {

    private  AccessioningService<String,String> service;

    @Before
    public void setUp() throws Exception {

        AccessionGenerator<String, String> generator = new SingleAccessionGenerator<String, String>() {
            private static final String PREFIX = "ACC";

            private int counter = 0;

            @Override
            protected String generateAccession(String object) {
                return PREFIX + object;
            }
        };

        AccessionRepository<String, String> repository = new TestMapAccessionRepository();

        service = new AccessioningService<String,String>(generator){

            @Override
            public Map<String, String> get(List<String> objects) {
                return repository.get(objects);
            }

            @Override
            public void add(Map<String, String> accessions) {
                repository.add(accessions);
            }
        };
    }

    @Test
    public void everyNewObjectReceiveOneAccession() throws Exception {
        List<String> newObjects = Arrays.asList("Object 1", "Object 2", "Object 3");
        Map<String, String> accessions = service.getAccessions(newObjects);

        for (String object : newObjects) {
            assertNotNull(accessions.get(object));
        }
    }

    @Test
    public void sameObjectsGetSameAccession() throws Exception {
        String object1 = "Object 1";
        String anotherObject1 = "Object 1";

        List<String> newObjects = Arrays.asList(object1, anotherObject1);
        Map<String, String> accessions = service.getAccessions(newObjects);

        String accession1 = accessions.get(object1);
        String anotherAccession1 = accessions.get(anotherObject1);

        assertEquals(accession1, anotherAccession1);
    }

    @Test
    public void differentObjectsGetDifferentAccessions() throws Exception {
        String object1 = "Object 1";
        String object2 = "Object 2";

        List<String> newObjects = Arrays.asList(object1, object2);
        Map<String, String> accessions = service.getAccessions(newObjects);

        String accession1 = accessions.get(object1);
        String accession2 = accessions.get(object2);

        assertNotEquals(accession1, accession2);
    }

    @Test
    public void differentCallsToTheServiceUsingSameObjectsWillReturnSameAccessions() throws Exception {
        String object1 = "Object 1";
        String object2 = "Object 2";

        Map<String, String> accessions = service.getAccessions(Arrays.asList(object1, object2));

        String accession1 = accessions.get(object1);
        String accession2 = accessions.get(object2);

        Map<String, String> accessionsFromSecondServiceCall = service.getAccessions(Arrays.asList(object1, object2));
        
        assertEquals(accession1, accessionsFromSecondServiceCall.get(object1));
        assertEquals(accession2, accessionsFromSecondServiceCall.get(object2));
    }

    @Test
    public void mixingAlreadyAccessionedAndNewObjectsIsAllowed() throws Exception {
        String object1 = "Object 1";
        String object2 = "Object 2";

        Map<String, String> accessions = service.getAccessions(Arrays.asList(object1, object2));

        String accession1 = accessions.get(object1);
        String accession2 = accessions.get(object2);

        String object3 = "Object 3";
        String object4 = "Object 4";

        List<String> objectsToAccession = Arrays.asList(object1, object2, object3, object4);
        Map<String, String> accessionsFromSecondServiceCall = service
                .getAccessions(Arrays.asList(object1, object2, object3, object4));

        assertEquals(accession1, accessionsFromSecondServiceCall.get(object1));
        assertEquals(accession2, accessionsFromSecondServiceCall.get(object2));
        assertNotNull(accessionsFromSecondServiceCall.get(object3));
        assertNotNull(accessionsFromSecondServiceCall.get(object4));

        assertEquals(objectsToAccession.size(),
                     new HashSet<>(accessionsFromSecondServiceCall.values()).size());
    }

}