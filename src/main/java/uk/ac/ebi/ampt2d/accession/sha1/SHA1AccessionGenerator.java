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
package uk.ac.ebi.ampt2d.accession.sha1;

import uk.ac.ebi.ampt2d.accession.AccessioningObject;
import uk.ac.ebi.ampt2d.accession.SingleAccessionGenerator;
import uk.ac.ebi.ampt2d.accession.utils.Sha1Util;

public class SHA1AccessionGenerator<T extends AccessioningObject> extends SingleAccessionGenerator<T, String> {

    @Override
    protected String generateAccession(T object) {
        String accession = Sha1Util.generateSha1Accession(object.getHash().getBytes());
        return accession;
    }

}
