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
package uk.ac.ebi.ampt2d.accession.commons.generators.monotonic.persistence.entities;

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
 * It is defined by the start of the monotonic sequence, the end and the last committed value to the database.
 * <p>
 * The last committed value of the block is initialized as the start position of the block minus one, to simplify the
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

    private long start;

    private long end;

    private long lastCommitted;

    // Create / update dates

    ContiguousIdBlock() {
        //Hibernate default constructor
    }

    public ContiguousIdBlock(String categoryId, String applicationInstanceId, long start, long size) {
        this.categoryId = categoryId;
        this.applicationInstanceId = applicationInstanceId;
        this.start = start;
        this.end = start + size - 1;
        this.lastCommitted = start - 1;
    }

    public ContiguousIdBlock nextBlock(String instanceId, long size) {
        return new ContiguousIdBlock(categoryId, instanceId, end + 1, size);
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
    public int compareTo(ContiguousIdBlock contiguousIdBlock) {
        return Long.compare(start, contiguousIdBlock.getStart());
    }
}
