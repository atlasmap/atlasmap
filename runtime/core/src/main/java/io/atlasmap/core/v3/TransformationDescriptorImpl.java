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

import io.atlasmap.api.v3.TransformationDescriptor;
import io.atlasmap.spi.v3.BaseTransformation;

/**
 *
 */
class TransformationDescriptorImpl implements TransformationDescriptor, Comparable<TransformationDescriptorImpl> {

    final Class<? extends BaseTransformation> transformationClass;
    private final String name;
    private final String description;

    public TransformationDescriptorImpl(String name, String description, Class<? extends BaseTransformation> transformationClass) {
        this.name = name;
        this.description = description;
        this.transformationClass = transformationClass;
    }

    /**
     * @see io.atlasmap.api.v3.TransformationDescriptor#name()
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * @see io.atlasmap.api.v3.TransformationDescriptor#description()
     */
    @Override
    public String description() {
        return description;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((transformationClass == null) ? 0 : transformationClass.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TransformationDescriptorImpl other = (TransformationDescriptorImpl) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return transformationClass == other.transformationClass;
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(TransformationDescriptorImpl other) {
        int comparison = name.compareTo(other.name);
        if (comparison != 0) {
            return comparison;
        }
        return transformationClass.getName().compareTo(other.transformationClass.getName());
    }
}
