/*
 *
 * Copyright 2017 EMBL - European Bioinformatics Institute
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

import uk.ac.ebi.ampt2d.accession.GenericDatabaseService;

import java.util.Map;

public class FileAccessioningDatabaseService extends GenericDatabaseService<FileMessage, FileEntity> {

    @Override
    public FileMessage toMessage(FileEntity fileEntity) {
        return new FileMessage(fileEntity.getHashedMessage());
    }

    @Override
    public FileEntity toEntity(Map.Entry<FileMessage, String> entry) {
        return new FileEntity(hashMessage(entry.getKey().getHash()), entry.getValue());
    }

    @Override
    public String hashMessage(String message) {
        return message;
    }
}
