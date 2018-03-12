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
package uk.ac.ebi.ampt2d.accession.commons.generators;

import uk.ac.ebi.ampt2d.accession.commons.core.SaveResponse;
import uk.ac.ebi.ampt2d.accession.commons.generators.exceptions.AccessionCouldNotBeGeneratedException;
import uk.ac.ebi.ampt2d.accession.commons.generators.monotonic.MonotonicAccessionGenerator;

import java.util.List;
import java.util.Map;
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
    public <HASH> List<ModelHashAccession<MODEL, HASH, String>> generateAccessions(Map<HASH, MODEL> messages) throws AccessionCouldNotBeGeneratedException {
        List<ModelHashAccession<MODEL, HASH, ACCESSION>> accessions = generator.generateAccessions(messages);
        return accessions.stream()
                .map(mha -> ModelHashAccession.of(mha.model(), mha.hash(), decorateAccession.apply(mha.accession())))
                .collect(Collectors.toList());
    }

    @Override
    public void postSave(SaveResponse<String, MODEL> response) {
        Map<ACCESSION, MODEL> savedAccession = response.getSavedAccessions().entrySet().stream()
                .collect(Collectors.toMap(o -> undecorateAccession.apply(o.getKey()), o -> o.getValue()));
        Map<ACCESSION, MODEL> unsavedAccession = response.getUnsavedAccessions().entrySet().stream()
                .collect(Collectors.toMap(o -> undecorateAccession.apply(o.getKey()), o -> o.getValue()));
        generator.postSave(new SaveResponse<>(savedAccession, unsavedAccession));
    }

    public static <MODEL> DecoratedAccessionGenerator<MODEL, Long> prefixSuxfixMonotonicAccessionGenerator(
            MonotonicAccessionGenerator<MODEL> generator,
            String prefix, String sufix) {
        return prefixSuxfixAccessionGenerator(generator, prefix, sufix, Long::parseLong);
    }

    public static <MODEL, ACCESSION> DecoratedAccessionGenerator<MODEL, ACCESSION> prefixSuxfixAccessionGenerator(
            AccessionGenerator<MODEL, ACCESSION> generator,
            String prefix, String sufix,
            Function<String, ACCESSION> parseAccession) {
        return new DecoratedAccessionGenerator<>(
                generator,
                accession -> {
                    String value = accession.toString();
                    if (prefix != null) {
                        value = prefix + value;
                    }
                    if (sufix != null) {
                        value = value + sufix;
                    }
                    return value;
                },
                s -> {
                    if (prefix != null) {
                        s = s.substring(prefix.length());
                    }
                    if (sufix != null) {
                        s = s.substring(0, s.length() - sufix.length());
                    }
                    return parseAccession.apply(s);
                });
    }

}
