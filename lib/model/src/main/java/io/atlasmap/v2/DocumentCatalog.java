/*
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Document catalog model which holds a list of {@link DocumentMetadata}
 * for both of Source Documents and Target Documents in imported order.
 */
public class DocumentCatalog implements Serializable {
    private List<DocumentMetadata> sources = new ArrayList<>();
    private List<DocumentMetadata> targets = new ArrayList<>();

    /**
     * Gets a List of source Document metadata.
     * @return map
     */
    public List<DocumentMetadata> getSources() {
        return sources;
    }

    /**
     * Sets a List of source Document metadata.
     * @param sources map
     */
    public void setSources(List<DocumentMetadata> sources) {
        this.sources = sources;
    }

    /**
     * Gets a List of target Document metadata.
     * @return map
     */
    public List<DocumentMetadata> getTargets() {
        return targets;
    }

    /**
     * Sets a List of target Document metadata.
     * @param targets map
     */
    public void setTargets(List<DocumentMetadata> targets) {
        this.targets = targets;
    }
    
}
