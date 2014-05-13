/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.xsom.util;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.parser.AnnotationContext;
import com.sun.xml.xsom.parser.AnnotationParser;
import com.sun.xml.xsom.parser.AnnotationParserFactory;
import javax.xml.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

/**
 * {@link AnnotationParserFactory} that parses annotations into a W3C DOM.
 *
 * <p>
 * If you use this parser factory, you'll get {@link Element} that represents
 * &lt;xs:annotation> from {@link XSAnnotation#getAnnotation()}.
 *
 * <p>
 * When multiple &lt;xs:annotation>s are found for the given schema component,
 * you'll see all &lt;xs:appinfo>s and &lt;xs:documentation>s combined under
 * one &lt;xs:annotation> element.
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
