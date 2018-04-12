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
package uk.ac.ebi.ampt2d.test.persistence;

import uk.ac.ebi.ampt2d.commons.accession.core.AccessionModel;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.accession.entities.AccessionedLongEntity;
import uk.ac.ebi.ampt2d.test.TestModel;

import javax.persistence.Entity;

@Entity
public class TestMonotonicEntity extends AccessionedLongEntity implements TestModel {

    private String something;

    TestMonotonicEntity() {
        super(null, null, true);
    }

    public TestMonotonicEntity(AccessionModel<TestModel, String, Long> triple) {
        this(triple.getAccession(), triple.getHash(), triple.isActive(), triple.getData().getSomething());
    }

    public TestMonotonicEntity(Long accession, String hashedMessage, boolean active, String something) {
        super(hashedMessage, accession, active);
        this.something = something;
    }

    @Override
    public String getSomething() {
        return something;
    }

}
