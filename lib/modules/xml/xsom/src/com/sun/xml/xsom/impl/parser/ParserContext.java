/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.xsom.impl.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.impl.ElementDecl;
import com.sun.xml.xsom.impl.SchemaImpl;
import com.sun.xml.xsom.impl.SchemaSetImpl;
import com.sun.xml.xsom.parser.AnnotationParserFactory;
import com.sun.xml.xsom.parser.XMLParser;
import com.sun.xml.xsom.parser.XSOMParser;

/**
 * Provides context information to be used by {@link NGCCRuntimeEx}s.
 * 
 * <p>
 * This class does the actual processing for {@link XSOMParser},
 * but to hide the details from the public API, this class in
 * a different package.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ParserContext {

    /** SchemaSet to which a newly parsed schema is put in. */
    public final SchemaSetImpl schemaSet = new SchemaSetImpl();

    private final XSOMParser owner;

    final XMLParser parser;


    private final Vector<Patch> patchers = new Vector<Patch>();
    private final Vector<Patch> errorCheckers = new Vector<Patch>();

    /**
     * Documents that are parsed already. Used to avoid cyclic inclusion/double
     * inclusion of schemas. Set of {@link SchemaDocumentImpl}s.
     *
     * The actual data structure is map from {@link SchemaDocumentImpl} to itself,
     * so that we can access the {@link SchemaDocumentImpl} itself.
     */
    public final Map<SchemaDocumentImpl, SchemaDocumentImpl> parsedDocuments = new HashMap<SchemaDocumentImpl, SchemaDocumentImpl>();


    public ParserContext( XSOMParser owner, XMLParser parser ) {
        this.owner = owner;
        this.parser = parser;

        try {
            parse(new InputSource(ParserContext.class.getResource("datatypes.xsd").toExternalForm()));

            SchemaImpl xs = (SchemaImpl)
                schemaSet.getSchema("http://www.w3.org/2001/XMLSchema");
            xs.addSimpleType(schemaSet.anySimpleType,true);
            xs.addComplexType(schemaSet.anyType,true);
        } catch( SAXException e ) {
            // this must be a bug of XSOM
            if(e.getException()!=null)
                e.getException().printStackTrace();
            else
                e.printStackTrace();
            throw new InternalError();
        }
    }

    public EntityResolver getEntityResolver() {
        return owner.getEntityResolver();
    }

    public AnnotationParserFactory getAnnotationParserFactory() {
        return owner.getAnnotationParserFactory();
    }

    /**
     * Parses a new XML Schema document.
     */
    public void parse( InputSource source ) throws SAXException {
        newNGCCRuntime().parseEntity(source,false,null,null);
    }


    public XSSchemaSet getResult() throws SAXException {
        // run all the patchers
        for (Patch patcher : patchers)
            patcher.run();
        patchers.clear();

        // build the element substitutability map
        Iterator itr = schemaSet.iterateElementDecls();
        while(itr.hasNext())
            ((ElementDecl)itr.next()).updateSubstitutabilityMap();

        // run all the error checkers
        for (Patch patcher : errorCheckers)
            patcher.run();
        errorCheckers.clear();


        if(hadError)    return null;
        else            return schemaSet;
    }

    public NGCCRuntimeEx newNGCCRuntime() {
        return new NGCCRuntimeEx(this);
    }



    /** Once an error is detected, this flag is set to true. */
    private boolean hadError = false;

    /** Turns on the error flag. */
    void setErrorFlag() { hadError=true; }

    /**
     * PatchManager implementation, which is accessible only from
     * NGCCRuntimEx.
     */
    final PatcherManager patcherManager = new PatcherManager() {
        public void addPatcher( Patch patch ) {
            patchers.add(patch);
        }
        public void addErrorChecker( Patch patch ) {
            errorCheckers.add(patch);
        }
        public void reportError( String msg, Locator src ) throws SAXException {
            // set a flag to true to avoid returning a corrupted object.
            setErrorFlag();

            SAXParseException e = new SAXParseException(msg,src);
            if(errorHandler==null)
                throw e;
            else
                errorHandler.error(e);
        }
    };

    /**
     * ErrorHandler proxy to turn on the hadError flag when an error
     * is found.
     */
    final ErrorHandler errorHandler = new ErrorHandler() {
        private ErrorHandler getErrorHandler() {
            if( owner.getErrorHandler()==null )
                return noopHandler;
            else
                return owner.getErrorHandler();
        }

        public void warning(SAXParseException e) throws SAXException {
            getErrorHandler().warning(e);
        }

        public void error(SAXParseException e) throws SAXException {
            setErrorFlag();
            getErrorHandler().error(e);
        }

        public void fatalError(SAXParseException e) throws SAXException {
            setErrorFlag();
            getErrorHandler().fatalError(e);
        }
    };

    /**
     * {@link ErrorHandler} that does nothing.
     */
    final ErrorHandler noopHandler = new ErrorHandler() {
        public void warning(SAXParseException e) {
        }
        public void error(SAXParseException e) {
        }
        public void fatalError(SAXParseException e) {
            setErrorFlag();
        }
    };
}
