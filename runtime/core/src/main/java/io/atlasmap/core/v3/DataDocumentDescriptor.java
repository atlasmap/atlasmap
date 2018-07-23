/**
 * Copyright (C) 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.core.v3;

import io.atlasmap.api.v3.DocumentRole;
import io.atlasmap.spi.v3.DataHandler;

/**
 *
 */
class DataDocumentDescriptor {

    private final String id;
    private final DocumentRole role;
    private final String dataFormat;
    final DataHandler handler;
    Object dataDocument;
    final SerializedImage serializedImage = new SerializedImage();

    DataDocumentDescriptor(String id, DocumentRole role, String dataFormat, DataHandler handler, Object dataDocument) {
        this.id = id;
        this.role = role;
        this.dataFormat = dataFormat;
        this.handler = handler;
        this.dataDocument = dataDocument;
        serializedImage.id = id;
        serializedImage.role = role;
        serializedImage.dataFormat = dataFormat;
    }

    String id() {
        return id;
    }

    DocumentRole role() {
        return role;
    }

    String dataFormat() {
        return dataFormat;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        DataDocumentDescriptor other = (DataDocumentDescriptor) object;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return role == other.role;
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        return result;
    }

    static class SerializedImage {
        String id;
        DocumentRole role;
        String dataFormat;

        /**
         * @see Object#equals(Object)
         */
        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null) {
                return false;
            }
            if (getClass() != object.getClass()) {
                return false;
            }
            SerializedImage other = (SerializedImage) object;
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            return role == other.role;
        }

        /**
         * @see Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + ((role == null) ? 0 : role.hashCode());
            return result;
        }
    }
}
