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

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.ampt2d.commons.accession.core.AccessioningService;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionCouldNotBeGeneratedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDoesNotExistException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.HashAlreadyExistsException;

import javax.validation.Valid;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BasicRestController<DTO extends MODEL, MODEL, HASH, ACCESSION> {

    private AccessioningService<MODEL, HASH, ACCESSION> service;
    private Function<MODEL, DTO> modelToDTO;

    public BasicRestController(AccessioningService<MODEL, HASH, ACCESSION> service,
                               Function<MODEL, DTO> modelToDTO) {
        this.service = service;
        this.modelToDTO = modelToDTO;
    }

    protected AccessioningService<MODEL, HASH, ACCESSION> getService() {
        return service;
    }

    protected Function<MODEL, DTO> getModelToDTO() {
        return modelToDTO;
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json",
            consumes = "application/json")
    public List<AccessionResponseDTO<DTO, MODEL, HASH, ACCESSION>> generateAccessions(@RequestBody @Valid List<DTO> dtos)
            throws AccessionCouldNotBeGeneratedException {
        return service.getOrCreateAccessions(dtos).stream()
                .map(accessionModel -> new AccessionResponseDTO<>(accessionModel, modelToDTO))
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/{accessions}", method = RequestMethod.GET, produces = "application/json")
    public List<AccessionResponseDTO<DTO, MODEL, HASH, ACCESSION>> get(@PathVariable List<ACCESSION> accessions,
                                                                       @RequestParam(name = "hideDeprecated", required = false,
                                                                               defaultValue = "false")
                                                                               boolean hideDeprecated) {
        return service.getByAccessions(accessions, hideDeprecated).stream()
                .map(accessionModel -> new AccessionResponseDTO<>(accessionModel, modelToDTO))
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/{accession}", method = RequestMethod.POST, produces = "application/json",
            consumes = "application/json")
    public AccessionResponseDTO<DTO, MODEL, HASH, ACCESSION> update(@PathVariable ACCESSION accession,
                                                                    @RequestBody @Valid DTO dto)
            throws AccessionDoesNotExistException, HashAlreadyExistsException {
        return new AccessionResponseDTO<>(service.update(accession, dto), modelToDTO);
    }

}
