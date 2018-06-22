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
package uk.ac.ebi.ampt2d.commons.accession;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.commons.accession.core.AccessioningService;
import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionWrapper;
import uk.ac.ebi.ampt2d.test.TestModel;
import uk.ac.ebi.ampt2d.test.configuration.TestJpaDatabaseServiceTestConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestJpaDatabaseServiceTestConfiguration.class})
public class BagCucumberStepDefinitions {

    @Autowired
    private AccessioningService<TestModel, String, String> accessioningService;

    private List<AccessionWrapper<TestModel, String, String>> lastGetOrCreateOperation;

    @Given("^the bag is empty$")
    public void the_bag_is_empty() {

    }

    @When("^I put (\\d+) (\\w+) in the bag$")
    public void i_put_something_in_the_bag(final int quantity, final String something) {

    }

    @Then("^the bag should contain only (\\d+) (\\w+)$")
    public void the_bag_should_contain_only_something(final int quantity, final String something) {
    }

    @Then("^the bag should contain (\\d+) (\\w+)$")
    public void the_bag_should_contain_something(final int quantity, final String something) {
    }

    @When("^I submit (\\w+,*)+ to accessioning service$")
    public void iSubmitObjectsToAccessioningService(String objects) throws Throwable {
        List<TestModel> models = Arrays.stream(objects.split(",")).map(TestModel::of).collect(Collectors.toList());
        lastGetOrCreateOperation = accessioningService.getOrCreate(models);
    }

    @Then("^I should receive accessions (\\w+,*)+$")
    public void iShouldReceiveAccessions(String objects) throws Throwable {
        final String[] split = objects.split(",");
        Assert.assertEquals(split.length, lastGetOrCreateOperation.size());
        lastGetOrCreateOperation
    }
}
