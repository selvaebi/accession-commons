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
package uk.ac.ebi.ampt2d.commons.accession.generators;

import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.models.SaveResponse;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionCouldNotBeGeneratedException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DecoratedAccessionGenerator<MODEL, ACCESSION> implements AccessionGenerator<MODEL, String> {

    private AccessionGenerator<MODEL, ACCESSION> generator;

    private Function<ACCESSION, String> decorateAccession;

    private Function<String, ACCESSION> undecorateAccession;

    public DecoratedAccessionGenerator(AccessionGenerator<MODEL, ACCESSION> generator,
                                       Function<ACCESSION, String> decorateAccession,
                                       Function<String, ACCESSION> undecorateAccession) {
        this.generator = generator;
        this.decorateAccession = decorateAccession;
        this.undecorateAccession = undecorateAccession;
    }

    @Override
    public <HASH> List<AccessionWrapper<MODEL, HASH, String>> generateAccessions(Map<HASH, MODEL> messages) throws
            AccessionCouldNotBeGeneratedException {
        List<AccessionWrapper<MODEL, HASH, ACCESSION>> accessions = generator.generateAccessions(messages);
        return accessions.stream()
                .map(mha -> new AccessionWrapper<>(decorateAccession.apply(mha.getAccession()), mha.getHash(),
                        mha.getData()))
                .collect(Collectors.toList());
    }

    @Override
    public void postSave(SaveResponse<String> response) {
        Set<ACCESSION> savedAccessions = response.getSavedAccessions().stream().map(undecorateAccession)
                .collect(Collectors.toSet());
        Set<ACCESSION> saveFailedAccessions = response.getSaveFailedAccessions().stream().map(undecorateAccession)
                .collect(Collectors.toSet());
        generator.postSave(new SaveResponse<>(savedAccessions, saveFailedAccessions));
    }

    public static <MODEL, ACCESSION> DecoratedAccessionGenerator<MODEL, ACCESSION> buildPrefixSuffixAccessionGenerator(
            AccessionGenerator<MODEL, ACCESSION> generator,
            String prefix, String suffix,
            Function<String, ACCESSION> parseAccession) {
        return new DecoratedAccessionGenerator<>(
                generator,
                accession -> {
                    String value = accession.toString();
                    if (prefix != null) {
                        value = prefix + value;
                    }
                    if (suffix != null) {
                        value = value + suffix;
                    }
                    return value;
                },
                s -> {
                    if (prefix != null) {
                        s = s.substring(prefix.length());
                    }
                    if (suffix != null) {
                        s = s.substring(0, s.length() - suffix.length());
                    }
                    return parseAccession.apply(s);
                });
    }

}
