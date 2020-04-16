/**
 * Copyright (C) 2020 Red Hat, Inc.
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
package io.atlasmap.xml.core.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class XSOMErrorHandler implements ErrorHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AtlasXmlSchemaSetParser.class);

    @Override
    public void error(SAXParseException arg0) throws SAXException {
        throw arg0;
    }

    @Override
    public void fatalError(SAXParseException arg0) throws SAXException {
        throw arg0;
    }

    @Override
    public void warning(SAXParseException arg0) throws SAXException {
        LOG.warn(arg0.getMessage(), arg0);
    }

}