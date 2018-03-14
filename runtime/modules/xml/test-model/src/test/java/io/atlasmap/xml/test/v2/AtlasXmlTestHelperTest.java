package io.atlasmap.xml.test.v2;

import org.junit.Test;

public class AtlasXmlTestHelperTest {

    @Test
    public void testXmlFPE() {
        XmlFlatPrimitiveElement xfpe = new XmlFlatPrimitiveElement();
        xfpe.setBooleanField(true);
        xfpe.setCharField("a");
        xfpe.setDoubleField(43214321.43214d);
        xfpe.setFloatField(23432.431f);
        xfpe.setIntField(-94);
        xfpe.setLongField(124234324L);
        xfpe.setShortField((short) 234);

        // Files.write(Paths.get(new
        // File("target/xmlflatprimitiveelement.xml").toURI()),
        // AtlasXmlTestHelper.marshal(xfpe).getBytes(), StandardOpenOption.CREATE_NEW);
    }

}
