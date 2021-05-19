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
package io.atlasmap.itests.reference.json_to_json;

import io.atlasmap.api.AtlasSession;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonTestHelper {

    static void addInputMappings(AtlasSession session, Action... mappings) {
        addMappings(((Mapping) session.getMapping().getMappings().getMapping().get(0)).getInputField().get(0), mappings);
    }

    static void addMappings(Field f, Action... mappings) {
        ArrayList<Action> l = new ArrayList<>();
        for (Action m : mappings) {
            l.add(m);
        }
        if (f.getActions() == null) {
            f.setActions(l);
        } else {
            f.getActions().addAll(l);
        }
    }

    static JsonField createJsonStringField(String path) {
        JsonField field = new JsonField();
        field.setPath(path);
        Pattern p = Pattern.compile(".*/(\\w+)<?>?");
        Matcher m = p.matcher(path);
        if (m.matches()) {
            field.setName(m.group(1));
        }
        field.setFieldType(FieldType.STRING);
        return field;
    }

    static JsonField addInputStringField(AtlasSession session, String path) {
        if (session.getMapping().getMappings().getMapping().isEmpty()) {
            session.getMapping().getMappings().getMapping().add(new Mapping());
        }

        JsonField f = createJsonStringField(path);
        ((Mapping) session.getMapping().getMappings().getMapping().get(0)).getInputField().add(f);
        return f;
    }

    static JsonField addOutputStringField(AtlasSession session, String path) {
        if (session.getMapping().getMappings().getMapping().isEmpty()) {
            session.getMapping().getMappings().getMapping().add(new Mapping());
        }

        JsonField f = createJsonStringField(path);
        ((Mapping) session.getMapping().getMappings().getMapping().get(0)).getOutputField().add(f);
        return f;
    }
}
