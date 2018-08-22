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

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class AccessionVersionsWrapper<MODEL, HASH, ACCESSION> {

    private ACCESSION accession;

    private List<AccessionWrapper<MODEL, HASH, ACCESSION>> data;

    /**
     * @param models
     * @Throws IllegalArgumentException if model list is null, empty or multiple accessions are find on the models
     */
    public AccessionVersionsWrapper(List<AccessionWrapper<MODEL, HASH, ACCESSION>> models) {
        Assert.notEmpty(models, "One or more data objects required.");
        Set<ACCESSION> accessions = models.stream().map(AccessionWrapper::getAccession).collect(Collectors.toSet());
        Assert.isTrue(accessions.size() == 1, "All model wrappers need to have the same accession value.");

        this.accession = accessions.iterator().next();
        this.data = models.stream()
                .sorted(Comparator.comparingInt(AccessionWrapper::getVersion))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    public ACCESSION getAccession() {
        return accession;
    }

    public List<AccessionWrapper<MODEL, HASH, ACCESSION>> getModelWrappers() {
        return data;
    }

    public Optional<AccessionWrapper<MODEL, HASH, ACCESSION>> getVersion(int version) {
        for (AccessionWrapper<MODEL, HASH, ACCESSION> wrapper : data) {
            if (wrapper.getVersion() == version) {
                return Optional.of(wrapper);
            }
        }
        return Optional.empty();
    }

}