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

import com.sun.xml.xsom.impl.SchemaImpl;
import com.sun.xml.xsom.parser.SchemaDocument;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link SchemaDocument} implementation.
 *
 * @author Kohsuke Kawaguchi
 */
public final class SchemaDocumentImpl implements SchemaDocument
{
    private final SchemaImpl schema;

    /**
     * URI of the schema document to be parsed. Can be null.
     */
    private final String schemaDocumentURI;

    /**
     * {@link SchemaDocumentImpl}s that are referenced from this document.
     */
    final Set<SchemaDocumentImpl> references = new HashSet<SchemaDocumentImpl>();

    /**
     * {@link SchemaDocumentImpl}s that are referencing this document.
     */
    final Set<SchemaDocumentImpl> referers = new HashSet<SchemaDocumentImpl>();

    protected SchemaDocumentImpl(SchemaImpl schema, String _schemaDocumentURI) {
        this.schema = schema;
        this.schemaDocumentURI = _schemaDocumentURI;
    }

    public String getSystemId() {
        return schemaDocumentURI;
    }

    public String getTargetNamespace() {
        return schema.getTargetNamespace();
    }

    public SchemaImpl getSchema() {
        return schema;
    }

    public Set<SchemaDocument> getReferencedDocuments() {
        return Collections.<SchemaDocument>unmodifiableSet(references);
    }

    public Set<SchemaDocument> getIncludedDocuments() {
        return getImportedDocuments(this.getTargetNamespace());
    }

    public Set<SchemaDocument> getImportedDocuments(String targetNamespace) {
        if(targetNamespace==null)
            throw new IllegalArgumentException();
        Set<SchemaDocument> r = new HashSet<SchemaDocument>();
        for (SchemaDocumentImpl doc : references) {
            if(doc.getTargetNamespace().equals(targetNamespace))
                r.add(doc);
        }
        return Collections.unmodifiableSet(r);
    }

    public boolean includes(SchemaDocument doc) {
        if(!references.contains(doc))
            return false;
        return doc.getSchema()==schema;
    }

    public boolean imports(SchemaDocument doc) {
        if(!references.contains(doc))
            return false;
        return doc.getSchema()!=schema;
    }

    public Set<SchemaDocument> getReferers() {
        return Collections.<SchemaDocument>unmodifiableSet(referers);
    }

    @Override
    public boolean equals(Object o) {
        SchemaDocumentImpl rhs = (SchemaDocumentImpl) o;

        if( this.schemaDocumentURI==null || rhs.schemaDocumentURI==null)
            return this==rhs;
        if(!schemaDocumentURI.equals(rhs.schemaDocumentURI) )
            return false;
        return this.schema==rhs.schema;
    }
    
    @Override
    public int hashCode() {
        if (schemaDocumentURI==null)
            return super.hashCode();
        if (schema == null) {
            return schemaDocumentURI.hashCode();
        }
        return schemaDocumentURI.hashCode()^this.schema.hashCode();
    }
}
