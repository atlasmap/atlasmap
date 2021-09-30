/*
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
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DefaultAtlasCompoundClassLoader extends CompoundClassLoader {
    private static final Logger LOG = LoggerFactory.getLogger(CompoundClassLoader.class);

    private Set<ClassLoader> delegates = new LinkedHashSet<>();

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        for (ClassLoader cl : classLoaders()) {
            try {
                return cl.loadClass(name);
            } catch (Throwable t) {
                LOG.debug("Class '{}' was not found with ClassLoader '{}': {}", name, cl);
                LOG.debug(t.getMessage(), t);
            }
        }
        throw new ClassNotFoundException(name);
    }

    private Set<ClassLoader> classLoaders() {
        Set<ClassLoader> answer = new LinkedHashSet<>(delegates);
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if (tccl != null && !tccl.equals(this)) {
            answer.add(tccl);
        }
        return answer;
    }

    @Override
    public URL getResource(String name) {
        for (ClassLoader cl : classLoaders()) {
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
        Set<URL> answer = new LinkedHashSet<>();
        for (ClassLoader cl : classLoaders()) {
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

    @Override
    public synchronized void addAlternativeLoader(ClassLoader cl) {
        if (cl != null && !this.equals(cl)) {
            delegates.add(cl);
        }
    }

}
