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

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.ac.ebi.ampt2d.commons.accession.core.DeprecatingAccessionService;

import java.util.List;

public class BasicDeprecatingAccessionRestController<MODEL, ACCESSION> {

    DeprecatingAccessionService<MODEL, ?, ACCESSION> deprecatingAccessionService;

    public BasicDeprecatingAccessionRestController(DeprecatingAccessionService<MODEL, ?, ACCESSION> deprecatingAccessionService) {
        this.deprecatingAccessionService = deprecatingAccessionService;
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json",
            consumes = "application/json")
    public void depricateAccessions(@RequestBody List<ACCESSION> accessions) {
        deprecatingAccessionService.deprecateAccessions(accessions);
    }
}
