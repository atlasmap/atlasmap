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
package io.atlasmap.examples.api;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;

public class Main {

    public static void main(String args[]) throws Exception {
        Main m = new Main();
        m.process();
    }

    public void process() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("atlasmap-mapping.adm");
        AtlasContextFactory factory = DefaultAtlasContextFactory.getInstance();
        AtlasContext context = factory.createContext(url.toURI());
        AtlasSession session = context.createSession();

        url = Thread.currentThread().getContextClassLoader().getResource("order.json");
        String source = new String(Files.readAllBytes(Paths.get(url.toURI())));
        System.out.println("Source document:\n" + source);

        session.setSourceDocument("JSONSchemaSource", source);
        context.process(session);
        String targetDoc = (String) session.getTargetDocument("XMLInstanceSource");

        printXML(targetDoc);
    }

    private void printXML(String targetDoc) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StringWriter writer = new StringWriter();
        transformer.transform(new StreamSource(new StringReader((String)targetDoc)), new StreamResult(writer));
        System.out.println("Target Document:\n" + writer.toString());
    }
}
