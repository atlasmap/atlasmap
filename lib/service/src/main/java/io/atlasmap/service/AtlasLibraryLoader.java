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
package io.atlasmap.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;

public class AtlasLibraryLoader extends ClassLoader {
    private static final Logger LOG = LoggerFactory.getLogger(AtlasLibraryLoader.class);

    private File saveDir;
    private URLClassLoader urlClassLoader;
    private Set<ClassLoader> alternativeLoaders = new HashSet<>();
    private Set<AtlasLibraryLoaderListener> listeners = new HashSet<>();

    public AtlasLibraryLoader(String saveDirName) throws AtlasException {
        super(AtlasLibraryLoader.class.getClassLoader());
        LOG.debug("Using {} as a lib directory", saveDirName);
        this.saveDir = new File(saveDirName);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        if (!saveDir.isDirectory()) {
            throw new AtlasException(String.format("'%s' is not a directory", saveDir.getName()));
        }
        reload();
    }

    public void addJarFromStream(InputStream is) throws Exception {
        File dest = new File(saveDir + File.separator + UUID.randomUUID().toString() + ".jar");
        while (dest.exists()) {
            dest = new File(saveDir + File.separator + UUID.randomUUID().toString() + ".jar");
        }
        FileOutputStream buffer = new FileOutputStream(dest);
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        buffer.close();
        List<URL> urls = new LinkedList<>();
        urls.add(dest.toURI().toURL());
        if (this.urlClassLoader != null) {
            URL[] origUrls = this.urlClassLoader.getURLs();
            urls.addAll(Arrays.asList(origUrls));
        }
        reload();
    }

    public void clearLibaries() {
        File[] files = saveDir.listFiles();
        if (!saveDir.exists() || !saveDir.isDirectory() || files == null) {
            return;
        }
        for (File f : saveDir.listFiles()) {
            f.delete();
        }
        reload();
    }

    public void reload() {
        List<URL> urls = new LinkedList<>();
        File[] files = saveDir.listFiles();
        if (!saveDir.exists() || !saveDir.isDirectory() || files == null) {
            return;
        }

        for (File f : files) {
            try {
                if (!f.isFile()) {
                    LOG.warn("Ignoring invalid file {}", f.getAbsolutePath());
                    continue;
                }
                urls.add(f.toURI().toURL());
            } catch (Exception e) {
                LOG.warn("Ignoring invalid file", e);
            }
        }
        // This won't work on hierarchical class loader like JavaEE or OSGi.
        // We don't have any plan to get design time services working on those though.
        this.urlClassLoader = urls.size() == 0 ? null
         : new URLClassLoader(urls.toArray(new URL[0]), AtlasLibraryLoader.class.getClassLoader());
        listeners.forEach(l -> l.onUpdate(this));
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        LOG.debug("Loading Class:{}", name);
        if (this.urlClassLoader != null) {
            try {
                return this.urlClassLoader.loadClass(name);
            } catch (Throwable t) {
                LOG.debug("Class not found: [ClassLoader:<uploaded jar>, Class name:{}, message:{}]",
                    name, t.getMessage());
            }
        }
        if (!this.alternativeLoaders.isEmpty()) {
            for (ClassLoader cl : this.alternativeLoaders) {
                try {
                    return cl.loadClass(name);
                } catch (Throwable t) {
                    LOG.debug("Class not found: [ClassLoader:{}, Class name:{}, message:{}]",
                        cl, name, t.getMessage());
                    continue;
                }
            }
        }
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            return tccl.loadClass(name);
        } catch (Throwable t) {
            LOG.debug("Class not found: [ClassLoader:{}, class name:{}, message:{}]",
                tccl, name, t.getMessage());
        }
        return super.loadClass(name);
    }

    @Override
    public URL getResource(String name) {
        URL answer;
        if (this.urlClassLoader != null) {
             answer = this.urlClassLoader.getResource(name);
             if (answer != null) {
                 LOG.debug("Found resource:[ClassLoader:{}, name:{}]", this.urlClassLoader, name);
                 return answer;
             }
        }
        if (!this.alternativeLoaders.isEmpty()) {
            for (ClassLoader cl : this.alternativeLoaders) {
                answer = cl.getResource(name);
                if (answer != null) {
                    LOG.debug("Found resource:[ClassLoader:{}, name:{}]", cl, name);
                    return answer;
                }
            }
        }
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        answer = tccl.getResource(name);
        if (answer != null) {
            LOG.debug("Found resource:[ClassLoader:{}, name:{}]", tccl, name);
            return answer;
        }
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Set<URL> answer = new HashSet<>();
        if (this.urlClassLoader != null) {
            for (Enumeration<URL> e = this.urlClassLoader.getResources(name); e.hasMoreElements();) {
                LOG.debug("Found resource:[ClassLoader:{}, name:{}]", this.urlClassLoader, name);
                answer.add(e.nextElement());
            }
        }
        if (!this.alternativeLoaders.isEmpty()) {
            for (ClassLoader cl : this.alternativeLoaders) {
                for (Enumeration<URL> e = cl.getResources(name); e.hasMoreElements();) {
                    LOG.debug("Found resource:[ClassLoader:{}, name:{}]", cl, name);
                    answer.add(e.nextElement());
                }
            }
        }
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        for (Enumeration<URL> e = tccl.getResources(name); e.hasMoreElements();) {
            LOG.debug("Found resource:[ClassLoader:{}, name:{}]", tccl, name);
            answer.add(e.nextElement());
        }
        for (Enumeration<URL> e = super.getResources(name); e.hasMoreElements();) {
            LOG.debug("Found resource:[ClassLoader:parent, name:{}]", this, name);
            answer.add(e.nextElement());
        }
        return new Enumeration<URL>() {
            Iterator<URL> iterator = answer.iterator();
            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public URL nextElement() {
                return iterator.next();
            }
        };
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        InputStream answer;
        if (this.urlClassLoader != null) {
            answer = this.urlClassLoader.getResourceAsStream(name);
            if (answer != null) {
                LOG.debug("Found resource:[ClassLoader:{}, name:{}]", this.urlClassLoader, name);
                return answer;
            }
        }
        if (!this.alternativeLoaders.isEmpty()) {
            for (ClassLoader cl : this.alternativeLoaders) {
                answer = cl.getResourceAsStream(name);
                if (answer != null) {
                    LOG.debug("Found resource:[ClassLoader:{}, name:{}]", cl, name);
                    return answer;
                }
            }
        }
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        answer = tccl.getResourceAsStream(name);
        if (answer != null) {
            LOG.debug("Found resource:[ClassLoader:{}, name:{}]", tccl, name);
            return answer;
        }
        return super.getResourceAsStream(name);
    }

    public boolean isEmpty() {
        return this.urlClassLoader == null;
    }

    public void addAlternativeLoader(ClassLoader cl) {
        this.alternativeLoaders.add(cl);
    }

    public void addListener(AtlasLibraryLoaderListener listener) {
        this.listeners.add(listener);
    }

    public interface AtlasLibraryLoaderListener {
        public void onUpdate(AtlasLibraryLoader loader);
    }
}
