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
package io.atlasmap.itests.reference.json_to_json;

import io.atlasmap.json.v2.JsonField;
import io.atlasmap.itests.reference.AtlasBaseActionsTest;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;

public class JsonJsonFieldActionsTest extends AtlasBaseActionsTest {

    public JsonJsonFieldActionsTest() {
        this.sourceField = createField("/contact/firstName");
        this.targetField = createField("/contact/firstName");
        this.docURI = "atlas:json";
    }

    protected Field createField(String path) {
        JsonField f = new JsonField();
        f.setPath(path);
        f.setFieldType(FieldType.STRING);
        return f;
    }

    @Override
    public Object createSource(String inputFirstName) {
        return "{ \"contact\": { \"firstName\": \"" + inputFirstName + "\" } }";
    }

    public Object getTargetValue(Object target, Class<?> targetClassExpected) {
        System.out.println("Extracting output value from: " + target);
        String result = (String) target;

        if(targetClassExpected != null && targetClassExpected.equals(Integer.class)) {
            result = result.substring("{\"contact\":{\"firstName\":".length());
            result = result.substring(0, result.length() - "}}".length());
            return Integer.valueOf(result);
        } else if(targetClassExpected != null && targetClassExpected.equals(Boolean.class)) {
            result = result.substring("{\"contact\":{\"firstName\":".length());
            result = result.substring(0, result.length() - "}}".length());
            return Boolean.valueOf(result);
        } else {
            // everything else is a string for JSON
            result = result.substring("{\"contact\":{\"firstName\":\"".length());
            result = result.substring(0, result.length() - "\"}}".length());
        }
        return result;
    }

}
