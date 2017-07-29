package io.atlasmap.reference.xmlToXml;

import io.atlasmap.reference.AtlasBaseActionsTest;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.xml.v2.XmlField;

public class XmlXmlFieldActionsTest extends AtlasBaseActionsTest {
    
    public XmlXmlFieldActionsTest() {
        this.inputField = createField("/contact/firstName");
        this.outputField = createField("/contact/firstName");
        this.docURI = "atlas:xml?complexType=xmlInputContact";
    }
    
    protected Field createField(String path) {
        XmlField f = new XmlField();
        f.setPath(path);
        f.setFieldType(FieldType.STRING);
        return f;
    }    
    
    @Override
    public Object createInput(String inputFirstName) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><contact><firstName>" + inputFirstName + "</firstName></contact>";
    }
    
    public Object getOutputValue(Object output) {
        System.out.println("Extracting output value from: " + output);
        String result = (String)output;
        result = result.substring("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><contact><firstName>".length());
        result = result.substring(0, result.length() - "</firstName></contact>".length());
        System.out.println("Output value extracted: " + result);
        return result;
    }          
}
