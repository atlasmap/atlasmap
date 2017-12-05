package io.atlasmap.xml.core;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class XmlPathTest {

    @Test
    public void testOneClass() {
        XmlPath foo = new XmlPath("");
        foo.appendField("user");
        assertEquals("user", foo.toString());
    }

    @Test
    public void testReplaceNamespaces() {

        Map<String, String> namespacesToReplace = new HashMap<String, String>();
        namespacesToReplace.put("x", "z");
        namespacesToReplace.put("y", "z");
        XmlPath path = new XmlPath("/orders/x:order/id/@y:custId", namespacesToReplace);
        assertEquals("orders", path.getSegments().get(0));
        assertEquals("z:order", path.getSegments().get(1));
        assertEquals("id", path.getSegments().get(2));
        assertEquals("@z:custId", path.getSegments().get(3));

        namespacesToReplace = new HashMap<String, String>();
        namespacesToReplace.put("a", "z");
        namespacesToReplace.put("b", "z");
        path = new XmlPath("/orders/x:order/id/@y:custId", namespacesToReplace);
        assertEquals("orders", path.getSegments().get(0));
        assertEquals("x:order", path.getSegments().get(1));
        assertEquals("id", path.getSegments().get(2));
        assertEquals("@y:custId", path.getSegments().get(3));

    }

}
