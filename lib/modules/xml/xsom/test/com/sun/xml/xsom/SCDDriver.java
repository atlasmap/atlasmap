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
package com.sun.xml.xsom;
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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

import org.xml.sax.Locator;

import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.util.ComponentNameFunction;

/**
 * Tests SCD.
 * @author Kohsuke Kawaguchi
 */
public class SCDDriver {
    public static void main(String[] args) throws Exception {
        XSOMParser p = new XSOMParser();

        for( int i=1; i<args.length; i++ )
            p.parse(args[i]);

        XSSchemaSet r = p.getResult();
        SCD scd = SCD.create(args[0], new DummyNSContext());
        Collection<XSComponent> result = scd.select(r);
        for( XSComponent c : result) {
            System.out.println(c.apply(new ComponentNameFunction()));
            print(c.getLocator());
            System.out.println();
        }
        System.out.printf("%1d match(s)\n",result.size());
    }

    private static void print(Locator locator) {
        System.out.printf("line %1d of %2s\n", locator.getLineNumber(), locator.getSystemId());

    }

    private static class DummyNSContext implements NamespaceContext {
        public String getNamespaceURI(String prefix) {
            return prefix;
        }

        public String getPrefix(String namespaceURI) {
            return namespaceURI;
        }

        public Iterator getPrefixes(String namespaceURI) {
            return Collections.singletonList(namespaceURI).iterator();
        }
    }
}
