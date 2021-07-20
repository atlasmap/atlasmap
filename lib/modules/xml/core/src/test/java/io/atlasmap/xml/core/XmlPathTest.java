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
package io.atlasmap.xml.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class XmlPathTest {

    @Test
    public void testOneClass() {
        XmlPath foo = new XmlPath("");
        foo.appendField("user");
        assertEquals("/user", foo.toString());
    }

    @Test
    public void testReplaceNamespaces() {

        Map<String, String> namespacesToReplace = new HashMap<String, String>();
        namespacesToReplace.put("x", "z");
        namespacesToReplace.put("y", "z");
        XmlPath path = new XmlPath("/orders/x:order/id/@y:custId", namespacesToReplace);
        assertEquals("orders", path.getSegments(false).get(0).getExpression());
        assertEquals("z:order", path.getSegments(false).get(1).getExpression());
        assertEquals("id", path.getSegments(false).get(2).getExpression());
        assertEquals("@z:custId", path.getSegments(false).get(3).getExpression());

        namespacesToReplace = new HashMap<String, String>();
        namespacesToReplace.put("a", "z");
        namespacesToReplace.put("b", "z");
        path = new XmlPath("/orders/x:order/id/@y:custId", namespacesToReplace);
        assertEquals("orders", path.getSegments(false).get(0).getExpression());
        assertEquals("x:order", path.getSegments(false).get(1).getExpression());
        assertEquals("id", path.getSegments(false).get(2).getExpression());
        assertEquals("@y:custId", path.getSegments(false).get(3).getExpression());

    }

}
