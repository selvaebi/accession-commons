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
package uk.ac.ebi.ampt2d.accession;

import java.nio.ByteBuffer;
import java.util.UUID;

public abstract class UuidAccessionGenerator<T> extends IndividualAccessionGenerator<T> {

    private byte[] namespaceUuidBytes;

    protected UuidAccessionGenerator(String namespace) {
        namespaceUuidBytes = getNamespaceUUIDbytes(namespace);
    }

    private byte[] getNamespaceUUIDbytes(String namespace) {
        UUID namespaceUuid = UUID.nameUUIDFromBytes(namespace.getBytes());
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(namespaceUuid.getMostSignificantBits());
        bb.putLong(namespaceUuid.getLeastSignificantBits());
        return bb.array();
    }

    @Override
    protected String generateAccesion(T object) {
        String objectId = getObjectId(object);

        // TODO: this generates a version 3 (name based) UUID: explore other options to get a version 5 UUID
        UUID accession = UUID.nameUUIDFromBytes(concatenateNamespaceAndNameBytes(objectId));

        return accession.toString();
    }

    private byte[] concatenateNamespaceAndNameBytes(String name) {
        byte[] nameBytes = name.getBytes();

        byte[] namespaceAndNameBytes = new byte[namespaceUuidBytes.length + nameBytes.length];
        System.arraycopy(namespaceUuidBytes, 0, namespaceAndNameBytes, 0, namespaceUuidBytes.length);
        System.arraycopy(nameBytes, 0, namespaceAndNameBytes, namespaceUuidBytes.length, nameBytes.length);

        return namespaceAndNameBytes;
    }

    protected abstract String getObjectId(T object);
}
