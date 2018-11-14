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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.atlasmap.api.AtlasException;

public class AtlasLibraryLoader extends ClassLoader {
    private String saveDir;
    private URLClassLoader urlClassLoader;
    private Set<ClassLoader> alternativeLoaders = new HashSet<>();
    private Set<AtlasLibraryLoaderListener> listeners = new HashSet<>();

    public AtlasLibraryLoader(String saveDir) throws AtlasException {
        super(Thread.currentThread().getContextClassLoader());
        this.saveDir = saveDir;
        File saveDirRef = new File(saveDir);
        if (!new File(saveDir).exists()) {
            saveDirRef.mkdirs();
        }
        if (!saveDirRef.isDirectory()) {
            throw new AtlasException(String.format("'%s' is not a directory", saveDirRef.getName()));
        }

        List<URL> urls = new LinkedList<>();
        File[] files = saveDirRef.listFiles();
        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(".jar")) {
                try {
                    urls.add(f.toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new AtlasException(e);
                }
            }
        }

        if (urls.size() > 0) {
            this.urlClassLoader = new URLClassLoader(urls.toArray(new URL[0]));
        }
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
        URL[] urls;
        if (this.urlClassLoader != null) {
            URL[] origUrls = this.urlClassLoader.getURLs();
            urls = new URL[origUrls.length + 1];
            System.arraycopy(origUrls, 0, urls, 0, origUrls.length);
        } else {
            urls = new URL[1];
        }
        urls[urls.length-1] = dest.toURI().toURL();
        this.urlClassLoader = new URLClassLoader(urls);
        listeners.forEach(l -> l.onUpdate(this));
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (this.urlClassLoader != null) {
            try {
                return this.urlClassLoader.loadClass(name);
            } catch (ClassNotFoundException e) {}
        }
        if (!this.alternativeLoaders.isEmpty()) {
            for (ClassLoader cl : this.alternativeLoaders) {
                try {
                    return cl.loadClass(name);
                } catch (ClassNotFoundException e) {
                    continue;
                }
            }
        }
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {}
        return super.loadClass(name);
    }

    @Override
    public URL getResource(String name) {
        URL answer;
        if (this.urlClassLoader != null) {
             answer = this.urlClassLoader.getResource(name);
             if (answer != null) {
                 return answer;
             }
        }
        if (!this.alternativeLoaders.isEmpty()) {
            for (ClassLoader cl : this.alternativeLoaders) {
                answer = cl.getResource(name);
                if (answer != null) {
                    return answer;
                }
            }
        }
        answer = Thread.currentThread().getContextClassLoader().getResource(name);
        if (answer != null) {
            return answer;
        }
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Set<URL> answer = new HashSet<>();
        if (this.urlClassLoader != null) {
            for (Enumeration<URL> e = this.urlClassLoader.getResources(name); e.hasMoreElements();) {
                answer.add(e.nextElement());
            }
        }
        if (!this.alternativeLoaders.isEmpty()) {
            for (ClassLoader cl : this.alternativeLoaders) {
                for (Enumeration<URL> e = cl.getResources(name); e.hasMoreElements();) {
                    answer.add(e.nextElement());
                }
            }
        }
        for (Enumeration<URL> e = Thread.currentThread().getContextClassLoader().getResources(name); e.hasMoreElements();) {
            answer.add(e.nextElement());
        }
        for (Enumeration<URL> e = super.getResources(name); e.hasMoreElements();) {
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
                 return answer;
             }
        }
        if (!this.alternativeLoaders.isEmpty()) {
            for (ClassLoader cl : this.alternativeLoaders) {
                answer = cl.getResourceAsStream(name);
                if (answer != null) {
                    return answer;
                }
            }
        }
        answer = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        if (answer != null) {
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
