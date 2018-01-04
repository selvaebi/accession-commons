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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A Sha1Util class to generate SHA1 identifier
 */
public class Sha1Util {

    private static final Logger sha1UtilLogger = LoggerFactory.getLogger(Sha1Util.class);

    public static String generateSha1Accession(byte[] nameBytes) {

        return DatatypeConverter.printHexBinary(toSHA1(nameBytes));
    }

    private static byte[] toSHA1(byte[] bytes) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            sha1UtilLogger.error("No Such Algorithm - SHA-1");
        }
        return md.digest(bytes);
    }

}
