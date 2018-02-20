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
package uk.ac.ebi.ampt2d.accession.common.rest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.ac.ebi.ampt2d.accession.common.accessioning.BasicAccessioningService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BasicRestController<MODEL, DTO extends MODEL, ACCESSIONING> {

    private BasicAccessioningService<MODEL, ?, ACCESSIONING> service;
    private Function<MODEL, DTO> modelToDTO;

    public BasicRestController(BasicAccessioningService<MODEL, ?, ACCESSIONING> service,
                               Function<MODEL, DTO> modelToDTO) {
        this.service = service;
        this.modelToDTO = modelToDTO;
    }

    protected BasicAccessioningService<MODEL, ?, ACCESSIONING> getService() {
        return service;
    }

    protected Function<MODEL, DTO> getModelToDTO() {
        return modelToDTO;
    }

    @RequestMapping(value = "/generateAccession", method = RequestMethod.POST, produces = "application/json",
            consumes = "application/json")
    public Map<ACCESSIONING, DTO> generateAccessions(@RequestBody List<DTO> DTOS) {
        return service.getOrCreateAccessions(DTOS).entrySet().stream().collect(Collectors.toMap(o -> o.getKey(),
                o -> modelToDTO.apply(o.getValue())));
    }

    @RequestMapping(value = "/{accessions}", method = RequestMethod.GET, produces = "application/json")
    public Map<ACCESSIONING, DTO> get(@PathVariable List<ACCESSIONING> accessions) {
        return service.getByAccessions(accessions).entrySet().stream().collect(Collectors.toMap(o -> o.getKey(),
                o ->modelToDTO.apply(o.getValue())));
    }

}
