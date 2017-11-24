package uk.ac.ebi.ampt2d.accession.file;

import org.springframework.context.annotation.ComponentScan;
import uk.ac.ebi.ampt2d.accession.AccessioningProperties;

@ComponentScan(basePackages = "uk.ac.ebi.ampt2d.accession.file", basePackageClasses = AccessioningProperties.class)
public class FileConfiguration {
}
