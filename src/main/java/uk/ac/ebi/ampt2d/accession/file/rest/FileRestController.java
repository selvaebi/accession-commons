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
package uk.ac.ebi.ampt2d.accession.file.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.ampt2d.accession.common.rest.BasicRestController;
import uk.ac.ebi.ampt2d.accession.file.FileAccessioningService;
import uk.ac.ebi.ampt2d.accession.file.FileModel;

@RestController
@RequestMapping(value = "/v1/file")
@ConditionalOnProperty(name = "services", havingValue = "file-accession")
public class FileRestController extends BasicRestController<FileModel, FileDTO, String> {

    public FileRestController(FileAccessioningService fileAccessioningService) {
        super(fileAccessioningService, new FileModelToDTO());
    }

}
