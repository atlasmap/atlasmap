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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasSession;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationStatus;

public class AtlasUtil {
    public static final int SPLIT_LIMIT = 4;
    public static final String NEW_LINE_CHARS = "(?m)$^|[\\r\\n]+\\z";
    private static final Logger LOG = LoggerFactory.getLogger(AtlasUtil.class);

    public static Properties loadPropertiesFromURL(URL url) throws Exception {
        try (InputStream is = url.openStream()) {
            Properties prop = new Properties();
            prop.load(is);
            return prop;
        }
    }

    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty() || string.matches("^\\s+$");
    }

    public static boolean matchUriModule(String uriA, String uriB) {
        if (uriA == null || uriB == null) {
            return false;
        }

        if (getUriModule(uriA) == null || getUriModule(uriB) == null) {
            return false;
        }

        return getUriModule(uriA).equalsIgnoreCase(getUriModule(uriB));

    }

    protected static void validateUri(String atlasUri) {
        if (!atlasUri.startsWith("atlas:")) {
            throw new IllegalStateException(
                    "Invalid atlas uri " + atlasUri + " does not begin with 'atlas:': " + atlasUri);
        }

        if (countCharacters(atlasUri, '?') > 1) {
            throw new IllegalStateException("Invalid atlas uri " + atlasUri + " multiple '?' characters: " + atlasUri);
        }
    }

    protected static List<String> getUriPartsAsArray(String atlasUri) {

        if (atlasUri == null) {
            return null;
        }

        if (AtlasUtil.isEmpty(atlasUri)) {
            return Arrays.asList(new String[0]);
        }

        validateUri(atlasUri);

        String[] pass1 = null;
        if (atlasUri.contains("?")) {
            pass1 = atlasUri.split("\\?", 2);
        }

        List<String> parts = new ArrayList<String>();
        if (pass1 != null && pass1.length >= 1) {
            parts.addAll(Arrays.asList(pass1[0].split(":", 4)));
        } else {
            parts.addAll(Arrays.asList(atlasUri.split(":", 4)));
        }

        return parts;
    }

    /**
     * Returns the "scheme" piece of an Atlas uri
     *
     * ie. atlas:stringseparated:csv?quoteChar=&quot;
     *
     *
     * scheme: atlas module: stringseparated remaining: csv config: quoteChar=&quot;
     *
     * if atlasUri is null, returns null. if empty or no scheme present, returns
     * empty. otherwise, the $scheme is returned
     *
     *@param atlasUri URI string
     * @return URI scheme
     *
     */
    public static String getUriScheme(String atlasUri) {
        List<String> uriA = AtlasUtil.getUriPartsAsArray(atlasUri);
        if (uriA == null || uriA.size() < 1 || isEmpty(uriA.get(0))) {
            return null;
        }

        return uriA.get(0);
    }

    public static String getUriModule(String atlasUri) {
        List<String> uriA = AtlasUtil.getUriPartsAsArray(atlasUri);
        if (uriA == null || uriA.size() < 2 || isEmpty(uriA.get(1))) {
            return null;
        }

        return uriA.get(1);
    }

    public static String getUriDataType(String atlasUri) {
        List<String> uriA = AtlasUtil.getUriPartsAsArray(atlasUri);
        if (uriA == null || uriA.size() < 3 || isEmpty(uriA.get(2))) {
            return null;
        }

        return uriA.get(2);
    }

    public static String getUriModuleVersion(String atlasUri) {
        List<String> uriA = AtlasUtil.getUriPartsAsArray(atlasUri);
        if (uriA == null || uriA.size() < 4 || isEmpty(uriA.get(3))) {
            return null;
        }

        return uriA.get(3);
    }

    public static String getUriParameterValue(String atlasUri, String key) {
        Map<String, String> params = getUriParameters(atlasUri);
        if (params == null || params.isEmpty()) {
            return null;
        }

        return params.get(key);
    }

    public static Map<String, String> getUriParameters(String atlasUri) {

        if (atlasUri == null) {
            return null;
        }

        Map<String, String> params = new HashMap<String, String>();
        if (AtlasUtil.isEmpty(atlasUri)) {
            return params;
        }

        validateUri(atlasUri);

        String[] pass1 = null;
        if (atlasUri.contains("?")) {
            pass1 = atlasUri.split("\\?", 2);
        }

        if (pass1 == null || pass1.length < 2 || pass1[1] == null || pass1[1].length() < 1) {
            return params;
        }

        String allParams = null;
        try {
            allParams = URLDecoder.decode(pass1[1], "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unable to parse uri" + atlasUri + " for configuration parameters", e);
            return params;
        }

        if (allParams == null) {
            return null;
        }

        String[] configs = allParams.split("&", SPLIT_LIMIT);
        if (configs == null || configs.length < 1) {
            return params;
        }

        for (int i = 0; i < configs.length; i++) {
            if (!configs[i].contains("=")) {
                LOG.warn("Invalid configuration parameter: " + configs[i] + " for uri: '" + atlasUri + "'");
                continue;
            }

            String[] cfgs = configs[i].split("=");
            if (cfgs == null || cfgs.length != 2) {
                LOG.warn("Invalid configuration parameter: " + configs[i] + " for uri: '" + atlasUri + "'");
                continue;
            }

            params.put(cfgs[0], cfgs[1]);
        }

        return params;
    }

    public static int countCharacters(String text, char match) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == match) {
                count++;
            }
        }
        return count;
    }

    public static List<Class<?>> findClassesForPackage(String scannedPackage) {
        String scannedPath = scannedPackage.replace('.', '/');
        URL scannedUrl = getResource(scannedPath);

        if (scannedUrl == null) {
            throw new IllegalArgumentException(String.format("Unable to detect resources for url='%s' for package='%s'",
                    scannedPath, scannedPackage));
        }

        if ("jar".equals(scannedUrl.getProtocol())) {
            return findClassesFromJar(scannedUrl);
        }

        File scannedFd = new File(scannedUrl.getFile());
        List<Class<?>> classes = new ArrayList<Class<?>>();

        if (scannedFd.listFiles() == null) {
            return classes;
        }

        for (File file : scannedFd.listFiles()) {
            classes.addAll(find(file, scannedPackage));
        }

        return classes;
    }

    public static void addAudit(AtlasSession session, String docId, String message, String path, AuditStatus status,
            String value) {
        session.getAudits().getAudit().add(createAudit(status, docId, path, value, message));
    }

    public static Audit createAudit(AuditStatus status, String docId, String path, String value, String message) {
        Audit audit = new Audit();
        audit.setDocId(docId);
        audit.setMessage(message);
        audit.setPath(path);
        audit.setStatus(status);
        audit.setValue(value);
        return audit;
    }

    public static void addAudit(AtlasSession session, Validation validation) {
        Audit audit = new Audit();
        audit.setDocId(validation.getId());
        audit.setMessage(validation.getMessage());
        audit.setStatus(AtlasUtil.toAuditStatus(validation.getStatus()));
    }

    public static AuditStatus toAuditStatus(ValidationStatus vstatus) {
        switch (vstatus) {
        case ERROR:
            return AuditStatus.ERROR;
        case WARN:
            return AuditStatus.WARN;
        case INFO:
            return AuditStatus.INFO;
        case ALL:
            return AuditStatus.ALL;
        case NONE:
            return AuditStatus.NONE;
        default:
            return null;
        }
    }

    protected static URL getResource(String scannedPath) {
        URL url = null;

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = AtlasUtil.class.getClassLoader();
        }

        if (classLoader != null) {
            url = classLoader.getResource(scannedPath);
            if (url != null) {
                return url;
            }
        }

        return ClassLoader.getSystemResource(scannedPath);
    }

    protected static List<Class<?>> find(File file, String scannedPackage) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        String resource = scannedPackage + '.' + file.getName();
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                classes.addAll(find(child, resource));
            }
        } else if (resource.endsWith(".class")) {
            int endIndex = resource.length() - ".class".length();
            String className = resource.substring(0, endIndex);
            try {
                classes.add(Class.forName(className));
            } catch (ClassNotFoundException ignore) {
            }
        }
        return classes;
    }

    protected static List<Class<?>> findClassesFromJar(URL jarFileUrl) {
        List<Class<?>> classNames = new ArrayList<Class<?>>();

        JarURLConnection connection = null;

        try {
            connection = (JarURLConnection) jarFileUrl.openConnection();
        } catch (IOException e) {
            LOG.warn(String.format("Unable to load classes from jar file=%s msg=%s", jarFileUrl, e.getMessage()), e);
            return classNames;
        }

        try (ZipInputStream zip = new ZipInputStream(
                new FileInputStream(new File(connection.getJarFileURL().toURI())))) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace('/', '.');
                    className = className.substring(0, className.length() - ".class".length());
                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                    if (classLoader == null) {
                        classLoader = AtlasUtil.class.getClassLoader();
                    }
                    try {
                        Class<?> clazz = Class.forName(className, false, classLoader);
                        classNames.add(clazz);
                    } catch (ClassNotFoundException e) {
                        LOG.warn(String.format("Unable to load class=%s from jar file=%s msg=%s", className,
                                jarFileUrl, e.getMessage()), e);
                    }
                }
            }
        } catch (URISyntaxException | IOException e) {
            LOG.warn(String.format("Unable to load classes from jar file=%s msg=%s", jarFileUrl, e.getMessage()), e);
        }
        return classNames;
    }

    public static String getChainedMessage(Throwable t) {
        StringBuilder buf = new StringBuilder();
        buf.append(t.getMessage());
        Throwable target = t;
        while((target = target.getCause()) != null) {
            if (target.getMessage() != null && !target.getMessage().isEmpty()) {
                buf.append(" - ");
                buf.append(target.getMessage());
            }
        }
        return buf.toString();
    }
}
