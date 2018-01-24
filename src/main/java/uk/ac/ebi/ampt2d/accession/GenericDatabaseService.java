package uk.ac.ebi.ampt2d.accession;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class GenericDatabaseService<MESSAGE_TYPE extends Message, ENTITY_TYPE extends AccessionableEntity> implements DatabaseService<MESSAGE_TYPE, ENTITY_TYPE> {

    @Autowired
    private AccessioningRepository<ENTITY_TYPE, String> repository;

    @Override
    public Map<MESSAGE_TYPE, String> findObjectsInDB(List<MESSAGE_TYPE> accessionObjects) {
        List<String> hashes = accessionObjects.stream().map(obj ->
                hashMessage(obj.getMessage())).collect(Collectors.toList());
        Collection<ENTITY_TYPE> studyEntities = repository.findByHashedMessageIn(hashes);
        return studyEntities.stream().collect(Collectors.toMap(this::toMessage, AccessionableEntity::getAccession));
    }

    @Override
    public void save(Map<MESSAGE_TYPE, String> accessioningObjects) {
        Set<ENTITY_TYPE> entitySet = accessioningObjects.entrySet().stream().map(this::toEntity).collect(Collectors.toSet());
        repository.save(entitySet);
    }

}
