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

/**
 * Manages patchers.
 * 
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public interface PatcherManager {
    void addPatcher( Patch p );
    void addErrorChecker( Patch p );
    /**
     * Reports an error during the parsing.
     * 
     * @param source
     *      location of the error in the source file, or null if
     *      it's unavailable.
     */
    void reportError( String message, Locator source ) throws SAXException;
    
    
    public interface Patcher {
        void run() throws SAXException;
    }
}
