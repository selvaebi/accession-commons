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

import uk.ac.ebi.ampt2d.accession.common.utils.DigestFunction;
import uk.ac.ebi.ampt2d.accession.common.utils.HashingFunction;
import uk.ac.ebi.ampt2d.accession.common.accessioning.SaveResponse;
import uk.ac.ebi.ampt2d.accession.common.hashing.SHA1HashingFunction;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Base class for accession generators that don't suffer performance penalty if they don't generate the accessions
 * in batches
 *
 * @param <MODEL>
 * @param <ACCESSION>
 */
public class SingleAccessionGenerator<MODEL, ACCESSION> implements AccessionGenerator<MODEL, ACCESSION> {

    private final Function<MODEL, ACCESSION> generateAccessionFunction;

    public SingleAccessionGenerator(Function<MODEL, ACCESSION> generateAccessionFunction) {
        this.generateAccessionFunction = generateAccessionFunction;
    }

    @Override
    public <HASH> List<ModelHashAccession<MODEL, HASH, ACCESSION>> generateAccessions(Map<HASH, MODEL> messages) {
        return messages.entrySet()
                .stream()
                .map(entry -> ModelHashAccession.of(entry.getValue(), entry.getKey(),
                        generateAccessionFunction.apply(entry.getValue())))
                .collect(Collectors.toList());
    }

    @Override
    public void postSave(SaveResponse<ACCESSION, MODEL> response) {
        // No action performed, as all the accessions are generated on the fly.
    }

    public static <MESSAGE, ACCESSION> SingleAccessionGenerator<MESSAGE, ACCESSION> ofHashAccessionGenerator(
            DigestFunction<MESSAGE> digestFunction,
            HashingFunction<ACCESSION> hashingFunction) {
        return new SingleAccessionGenerator<>(message -> digestFunction.andThen(hashingFunction).apply(message));
    }

    public static <MESSAGE> SingleAccessionGenerator<MESSAGE, String> ofSHA1AccessionGenerator(
            DigestFunction<MESSAGE> digestFunction){
        return ofHashAccessionGenerator(digestFunction, new SHA1HashingFunction());
    }

}
