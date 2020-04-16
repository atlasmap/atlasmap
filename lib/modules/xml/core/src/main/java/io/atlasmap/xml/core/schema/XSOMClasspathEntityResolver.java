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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class XSOMClasspathEntityResolver implements EntityResolver {
    private ClassLoader classLoader;

    public XSOMClasspathEntityResolver(ClassLoader loader) {
        this.classLoader = loader != null ? loader : XSOMClasspathEntityResolver.class.getClassLoader();
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (publicId != null || systemId == null) {
            return null;
        }
        URI uri;
        try {
            uri = new URI(systemId);
        } catch (Exception e) {
            return null;
        }
        if (uri.getScheme() != null || uri.getSchemeSpecificPart() == null) {
            return null;
        }
        String path = uri.getSchemeSpecificPart();
        if (path.startsWith(".") || path.startsWith(File.pathSeparator)) {
            return null;
        }

        InputStream is = classLoader.getResourceAsStream(path);
        if (is == null) {
            return null;
        }
        return new InputSource(is);
    }
    
}