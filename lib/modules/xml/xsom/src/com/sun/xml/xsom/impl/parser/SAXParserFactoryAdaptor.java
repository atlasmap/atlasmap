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
package com.sun.xml.xsom.impl.parser;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderAdapter;

import com.sun.xml.xsom.parser.XMLParser;


/**
 * {@link SAXParserFactory} implementation that ultimately
 * uses {@link XMLParser} to parse documents.
 * 
 * @deprecated 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SAXParserFactoryAdaptor extends SAXParserFactory {
    
    private final XMLParser parser;
    
    public SAXParserFactoryAdaptor( XMLParser _parser ) {
        this.parser = _parser;
    }
    
    public SAXParser newSAXParser() throws ParserConfigurationException, SAXException {
        return new SAXParserImpl();
    }

    public void setFeature(String name, boolean value) {
        throw new UnsupportedOperationException("XSOM parser does not support JAXP features.");
    }

    public boolean getFeature(String name) {
        return false;
    }
    
    private class SAXParserImpl extends SAXParser
    {
        private final XMLReaderImpl reader = new XMLReaderImpl();
        
        /**
         * @deprecated
         */
        public org.xml.sax.Parser getParser() throws SAXException {
            return new XMLReaderAdapter(reader);
        }

        public XMLReader getXMLReader() throws SAXException {
            return reader;
        }

        public boolean isNamespaceAware() {
            return true;
        }

        public boolean isValidating() {
            return false;
        }

        public void setProperty(String name, Object value) {
        }

        public Object getProperty(String name) {
            return null;
        }
    }
    
    private class XMLReaderImpl extends XMLFilterImpl
    {
        public void parse(InputSource input) throws IOException, SAXException {
            parser.parse(input,this,this,this);
        }

        public void parse(String systemId) throws IOException, SAXException {
            parser.parse(new InputSource(systemId),this,this,this);
        }
    }
}
