package uk.ac.ebi.ampt2d.accession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccessioningService<T> {
    private AccessionRepository<T> accessionRepository;

    private AccessionGenerator<T> accessionGenerator;

    public AccessioningService(AccessionRepository<T> accessionRepository,
                               AccessionGenerator<T> accessionGenerator) {
        this.accessionRepository = accessionRepository;
        this.accessionGenerator = accessionGenerator;
    }

    public Map<T, String> createAccessions(List<T> objects) {
        Map<T, String> accessions = new HashMap<T, String>();
        for (T object : objects) {
            String accession = accessionRepository.get(object);
            if (accession == null) {
                accession = accessionGenerator.get(object);
                accessionRepository.add(object, accession);
            }
            accessions.put(object, accession);
        }
        return accessions;
    }
}
