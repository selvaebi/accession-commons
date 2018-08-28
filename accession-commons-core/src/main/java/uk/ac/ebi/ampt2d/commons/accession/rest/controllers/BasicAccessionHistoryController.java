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
package uk.ac.ebi.ampt2d.commons.accession.rest.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.ac.ebi.ampt2d.commons.accession.core.HistoryService;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDoesNotExistException;
import uk.ac.ebi.ampt2d.commons.accession.core.models.HistoryEvent;
import uk.ac.ebi.ampt2d.commons.accession.rest.dto.HistoryEventDTO;
import uk.ac.ebi.ampt2d.commons.accession.utils.ListConverter;

import java.util.List;
import java.util.function.Function;

public class BasicAccessionHistoryController<DTO extends MODEL, MODEL, ACCESSION> {

    private HistoryService<MODEL, ACCESSION> service;
    private ListConverter<HistoryEvent<MODEL, ACCESSION>, HistoryEventDTO> converter;

    public BasicAccessionHistoryController(HistoryService<MODEL, ACCESSION> service, Function<MODEL, DTO> modelToDTO) {
        this.service = service;
        this.converter = new ListConverter<>(event -> new HistoryEventDTO(event, modelToDTO));
    }

    @RequestMapping(value = "/{accession}", method = RequestMethod.GET, produces = "application/json")
    public List<HistoryEventDTO> get(@PathVariable ACCESSION accession) throws AccessionDoesNotExistException {
        return converter.convert(service.getHistory(accession));
    }

}
