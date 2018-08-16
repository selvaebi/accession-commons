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
package uk.ac.ebi.ampt2d.commons.accession.core.models;

import uk.ac.ebi.ampt2d.commons.accession.persistence.models.IAccessionedObject;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Definition of an event triggered when an operation is performed over an accessioned object.
 * Some operations could make some objects become inactive.
 *
 * @param <MODEL>
 * @param <ACCESSION>
 */
public interface IEvent<MODEL, ACCESSION> {

    ACCESSION getAccession();

    ACCESSION getMergedInto();

    EventType getEventType();

    String getReason();

    LocalDateTime getCreatedDate();

    List<? extends IAccessionedObject<MODEL, ?, ACCESSION>> getInactiveObjects();

}
