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
package uk.ac.ebi.ampt2d.commons.accession.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.ac.ebi.ampt2d.test.configuration.BasicRestControllerTestConfiguration;
import uk.ac.ebi.ampt2d.test.rest.BasicRestModel;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@ContextConfiguration(classes = {BasicRestControllerTestConfiguration.class})
public class BasicRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<BasicRestModel> jsonModel;

    @Autowired
    private JacksonTester<List<BasicRestModel>> jsonModelList;

    @Autowired
    private JacksonTester<List<AccessionResponseDTO<BasicRestModel, BasicRestModel, String, String>>> jsonAccessions;

    @Test
    public void testNoContentIfAccessioningDoesNotExist() throws Exception {
        mockMvc.perform(get("/v1/test/notExistingId").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    public void testAccessionOk() throws Exception {
        mockMvc.perform(post("/v1/test")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(jsonModelList.write(Arrays.asList(new BasicRestModel("simpleTest"))).getJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].data.value", containsInAnyOrder("simpleTest")));
    }

    @Test
    public void testMultipleAccessionOk() throws Exception {
        mockMvc.perform(post("/v1/test")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(jsonModelList.write(Arrays.asList(
                        new BasicRestModel("simpleTest2"),
                        new BasicRestModel("simpleTest3"))).getJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].data.value", containsInAnyOrder("simpleTest2", "simpleTest3")));
    }

    @Test
    public void testThrowExceptions() throws Exception {
        mockMvc.perform(post("/v1/test")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(jsonModelList.write(
                        Arrays.asList(new BasicRestModel("MissingUnsavedAccessionsException"))).getJson()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.exception")
                        .value("uk.ac.ebi.ampt2d.commons.accession.core.exceptions.MissingUnsavedAccessionsException"));
        mockMvc.perform(post("/v1/test")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(jsonModelList.write(
                        Arrays.asList(new BasicRestModel("AccessionIsNotPendingException"))).getJson()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.exception")
                        .value("uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionIsNotPendingException"));
        mockMvc.perform(post("/v1/test")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(jsonModelList.write(
                        Arrays.asList(new BasicRestModel("AccessionCouldNotBeGeneratedException"))).getJson()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.exception")
                        .value("uk.ac.ebi.ampt2d.commons.accession.core.exceptions" +
                                ".AccessionCouldNotBeGeneratedException"));
    }

    @Test
    public void testUpdate() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(post("/v1/test")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(jsonModelList.write(Arrays.asList(new BasicRestModel("update-test-1"))).getJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].data.value", containsInAnyOrder("update-test-1")))
                .andReturn();

        doUpdate(jsonAccessions.parseObject(mvcResult.getResponse().getContentAsString()).get(0).getAccession(),
                new BasicRestModel("update-test-1b"));
    }

    private void doUpdate(String accession, BasicRestModel basicRestModel) throws Exception {
        mockMvc.perform(post("/v1/test/" + accession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(jsonModel.write(basicRestModel).getJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.value").value(basicRestModel.getValue()))
                .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    public void testAccessionDoesNotExistsUpdate() throws Exception {
        mockMvc.perform(post("/v1/test/doesnotexist")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(jsonModel.write(new BasicRestModel("update-test-1b")).getJson()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testHashCollisionUpdate() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(post("/v1/test")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(jsonModelList.write(Arrays.asList(
                        new BasicRestModel("update-test-2"),
                        new BasicRestModel("update-test-3"))).getJson()))
                .andExpect(status().isOk())
                .andReturn();

        String accession = jsonAccessions.parseObject(mvcResult.getResponse().getContentAsString()).get(1)
                .getAccession();
        mockMvc.perform(post("/v1/test/" + accession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(jsonModel.write(new BasicRestModel("update-test-2")).getJson()))
                .andExpect(status().isConflict());
    }

    @Test
    public void testGetAccessionVersion() throws Exception{
        final MvcResult mvcResult = mockMvc.perform(post("/v1/test")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(jsonModelList.write(Arrays.asList(
                        new BasicRestModel("get-accession-version-test-1"))).getJson()))
                .andExpect(status().isOk())
                .andReturn();
        String accession = jsonAccessions.parseObject(mvcResult.getResponse().getContentAsString()).get(0)
                .getAccession();
        doUpdate(accession,new BasicRestModel("get-accession-version-test-1b"));

        mockMvc.perform(get("/v1/test/"+accession+"/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].data.value", containsInAnyOrder("get-accession-version-test-1")));
        mockMvc.perform(get("/v1/test/"+accession+"/2").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].data.value", containsInAnyOrder("get-accession-version-test-1b")));
    }

    @Test
    public void testGetAccessionVersionNotExists() throws Exception{
        final MvcResult mvcResult = mockMvc.perform(post("/v1/test")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(jsonModelList.write(Arrays.asList(
                        new BasicRestModel("get-accession-version-test-2"))).getJson()))
                .andExpect(status().isOk())
                .andReturn();
        String accession = jsonAccessions.parseObject(mvcResult.getResponse().getContentAsString()).get(0)
                .getAccession();
        doUpdate(accession,new BasicRestModel("get-accession-version-test-2b"));

        mockMvc.perform(get("/v1/test/notexists/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/v1/test/"+accession+"/3").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAccession() throws Exception{
        final MvcResult mvcResult = mockMvc.perform(post("/v1/test")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(jsonModelList.write(Arrays.asList(
                        new BasicRestModel("get-accession-test-1"))).getJson()))
                .andExpect(status().isOk())
                .andReturn();
        String accession = jsonAccessions.parseObject(mvcResult.getResponse().getContentAsString()).get(0)
                .getAccession();
        doUpdate(accession,new BasicRestModel("get-accession-test-1b"));

        mockMvc.perform(get("/v1/test/"+accession).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].data.value", containsInAnyOrder("get-accession-test-1b")));
    }

}
