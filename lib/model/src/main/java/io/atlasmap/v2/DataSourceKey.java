/**
 * Copyright (C) 2017 Red Hat, Inc.
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
package io.atlasmap.v2;

public class DataSourceKey {
    private boolean isSource;
    private String documentId;
    private int hashCode;

    public DataSourceKey(boolean isSource, String docId) {
        this.isSource = isSource;
        this.documentId = docId;
        this.hashCode = ((isSource ? "source:" : "target:") + documentId).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof DataSourceKey)) {
            return false;
        }
        DataSourceKey other = (DataSourceKey)o;
        if (isSource != other.isSource || (documentId == null ^ other.documentId == null)) {
            return false;
        }
        return documentId == null || documentId.equals(other.documentId);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

}