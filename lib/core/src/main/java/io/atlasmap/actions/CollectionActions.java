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
package io.atlasmap.actions;

import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.spi.AtlasFieldAction;
import io.atlasmap.v2.CopyTo;
import io.atlasmap.v2.FieldType;

public class CollectionActions implements AtlasFieldAction {

    @AtlasActionProcessor(sourceType = FieldType.ANY)
    public static Object[] copyTo(CopyTo action, Object input) {
        // This a noop processor. Nevertheless it's signature is important to signal that's a one-to-many action.
        // It's behavior is implemented directly into DefaultAtlasContext
        return new Object[]{};
    }

}
