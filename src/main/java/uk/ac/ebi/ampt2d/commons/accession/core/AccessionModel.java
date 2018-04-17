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

/**
 * Wrapper containing the object that has been accessioned, as well as additional information like the accession or a
 * flag indicating whether the accession is active. To be used in the service layer.
 *
 * @param <MODEL>
 * @param <HASH>
 * @param <ACCESSION>
 */
public class AccessionModel<MODEL, HASH, ACCESSION> {

    private ACCESSION accession;

    private HASH hash;

    private boolean active;

    private MODEL data;

    public AccessionModel(ACCESSION accession, HASH hash, boolean active, MODEL data) {
        this.accession = accession;
        this.hash = hash;
        this.active = active;
        this.data = data;
    }

    public ACCESSION getAccession() {
        return accession;
    }

    public HASH getHash() {
        return hash;
    }

    public boolean isActive() {
        return active;
    }

    public MODEL getData() {
        return data;
    }

    public static <MODEL, HASH, ACCESSION> AccessionModel<MODEL, HASH, ACCESSION> of(ACCESSION accession, HASH hash,
                                                                                     MODEL data) {
        return new AccessionModel<>(accession, hash, true, data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccessionModel)) return false;

        AccessionModel<?, ?, ?> that = (AccessionModel<?, ?, ?>) o;

        if (active != that.active) return false;
        if (!accession.equals(that.accession)) return false;
        return hash.equals(that.hash);
    }

    @Override
    public int hashCode() {
        int result = accession.hashCode();
        result = 31 * result + hash.hashCode();
        result = 31 * result + (active ? 1 : 0);
        return result;
    }
}
