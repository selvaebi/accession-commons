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

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.ampt2d.accession.sha1.Sha1Util;

public class Sha1UtilTest {

    @Test
    public void testGenerateAccession() {
        String object1 = "Object1";
        String object2 = "Object2";

        String object1Accession = Sha1Util.generateSha1Accession(object1.getBytes());
        String object2Accession = Sha1Util.generateSha1Accession(object2.getBytes());
        String object3Accession = Sha1Util.generateSha1Accession(object2.getBytes());

        Assert.assertNotEquals(object1Accession, object2Accession);
        Assert.assertEquals(object2Accession, object3Accession);
        Assert.assertEquals(40, object1Accession.length());

    }

}