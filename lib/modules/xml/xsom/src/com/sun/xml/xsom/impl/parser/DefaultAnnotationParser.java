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

import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.xml.xsom.parser.AnnotationContext;
import com.sun.xml.xsom.parser.AnnotationParser;

/**
 * AnnotationParser that just ignores annotation.
 * 
 * <p>
 * This class doesn't have any state. So it should be used as a singleton.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class DefaultAnnotationParser extends AnnotationParser {
    
    private DefaultAnnotationParser() {}
    
    public static final AnnotationParser theInstance = new DefaultAnnotationParser();
    
    public ContentHandler getContentHandler(
        AnnotationContext contest, String elementName,
        ErrorHandler errorHandler, EntityResolver entityResolver ) {
        return new DefaultHandler();
    }
    
    public Object getResult( Object existing ) {
        return null;
    }
    
    
}

