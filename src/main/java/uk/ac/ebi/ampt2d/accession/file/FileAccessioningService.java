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

package uk.ac.ebi.ampt2d.accession.file;

import uk.ac.ebi.ampt2d.accession.common.generators.SingleAccessionGenerator;
import uk.ac.ebi.ampt2d.accession.common.accessioning.BasicAccessioningService;
import uk.ac.ebi.ampt2d.accession.common.persistence.DatabaseService;
import uk.ac.ebi.ampt2d.accession.common.hashing.SHA1HashingFunction;

public class FileAccessioningService extends BasicAccessioningService<FileModel, String, String> {

    public FileAccessioningService(DatabaseService<FileModel, String, String> dbService) {
        super(new SingleAccessionGenerator<>(fileModel -> fileModel.getHash()),
                dbService,
                fileModel -> fileModel.getHash(),
                message -> message);
    }

}
