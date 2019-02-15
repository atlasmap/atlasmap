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
package io.atlasmap.itests.reference;

import java.util.Iterator;
import java.util.List;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldType;

public class MyCustomFieldActions extends Action implements AtlasFieldAction {

    private static final long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "Concat", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public String concat(Object input) {
        @SuppressWarnings("unchecked")
        Iterator<String> list = ((List<String>)input).iterator();
        StringBuilder buf = new StringBuilder();
        buf.append(list.next());
        while (list.hasNext()) {
            buf.append("-").append(list.next());
        }
        return buf.toString();
    }

}
