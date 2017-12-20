/*
 *
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
 *
 */
package uk.ac.ebi.ampt2d.accession.object;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.accession.AccessioningObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "services=object-accession")
public class ObjectAccessioningRestControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ObjectAccessioningRepository objectAccessioningRepository;

    private Map<String, String> studyMap1;
    private Map<String, String> studyMap2;

    @Before
    public void setup() {
        studyMap1 = new HashMap<>();
        studyMap1.put("title", "Title1");
        studyMap1.put("type", "Type1");
        studyMap1.put("submitterEmail", "Email1");
        studyMap2 = new HashMap<>();
        studyMap2.put("title", "Title2");
        studyMap2.put("type", "Type2");
        studyMap2.put("submitterEmail", "Email2");
    }

    @Test
    public void testRestApi() {

        AccessioningObject study1 = new AccessionObject(studyMap1);
        AccessioningObject study2 = new AccessionObject(studyMap2);

        String url = "/v1/accession/study";
        HttpEntity<Object> requestEntity = new HttpEntity<>(Arrays.asList(study1, study2));

        ResponseEntity<Set<AccessionObject>> response = testRestTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<Set<AccessionObject>>() {
        });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    public void requestPostTwiceAndWeGetSameAccessions() {

        AccessioningObject study1 = new AccessionObject(studyMap1);
        AccessioningObject study2 = new AccessionObject(studyMap2);

        String url = "/v1/accession/study";
        HttpEntity<Object> requestEntity = new HttpEntity<>(Arrays.asList(study1, study2));

        ResponseEntity<Set<AccessionObject>> response = testRestTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<Set<AccessionObject>>() {
        });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals(2, objectAccessioningRepository.count());

        //Accessing Post Request again with same files
        response = testRestTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<Set<AccessionObject>>() {
        });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals(2, objectAccessioningRepository.count());
    }

}
