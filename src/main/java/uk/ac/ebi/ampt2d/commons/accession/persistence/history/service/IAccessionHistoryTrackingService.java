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
package uk.ac.ebi.ampt2d.commons.accession.persistence.history.service;

/**
 * Interface for services that allow to interact with the components to track the history of accessions.
 *
 * @param <ACCESSION>
 */
public interface IAccessionHistoryTrackingService<ACCESSION> {

    void merge(String reason, ACCESSION... accessions);

    void update(String reason, ACCESSION... accessions);

    void deprecate(String reason, ACCESSION... accessions);

}
