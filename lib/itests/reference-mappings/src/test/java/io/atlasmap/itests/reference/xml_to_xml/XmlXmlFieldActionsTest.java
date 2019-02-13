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
package io.atlasmap.itests.reference.xml_to_xml;

import io.atlasmap.itests.reference.AtlasBaseActionsTest;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.xml.v2.XmlField;

public class XmlXmlFieldActionsTest extends AtlasBaseActionsTest {

    public XmlXmlFieldActionsTest() {
        this.sourceField = createField("/contact/firstName");
        this.targetField = createField("/contact/firstName");
        this.docURI = "atlas:xml?complexType=xmlInputContact";
    }

    protected Field createField(String path) {
        XmlField f = new XmlField();
        f.setPath(path);
        f.setFieldType(FieldType.STRING);
        return f;
    }

    @Override
    public Object createSource(String inputFirstName) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><contact><firstName>" + inputFirstName
                + "</firstName></contact>";
    }

    public Object getTargetValue(Object target, Class<?> ouputClassExpected) {
        System.out.println("Extracting output value from: " + target);
        String result = (String) target;
        result = result.substring("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><contact><firstName>".length());
        result = result.substring(0, result.length() - "</firstName></contact>".length());
        System.out.println("Output value extracted: " + result);

        if(ouputClassExpected != null && ouputClassExpected.equals(Integer.class)) {
            return Integer.valueOf(result);
        }
        return result;
    }
}
