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
package uk.ac.ebi.ampt2d.commons.accession.persistence.services;

import uk.ac.ebi.ampt2d.commons.accession.core.models.EventType;
import uk.ac.ebi.ampt2d.commons.accession.persistence.models.IAccessionedObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.ac.ebi.ampt2d.commons.accession.core.models.EventType.DEPRECATED;
import static uk.ac.ebi.ampt2d.commons.accession.core.models.EventType.MERGED;
import static uk.ac.ebi.ampt2d.commons.accession.core.models.EventType.PATCHED;
import static uk.ac.ebi.ampt2d.commons.accession.core.models.EventType.UPDATED;

public abstract class BasicInactiveAccessionService<
        MODEL,
        ACCESSION extends Serializable,
        ACCESSION_ENTITY extends IAccessionedObject<MODEL, ?, ACCESSION>,
        ACCESSION_INACTIVE_ENTITY extends IAccessionedObject<MODEL, ?, ACCESSION>
        >
        implements InactiveAccessionService<MODEL, ACCESSION, ACCESSION_ENTITY> {

    private Function<ACCESSION_ENTITY, ACCESSION_INACTIVE_ENTITY> toInactiveEntity;

    public BasicInactiveAccessionService(
            Function<ACCESSION_ENTITY, ACCESSION_INACTIVE_ENTITY> toInactiveEntity) {
        this.toInactiveEntity = toInactiveEntity;
    }

    @Override
    public void update(ACCESSION_ENTITY entity, String reason) {
        saveHistory(UPDATED, entity.getAccession(), reason, Arrays.asList(toInactiveEntity.apply(entity)));
    }

    private void saveHistory(EventType type, ACCESSION accession, String reason,
                             List<ACCESSION_INACTIVE_ENTITY> entities) {
        saveHistory(type, accession, null, reason, entities);
    }

    @Override
    public void deprecate(ACCESSION accession, Collection<ACCESSION_ENTITY> accessionEntities, String reason) {
        saveHistory(DEPRECATED, accession, reason, toInactiveEntities(accessionEntities));
    }

    private List<ACCESSION_INACTIVE_ENTITY> toInactiveEntities(Collection<ACCESSION_ENTITY> accessionEntities) {
        return accessionEntities.stream().map(toInactiveEntity).collect(Collectors.toList());
    }

    @Override
    public void merge(ACCESSION accession, ACCESSION mergeInto,
                      List<ACCESSION_ENTITY> accessionEntities, String reason) {
        saveHistory(MERGED, accession, mergeInto, reason,
                toInactiveEntities(accessionEntities));
    }

    @Override
    public void patch(ACCESSION accession, String reason) {
        saveHistory(PATCHED, accession, reason, null);
    }

    protected abstract void saveHistory(EventType type, ACCESSION accession, ACCESSION mergeInto,
                                        String reason, List<ACCESSION_INACTIVE_ENTITY> entities);
}
