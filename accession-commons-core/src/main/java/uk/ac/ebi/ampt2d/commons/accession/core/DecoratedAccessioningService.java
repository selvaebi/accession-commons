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
package uk.ac.ebi.ampt2d.commons.accession.core;

import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionCouldNotBeGeneratedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDeprecatedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDoesNotExistException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionMergedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.HashAlreadyExistsException;
import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionVersionsWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionWrapper;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DecoratedAccessioningService<MODEL, HASH, DB_ACCESSION, ACCESSION>
        implements AccessioningService<MODEL, HASH, ACCESSION> {

    private final AccessioningService<MODEL, HASH, DB_ACCESSION> service;
    private final Function<DB_ACCESSION, ACCESSION> decoratingFunction;
    private final Function<ACCESSION, DB_ACCESSION> parsingFunction;

    public DecoratedAccessioningService(AccessioningService<MODEL, HASH, DB_ACCESSION> service,
                                        Function<DB_ACCESSION, ACCESSION> decoratingFunction,
                                        Function<ACCESSION, DB_ACCESSION> parsingFunction) {
        this.service = service;
        this.decoratingFunction = decoratingFunction;
        this.parsingFunction = parsingFunction;
    }

    @Override
    public List<AccessionWrapper<MODEL, HASH, ACCESSION>> getOrCreate(List<? extends MODEL> messages)
            throws AccessionCouldNotBeGeneratedException {
        return decorate(service.getOrCreate(messages));
    }

    private List<AccessionWrapper<MODEL, HASH, ACCESSION>> decorate(
            List<AccessionWrapper<MODEL, HASH, DB_ACCESSION>> accessionWrappers) {
        return accessionWrappers.stream().map(this::decorate).collect(Collectors.toList());
    }

    private AccessionWrapper<MODEL, HASH, ACCESSION> decorate(AccessionWrapper<MODEL, HASH, DB_ACCESSION> wrapper) {
        return new AccessionWrapper<>(decoratingFunction.apply(wrapper.getAccession()), wrapper.getHash(),
                wrapper.getData(), wrapper.getVersion());
    }

    @Override
    public List<AccessionWrapper<MODEL, HASH, ACCESSION>> get(List<? extends MODEL> accessionedObjects) {
        return decorate(service.get(accessionedObjects));
    }

    @Override
    public AccessionWrapper<MODEL, HASH, ACCESSION> getByAccession(ACCESSION accession)
            throws AccessionDoesNotExistException,AccessionMergedException, AccessionDeprecatedException {
        return decorate(service.getByAccession(parse(accession)));
    }

    private List<DB_ACCESSION> parse(List<ACCESSION> accessions) {
        return accessions.stream().map(parsingFunction).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public AccessionWrapper<MODEL, HASH, ACCESSION> getByAccessionAndVersion(ACCESSION accession, int version)
            throws AccessionDoesNotExistException, AccessionMergedException, AccessionDeprecatedException {
        return decorate(service.getByAccessionAndVersion(parse(accession), version));
    }

    private DB_ACCESSION parse(ACCESSION accession) throws AccessionDoesNotExistException {
        DB_ACCESSION dbAccession = parsingFunction.apply(accession);
        if (dbAccession == null) {
            throw new AccessionDoesNotExistException(accession);
        }
        return dbAccession;

    }

    @Override
    public AccessionVersionsWrapper<MODEL, HASH, ACCESSION> update(ACCESSION accession, int version, MODEL message)
            throws AccessionDoesNotExistException, HashAlreadyExistsException, AccessionDeprecatedException,
            AccessionMergedException {
        return decorate(service.update(parse(accession), version, message));
    }

    private AccessionVersionsWrapper<MODEL, HASH, ACCESSION> decorate(
            AccessionVersionsWrapper<MODEL, HASH, DB_ACCESSION> accessionVersions) {
        return new AccessionVersionsWrapper<>(decorate(accessionVersions.getModelWrappers()));
    }

    @Override
    public AccessionVersionsWrapper<MODEL, HASH, ACCESSION> patch(ACCESSION accession, MODEL message)
            throws AccessionDoesNotExistException, HashAlreadyExistsException, AccessionDeprecatedException,
            AccessionMergedException {
        return decorate(service.patch(parse(accession), message));
    }

    @Override
    public void deprecate(ACCESSION accession, String reason) throws AccessionMergedException,
            AccessionDoesNotExistException, AccessionDeprecatedException {
        service.deprecate(parse(accession), reason);
    }

    @Override
    public void merge(ACCESSION accessionOrigin, ACCESSION mergeInto, String reason) throws AccessionMergedException,
            AccessionDoesNotExistException, AccessionDeprecatedException {
        service.merge(parse(accessionOrigin), parse(mergeInto), reason);
    }

    public static <MODEL, HASH, DB_ACCESSION> DecoratedAccessioningService<MODEL, HASH, DB_ACCESSION, String>
    buildPrefixAccessionService(AccessioningService<MODEL, HASH, DB_ACCESSION> service, String prefix,
                                Function<String, DB_ACCESSION> parseFunction) {
        return new DecoratedAccessioningService<>(service, accession -> prefix + accession,
                s -> {
                    if (s.length() <= prefix.length() || !Objects.equals(s.substring(0, prefix.length()), prefix)) {
                        return null;
                    }
                    return parseFunction.apply(s.substring(prefix.length()));
                });
    }

    public static <MODEL, HASH> DecoratedAccessioningService<MODEL, HASH, Long, String>
    buildPrefixPaddedLongAccessionService(AccessioningService<MODEL, HASH, Long> service, String prefix,
                                          String padFormat, Function<String, Long> parseFunction) {
        return new DecoratedAccessioningService<>(service, accession -> prefix + String.format(padFormat, accession),
                s -> {
                    if (s.length() <= prefix.length() || !Objects.equals(s.substring(0, prefix.length()), prefix)) {
                        return null;
                    }
                    return parseFunction.apply(s.substring(prefix.length()));
                });
    }

}