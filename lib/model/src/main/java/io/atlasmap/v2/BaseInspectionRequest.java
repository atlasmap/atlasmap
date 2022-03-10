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
import java.util.List;

/**
 * The base class for Document inspection request that AtlasMap UI sends
 * to the backend.
 */
public abstract class BaseInspectionRequest implements Serializable {
    /** Paths to inspect. */
    protected List<String> inspectPaths;

    /** Phrase to limit the inspection **/
    protected String searchPhrase;

    /**
     * Gets the paths to inspect.
     * @return paths
     */
    public List<String> getInspectPaths() {
        return inspectPaths;
    }

    /**
     * Sets the paths to inspect.
     * @param inspectPaths paths
     */
    public void setInspectPaths(List<String> inspectPaths) {
        this.inspectPaths = inspectPaths;
    }

    /**
     * Gets the search phrase to limit the inspection.
     * @return searchPhrase
     */
    public String getSearchPhrase() {
        return searchPhrase;
    }

    /**
     * Gets the search phrase to limit the inspection.
     * @param searchPhrase phrase
     */
    public void setSearchPhrase(String searchPhrase) {
        this.searchPhrase = searchPhrase;
    }
}
