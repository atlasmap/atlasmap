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
package com.sun.xml.xsom.util;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.parser.AnnotationContext;
import com.sun.xml.xsom.parser.AnnotationParser;
import com.sun.xml.xsom.parser.AnnotationParserFactory;

/**
 * {@link AnnotationParserFactory} that parses annotations into a W3C DOM.
 *
 * <p>
 * If you use this parser factory, you'll get {@link Element} that represents
 * &lt;xs:annotation&gt; from {@link XSAnnotation#getAnnotation()}.
 *
 * <p>
 * When multiple &lt;xs:annotation&gt;s are found for the given schema component,
 * you'll see all &lt;xs:appinfo&gt;s and &lt;xs:documentation&gt;s combined under
 * one &lt;xs:annotation&gt; element.
 *
 * @author Kohsuke Kawaguchi
 */
public class DomAnnotationParserFactory implements AnnotationParserFactory {
    
    public AnnotationParser create() {
        return new AnnotationParserImpl();
    }

    public AnnotationParser create(boolean disableSecureProcessing) {
        return new AnnotationParserImpl(disableSecureProcessing);
    }
    
    private static final ContextClassloaderLocal<SAXTransformerFactory> stf = new ContextClassloaderLocal<SAXTransformerFactory>() {
        @Override
        protected SAXTransformerFactory initialValue() throws Exception {
            return (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        }
    };

    private static class AnnotationParserImpl extends AnnotationParser {

        /**
         * Identity transformer used to parse SAX into DOM.
         */
        private final TransformerHandler transformer;
        private DOMResult result;

        AnnotationParserImpl() {
            this(false);
        }

        AnnotationParserImpl(boolean disableSecureProcessing) {
            try {
                SAXTransformerFactory factory = stf.get();
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, disableSecureProcessing);
                transformer = factory.newTransformerHandler();
            } catch (TransformerConfigurationException e) {
                throw new Error(e); // impossible
            }
        }
        
        public ContentHandler getContentHandler(AnnotationContext context, String parentElementName, ErrorHandler errorHandler, EntityResolver entityResolver) {
            result = new DOMResult();
            transformer.setResult(result);
            return transformer;
        }

        public Object getResult(Object existing) {
            Document dom = (Document)result.getNode();
            Element e = dom.getDocumentElement();
            if(existing instanceof Element) {
                // merge all the children
                Element prev = (Element) existing;
                Node anchor = e.getFirstChild();
                while(prev.getFirstChild()!=null) {
                    Node move = prev.getFirstChild();
                    e.insertBefore(e.getOwnerDocument().adoptNode(move), anchor );
                }
            }
            return e;
        }
    }
}
