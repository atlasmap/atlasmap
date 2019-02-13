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
package io.atlasmap.core;

import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CompoundClassLoader extends ClassLoader {
    private static final Logger LOG = LoggerFactory.getLogger(CompoundClassLoader.class);

    private Set<ClassLoader> delegates = new HashSet<>();

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        for (ClassLoader cl : delegates) {
            try {
                return cl.loadClass(name);
            } catch (Throwable t) {
                LOG.debug("Class '{}' was not found with ClassLoader '{}': {}", name, cl);
                LOG.debug(t.getMessage(), t);
            }
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    public URL getResource(String name) {
        for (ClassLoader cl : delegates) {
            URL url = cl.getResource(name);
            if (url != null) {
                return url;
            }
            LOG.debug("Resource '{}' was not found with ClassLoader '{}': {}", name, cl);
        }
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) {
        List<URL> answer = new LinkedList<>();
        for (ClassLoader cl : delegates) {
            try {
                Enumeration<URL> urls = cl.getResources(name);
                while (urls != null && urls.hasMoreElements()) {
                    answer.add(urls.nextElement());
                }
            } catch (Exception e) {
                LOG.debug("I/O error while looking for a resource '{}' with ClassLoader '{}': {}", name, cl);
                LOG.debug(e.getMessage(), e);
            }
        }
        return Collections.enumeration(answer);
    }

    public synchronized void add(ClassLoader cl) {
        delegates.add(cl);
    }

}