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
package uk.ac.ebi.ampt2d.accession.utils;

import org.junit.Assert;
import org.junit.Test;

public class Sha1UtilTest {

    @Test
    public void testGenerateAccession() {

        String namespace = "AMP";
        String name1 = "Object1";
        String name2 = "Object2";

        String name1Accession = Sha1Util.generateSha1Accession(namespace.getBytes(), name1.getBytes());
        String name2Accession = Sha1Util.generateSha1Accession(namespace.getBytes(), name2.getBytes());
        String name3Accession = Sha1Util.generateSha1Accession(namespace.getBytes(), name2.getBytes());

        Assert.assertNotEquals(name1Accession, name2Accession);
        Assert.assertEquals(name2Accession, name3Accession);
        Assert.assertEquals(40,name1Accession.length());

    }

}