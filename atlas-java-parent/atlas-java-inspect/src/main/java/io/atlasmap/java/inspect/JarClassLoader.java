/**
 * Copyright (C) 2017 Red Hat, Inc.
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
package io.atlasmap.java.inspect;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(ClassInspectionService.class);
    private URLClassLoader loader;

    public JarClassLoader(String... paths) {
        Set<URL> urls = new HashSet<>();
        for (String path : paths) {
            File f = new File(path);
            if (!f.exists()) {
                continue;
            }
            populateFileUrl(f, urls);
        }
        loader = new URLClassLoader(urls.toArray(new URL[0]));
    }

    private void populateFileUrl(File file, Set<URL> urls) {
        if (file.isDirectory()) {
            for (File subf : file.listFiles()) {
                populateFileUrl(subf, urls);
            }
        } else if (file.getName().toLowerCase().endsWith(".jar")) {
            try {
                urls.add(file.toURI().toURL());
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignoring invalid file name: {}", file.getName());
                }
            }
        }
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loader.loadClass(name);
    }

    public URL getResource(String name) {
        return loader.getResource(name);
    }

    public InputStream getResourceAsStream(String name) {
        return loader.getResourceAsStream(name);
    }

}
