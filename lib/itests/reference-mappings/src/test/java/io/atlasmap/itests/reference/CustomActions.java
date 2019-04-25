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

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.atlasmap.spi.AtlasFieldAction;
import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.AtlasActionProperty;
import io.atlasmap.v2.FieldType;

public class CustomActions implements AtlasFieldAction {

    public static class Concat extends Action {

        @JsonPropertyDescription("The separator to use between concatenated items.")
        @AtlasActionProperty(title = "Separator", type = FieldType.STRING)
        public String separator = "-";

    }

    @AtlasActionProcessor
    public static String concat(Concat action, Collection<String> input) {
        @SuppressWarnings("unchecked")
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (String i : input) {
            if( !first ) {
                buf.append(action.separator);
            }
            first=false;
            buf.append(i);
        }
        return buf.toString();
    }

}
