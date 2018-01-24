package uk.ac.ebi.ampt2d.accession;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@NoRepositoryBean
public interface AccessioningRepository<ENTITY_TYPE extends AccessionableEntity, ID_TYPE extends Serializable> extends Repository<ENTITY_TYPE, ID_TYPE> {

    Collection<ENTITY_TYPE> findByHashedMessageIn(List<ID_TYPE> hashes);

    <S extends ENTITY_TYPE> Iterable<S> save(Iterable<S> var1);

    long count();

    void flush();

}
