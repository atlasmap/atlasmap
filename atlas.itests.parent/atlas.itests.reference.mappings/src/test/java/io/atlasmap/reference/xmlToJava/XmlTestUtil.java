package io.atlasmap.reference.xmlToJava;

import java.nio.file.Files;
import java.nio.file.Paths;

public class XmlTestUtil {
    
    public static String loadXmlStringFromFile(String filename) throws Exception {
        return new String(Files.readAllBytes(Paths.get(filename)));
    }
}
