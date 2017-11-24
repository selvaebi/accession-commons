package uk.ac.ebi.ampt2d.accession.server;

import uk.ac.ebi.ampt2d.accession.file.UuidFile;

import java.util.UUID;

public class FileAccession {

    private UuidFile file;

    private UUID accession;

    FileAccession() {
    }

    FileAccession(UuidFile file, UUID accession) {
        this.file = file;
        this.accession = accession;
    }

    public UuidFile getFile() {
        return file;
    }

    public UUID getAccession() {
        return accession;
    }
}

