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
package uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * This class represents a block allocated by an application instance, in a monotonic sequence associated with a
 * category.
 * <p>
 * It is defined by the first and last values of the monotonic sequence, and the last committed value to the database.
 * <p>
 * The last committed value of the block is initialized as the first value of the block minus one, to simplify the
 * application logic that implements the calculations.
 */
@Entity
@Table(
        name = "contiguous_id_blocks",
        indexes = {
                @Index(name = "CATEGORY_INDEX", columnList = "categoryId")
        }
)
public class ContiguousIdBlock implements Comparable<ContiguousIdBlock> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, length = 255)
    private String categoryId;

    @Column(nullable = false, length = 255)
    private String applicationInstanceId;

    private long firstValue;

    private long lastValue;

    private long lastCommitted;

    // Create / update dates

    ContiguousIdBlock() {
        //Hibernate default constructor
    }

    public ContiguousIdBlock(String categoryId, String applicationInstanceId, long firstValue, long size) {
        this.categoryId = categoryId;
        this.applicationInstanceId = applicationInstanceId;
        this.firstValue = firstValue;
        this.lastValue = firstValue + size - 1;
        this.lastCommitted = firstValue - 1;
    }

    public ContiguousIdBlock nextBlock(String instanceId, long size, long nextBlockInterval) {
        return new ContiguousIdBlock(categoryId, instanceId, lastValue + 1 + nextBlockInterval, size);
    }

    public long getId() {
        return id;
    }

    public long getLastCommitted() {
        return lastCommitted;
    }

    public void setLastCommitted(long lastCommitted) {
        this.lastCommitted = lastCommitted;
    }

    public long getFirstValue() {
        return firstValue;
    }

    public long getLastValue() {
        return lastValue;
    }

    public boolean isNotFull() {
        return lastCommitted != lastValue;
    }

    @Override
    public int compareTo(ContiguousIdBlock contiguousIdBlock) {
        return Long.compare(firstValue, contiguousIdBlock.getFirstValue());
    }
}
