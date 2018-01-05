/*
 *
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ampt2d.accession.serial.block.persistence.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * This is the block assignation class used to determine the range of values in a monotonic sequence that a specific
 * instance can assign.
 * <p>
 * It is defined by the start of the monotonic sequence, the end and the last committed value to the database.
 * <p>
 * The last committed value of the block is initialized as the start of the block minus one position to simplify
 * calculus logic in the application.
 */
@Entity
@Table(
        name = "continuous_id_blocks",
        indexes = {
                @Index(name = "CATEGORY_INDEX", columnList = "categoryId")
        }
)
public class ContinuousIdBlock implements Comparable<ContinuousIdBlock> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, length = 255)
    private String categoryId;

    @Column(nullable = false, length = 255)
    private String instanceId;

    private long start;

    private long end;

    private long lastCommitted;

    // Create / update dates

    ContinuousIdBlock() {
        //Hibernate default constructor
    }

    public ContinuousIdBlock(String categoryId, String instanceId, long start, long size) {
        this.categoryId = categoryId;
        this.instanceId = instanceId;
        this.start = start;
        this.end = start + size - 1;
        this.lastCommitted = start - 1;
    }

    public ContinuousIdBlock nextBlock(String instanceId, long size) {
        return new ContinuousIdBlock(categoryId, instanceId, end + 1, size);
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

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public boolean isNotFull() {
        return lastCommitted != end;
    }

    @Override
    public int compareTo(ContinuousIdBlock continuousIdBlock) {
        return Long.compare(start, continuousIdBlock.getStart());
    }
}
