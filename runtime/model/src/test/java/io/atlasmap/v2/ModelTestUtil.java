package io.atlasmap.v2;

import java.io.File;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.util.DomAnnotationParserFactory;

public class ModelTestUtil {

    public static XSSchema getCoreSchema() throws Exception {
        XSOMParser parser = new XSOMParser(SAXParserFactory.newInstance());
        parser.setErrorHandler(new ErrorHandler() {
            @Override
            public void error(SAXParseException arg0) throws SAXException {
                throw arg0;
            }
            @Override
            public void fatalError(SAXParseException arg0) throws SAXException {
                throw arg0;
            }
            @Override
            public void warning(SAXParseException arg0) throws SAXException {
                throw arg0;
            }
        });
        parser.setAnnotationParser(new DomAnnotationParserFactory());
        parser.parse(new File("target/classes/atlas-actions-v2.xsd"));
        parser.parse(new File("target/classes/atlas-model-v2.xsd"));
        return parser.getResult().getSchema("http://atlasmap.io/v2");
    }

}
