/*
 * Copyright (C) 2017 Oracle
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
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.impl.util.SchemaWriter;
import com.sun.xml.xsom.parser.SchemaDocument;
import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.util.DomAnnotationParserFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.OutputStreamWriter;
import java.util.Set;

/**
 * Parses all the schemas specified as the command line arguments,
 * then dumps it (to see if the parsing was done correctly.)
 */
public class Dumper {
    public static void main(String[] args) throws Exception {
        XSOMParser reader = new XSOMParser();
        // set an error handler so that you can receive error messages
        reader.setErrorHandler(new ErrorReporter(System.out));
        // DomAnnotationParserFactory is a convenient default to use
        reader.setAnnotationParser(new DomAnnotationParserFactory());

        try {
            // the parse method can by called many times
            for( int i=0; i<args.length; i++ )
                reader.parse(new File(args[i]));

            XSSchemaSet xss = reader.getResult();
            if(xss==null)
                System.out.println("error");
            else
                new SchemaWriter(new OutputStreamWriter(System.out)).visit(xss);

            dump(reader.getDocuments());
        } catch( SAXException e ) {
            if(e.getException()!=null)
                e.getException().printStackTrace();
            else
                e.printStackTrace();
            throw e;
        }
    }

    private static void dump(Set<SchemaDocument> documents) {
        for (SchemaDocument doc : documents) {
            System.out.println("Schema document: "+doc.getSystemId());
            System.out.println("  target namespace: "+doc.getTargetNamespace());
            for (SchemaDocument ref : doc.getReferencedDocuments()) {
                System.out.print("    -> "+ref.getSystemId());
                if(doc.includes(ref))
                    System.out.print(" (include)");
                System.out.println();
            }
        }

    }
}
