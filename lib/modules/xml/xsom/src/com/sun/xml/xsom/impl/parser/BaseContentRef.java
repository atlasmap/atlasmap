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

import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.impl.Ref;

public final class BaseContentRef implements Ref.ContentType, Patch {
    private final Ref.Type baseType;
    private final Locator loc;

    public BaseContentRef(final NGCCRuntimeEx $runtime, Ref.Type _baseType) {
        this.baseType = _baseType;
        $runtime.addPatcher(this);
        $runtime.addErrorChecker(new Patch() {
            public void run() throws SAXException {
                XSType t = baseType.getType();
                if (t.isComplexType() && t.asComplexType().getContentType().asParticle()!=null) {
                    $runtime.reportError(
                        Messages.format(Messages.ERR_SIMPLE_CONTENT_EXPECTED,
                            t.getTargetNamespace(), t.getName()), loc);
                }
            }
        });
        this.loc = $runtime.copyLocator();
    }

    public XSContentType getContentType() {
        XSType t = baseType.getType();
        if(t.asComplexType()!=null)
            return t.asComplexType().getContentType();
        else
            return t.asSimpleType();
    }

    public void run() throws SAXException {
        if (baseType instanceof Patch)
            ((Patch) baseType).run();
    }
}
