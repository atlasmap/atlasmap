package io.atlasmap.xml.test.v2;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AtlasXmlTestHelperTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testXmlFPE() throws Exception {
        XmlFlatPrimitiveElement xfpe = new XmlFlatPrimitiveElement();
        xfpe.setBooleanField(true);
        xfpe.setCharField("a");
        xfpe.setDoubleField(43214321.43214d);
        xfpe.setFloatField(23432.431f);
        xfpe.setIntField(-94);
        xfpe.setLongField(124234324l);
        xfpe.setShortField((short)234);
        
        Files.write(Paths.get(new File("target/xmlflatprimitiveelement.xml").toURI()), AtlasXmlTestHelper.marshal(xfpe).getBytes(), StandardOpenOption.CREATE_NEW);
    }

}
