package uk.ac.ebi.ampt2d.accession;

public interface AccessionGenerator<T> {
    String get(T object);
}
