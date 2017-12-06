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

import java.util.UUID;

public class UUIDUtilTest {

    private static final int VERSION_FIVE = 5;

    @Test
    public void testGetNamespaceUUIDFromBytes() {
        String namespace1 = "AMP";
        String namespace2 = "PMA";

        UUID namespace1UUID = UUIDUtil.getNamespaceUUIDFromBytes(namespace1.getBytes());
        UUID namespace2UUID = UUIDUtil.getNamespaceUUIDFromBytes(namespace2.getBytes());
        UUID namespace3UUID = UUIDUtil.getNamespaceUUIDFromBytes(namespace2.getBytes());

        Assert.assertNotEquals(namespace1UUID, namespace2UUID);
        Assert.assertEquals(namespace2UUID, namespace3UUID);
        Assert.assertEquals(VERSION_FIVE, namespace1UUID.version());
    }

    @Test
    public void testGenerateVersion5Uuid() {

        String namespace = "AMP";
        String name1 = "Object1";
        String name2 = "Object2";

        UUID namespaceUUID = UUIDUtil.getNamespaceUUIDFromBytes(namespace.getBytes());

        UUID name1UUIDObject = UUIDUtil.generateVersion5Uuid(namespaceUUID, name1.getBytes());
        UUID name2UUIDObject = UUIDUtil.generateVersion5Uuid(namespaceUUID, name2.getBytes());
        UUID name3UUIDObject = UUIDUtil.generateVersion5Uuid(namespaceUUID, name2.getBytes());

        Assert.assertNotEquals(name1UUIDObject, name2UUIDObject);
        Assert.assertEquals(name2UUIDObject, name3UUIDObject);
        Assert.assertEquals(VERSION_FIVE, name1UUIDObject.version());
        Assert.assertEquals(VERSION_FIVE, name2UUIDObject.version());

    }

    @Test
    public void testUuidGenerationWithPython() {

        String namespace = "AMP";
        String name = "Object1";

        //These values are generated in python for the above namespace and name
        String namespaceUuidFromPython = "458fcef2-d938-53c3-9db4-46b96bfd1fee";
        String nameUuidFromPython = "ff7de0c4-d984-5198-8b70-48a8face0d50";

        UUID javaNamespaceUuid = UUIDUtil.getNamespaceUUIDFromBytes(namespace.getBytes());

        Assert.assertEquals(namespaceUuidFromPython, javaNamespaceUuid.toString());

        UUID javaUuid = UUIDUtil.generateVersion5Uuid(javaNamespaceUuid, name.getBytes());

        Assert.assertEquals(nameUuidFromPython, javaUuid.toString());

    }

}