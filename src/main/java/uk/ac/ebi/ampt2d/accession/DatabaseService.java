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
package uk.ac.ebi.ampt2d.accession;

import java.util.List;
import java.util.Map;

public interface DatabaseService<MESSAGE_TYPE, ENTITY_TYPE> {

    Map<MESSAGE_TYPE, String> findObjectsInDB(List<MESSAGE_TYPE> hashes);

    void save(Map<MESSAGE_TYPE, String> accessioningObjects);

    MESSAGE_TYPE toMessage(ENTITY_TYPE studyEntity);

    ENTITY_TYPE toEntity(Map.Entry<MESSAGE_TYPE, String> entry);

    String hashMessage(String message);

}
