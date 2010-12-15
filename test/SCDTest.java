/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSComponent;

import java.util.Collection;

/**
 * @author Kohsuke Kawaguchi
 */
public class SCDTest extends AbstractXSOMTest {
    /**
     * Taken from spec examples in section 4.2.16
     */
    public void testSpec() throws Exception {
        XSSchemaSet s = load("scdtest.xsd");

        MapNamespaceContext nsc = new MapNamespaceContext("", "", "my", "tns");

        assertOne("foo-bar element declaration",s.select("/my:foo-bar",nsc));
        assertOne("articleType complex type",s.select("type::my:articleType",nsc));
        assertOne("articleType complex type",s.select("/type::my:articleType",nsc));
        assertOne("section element declaration", s.select("/type::my:articleType/model::sequence/element::my:section",nsc));
        assertOne("appendix element declaration",s.select("/type::my:articleType/model::sequence/element::my:appendix",nsc));
        assertOne("anonymous complex type",s.select("/element::my:chapter/type::0",nsc));
        assertOne("wildcard",s.select("/element::my:chapter/type::0/model::sequence/any::*",nsc));
        assertOne("name attribute declaration",s.select("/element::my:chapter/type::0/attribute::name",nsc));
    }

    private void assertOne(String name, Collection<XSComponent> r) {
        assertEquals(1,r.size());
        assertEquals(name,r.iterator().next().toString());
    }
}
