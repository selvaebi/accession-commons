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
package uk.ac.ebi.ampt2d.accession.common.generators;

import uk.ac.ebi.ampt2d.accession.common.accessioning.SaveResponse;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generator that can encapsulate another generator with the purpose of decorating the output accession
 *
 * @param <MODEL>
 * @param <DECORATED_ACCESSION>
 * @param <ACCESSION>
 */
public class DecoratedGenerator<MODEL, DECORATED_ACCESSION, ACCESSION> implements
        AccessionGenerator<MODEL, DECORATED_ACCESSION> {

    private final AccessionGenerator<MODEL, ACCESSION> generator;

    private final Function<ACCESSION, DECORATED_ACCESSION> decorationFunction;

    private final Function<DECORATED_ACCESSION, ACCESSION> removeDecorationFunction;

    public DecoratedGenerator(AccessionGenerator<MODEL, ACCESSION> generator,
                              Function<ACCESSION, DECORATED_ACCESSION> decorationFunction,
                              Function<DECORATED_ACCESSION, ACCESSION> removeDecorationFunction) {
        this.generator = generator;
        this.decorationFunction = decorationFunction;
        this.removeDecorationFunction = removeDecorationFunction;
    }

    @Override
    public <HASH> List<ModelHashAccession<MODEL, HASH, DECORATED_ACCESSION>> generateAccessions(
            Map<HASH, MODEL> messages) {
        return decorate(generator.generateAccessions(messages));
    }

    private <HASH> List<ModelHashAccession<MODEL, HASH, DECORATED_ACCESSION>> decorate(
            List<ModelHashAccession<MODEL, HASH, ACCESSION>> modelHashAccessions) {
        return modelHashAccessions.stream()
                .map(threeTuple -> ModelHashAccession.of(
                        threeTuple.model(),
                        threeTuple.hash(),
                        decorationFunction.apply(threeTuple.accession())))
                .collect(Collectors.toList());
    }

    @Override
    public void postSave(SaveResponse<DECORATED_ACCESSION, MODEL> response) {
        generator.postSave(new SaveResponse<>(removeDecoration(response.getSavedAccessions()),
                removeDecoration(response.getUnsavedAccessions()),
                removeDecoration(response.getAccessionOfUnsavedMessages())));
    }

    private Map<ACCESSION, MODEL> removeDecoration(Map<DECORATED_ACCESSION, MODEL> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(e -> removeDecorationFunction.apply(e.getKey()), e -> e.getValue()));
    }

}
