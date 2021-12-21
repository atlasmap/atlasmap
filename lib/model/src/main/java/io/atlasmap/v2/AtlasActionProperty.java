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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation to add a metadata on a field action parameter.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AtlasActionProperty {
    /**
     * The user friendly title of the field action parameter.
     * @return title
     */
    String title();

    /**
     * The type of the field action parameter.
     * @return {@link FieldType}
     */
    FieldType type();

    /**
     * The collection type of the field action parameter.
     * @return {@link CollectionType}
     */
    CollectionType collectionType() default CollectionType.NONE;
}
