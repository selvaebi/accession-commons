package uk.ac.ebi.ampt2d.accession;

public interface AccessionRepository<T> {
    String get(T object);

    void add(T object, String accession);
}
