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
package uk.ac.ebi.ampt2d.commons.accession.rest.dto;

import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionVersionsWrapper;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AccessionVersionsResponseDTO<DTO, MODEL, HASH, ACCESSION> {

    private ACCESSION accession;

    private Map<Integer, DTO> versions;

    AccessionVersionsResponseDTO() {
    }

    public AccessionVersionsResponseDTO(AccessionVersionsWrapper<MODEL, HASH, ACCESSION> wrapper,
                                        Function<MODEL, DTO> modelToDto) {
        this.accession = wrapper.getAccession();
        this.versions = wrapper.getModelWrappers().stream()
                .collect(Collectors.toMap(o -> o.getVersion(), o -> modelToDto.apply(o.getData())));
    }

    public ACCESSION getAccession() {
        return accession;
    }

    public Map<Integer, DTO> getVersions() {
        return versions;
    }

}