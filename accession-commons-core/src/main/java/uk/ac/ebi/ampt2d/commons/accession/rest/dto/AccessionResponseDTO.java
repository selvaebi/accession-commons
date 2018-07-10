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

import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionWrapper;

import java.util.function.Function;

/**
 * Response containing the object that has been accessioned, as well as additional information like the accession or a
 * flag indicating whether the accession is active. To be used at the REST API layer.
 *
 * @param <DTO>
 * @param <MODEL>
 * @param <HASH>
 * @param <ACCESSION>
 */
public class AccessionResponseDTO<DTO, MODEL, HASH, ACCESSION> {

    private ACCESSION accession;

    private int version;

    private DTO data;

    AccessionResponseDTO() {
    }

    public AccessionResponseDTO(AccessionWrapper<MODEL, HASH, ACCESSION> accessionWrapper, Function<MODEL, DTO> modelToDto) {
        this.accession = accessionWrapper.getAccession();
        this.version = accessionWrapper.getVersion();
        this.data = modelToDto.apply(accessionWrapper.getData());
    }

    public ACCESSION getAccession() {
        return accession;
    }

    public int getVersion() {
        return version;
    }

    public DTO getData() {
        return data;
    }
}
