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

package com.sun.xml.xsom.parser;

import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;

/**
 * Used to parse &lt;xs:annotation>.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class AnnotationParser {
    /**
     * Called every time a new &lt;xs:annotation> element
     * is found.
     * 
     * The sub-tree rooted at &lt;xs:annotation> will be
     * sent to this ContentHandler as if it is a whole document.
     * 
     * @param context
     *      indicates the schema component that owns this annotation.
     *      Always non-null.
     * @param parentElementName
     *      local name of the element that contains &lt;xs:annotation>.
     *      (e.g., "element", "attribute", ... )
     * @param errorHandler
     *      The error handler that the client application specifies.
     *      The returned content handler can send its errors to this
     *      object.
     * @param entityResolver
     *      The entity resolver that is currently in use. Again,
     *      The returned content handler can use this object
     *      if it needs to resolve entities.
     */
    public abstract ContentHandler getContentHandler(
        AnnotationContext context,
        String parentElementName,
        ErrorHandler errorHandler,
        EntityResolver entityResolver );
    
    /**
     * Once the SAX events are fed to the ContentHandler,
     * this method will be called to retrieve the parsed result.
     * 
     * @param existing
     *      An annotation object which was returned from another
     *      AnnotationParser before. Sometimes, one schema component
     *      can have multiple &lt:xs:annotation> elements and
     *      this parameter is used to merge all those annotations
     *      together. If there is no existing object, null will be
     *      passed.
     * @return
     *      Any object, including null.
     */
    public abstract Object getResult( Object existing );
}

