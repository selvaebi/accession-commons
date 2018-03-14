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
package uk.ac.ebi.ampt2d.commons.generators.monotonic.persistence.service;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.ampt2d.commons.generators.monotonic.persistence.entities.ContiguousIdBlock;
import uk.ac.ebi.ampt2d.commons.generators.monotonic.persistence.repositories.ContiguousIdBlockRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContiguousIdBlockService {

    private ContiguousIdBlockRepository repository;

    public ContiguousIdBlockService(ContiguousIdBlockRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void save(Iterable<ContiguousIdBlock> blocks) {
        repository.save(blocks);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ContiguousIdBlock reserveNewBlock(String categoryId, String instanceId, long size) {
        ContiguousIdBlock lastBlock = repository.findFirstByCategoryIdOrderByEndDesc(categoryId);
        if (lastBlock != null) {
            return repository.save(lastBlock.nextBlock(instanceId, size));
        } else {
            ContiguousIdBlock newBlock = new ContiguousIdBlock(categoryId, instanceId, 0, size);
            return repository.save(newBlock);
        }
    }

    @Transactional(readOnly = true)
    public List<ContiguousIdBlock> getUncompletedBlocksByCategoryIdAndApplicationInstanceIdOrderByEndAsc(
            String categoryId, String applicationInstanceId) {
        try (Stream<ContiguousIdBlock> reservedBlocksOfThisInstance = repository
                .findAllByCategoryIdAndApplicationInstanceIdOrderByEndAsc(categoryId, applicationInstanceId)) {
            return reservedBlocksOfThisInstance.filter(ContiguousIdBlock::isNotFull).collect(Collectors.toList());
        }
    }

}
