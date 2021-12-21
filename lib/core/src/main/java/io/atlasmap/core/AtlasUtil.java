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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasSession;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.ComplexType;
import io.atlasmap.v2.Document;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationStatus;

/**
 * A collection of utility methods.
 */
public class AtlasUtil {
    /** split limit. */
    public static final int SPLIT_LIMIT = 4;
    /** new line chars. */
    public static final String NEW_LINE_CHARS = "(?m)$^|[\\r\\n]+\\z";
    private static final Logger LOG = LoggerFactory.getLogger(AtlasUtil.class);

    /**
     * Loads the property file from the specified URL.
     * @param url property file URL
     * @return loaded properties
     * @throws Exception unexpected error
     */
    public static Properties loadPropertiesFromURL(URL url) throws Exception {
        try (InputStream is = url.openStream()) {
            Properties prop = new Properties();
            prop.load(is);
            return prop;
        }
    }

    /**
     * Gets if the specified string is null or empty. Note that unlike {@link String#isEmpty()},
     * the whitespace(s) is also considered empty.
     * @param string string to test
     * @return true if it's null or empty, or false
     */
    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty() || string.matches("^\\s+$");
    }

    /**
     * Gets if the module part of the URI match.
     * @param uriA URI A
     * @param uriB URI B
     * @return true if the module part of the URI match
     */
    public static boolean matchUriModule(String uriA, String uriB) {
        if (uriA == null || uriB == null) {
            return false;
        }

        if (getUriModule(uriA) == null || getUriModule(uriB) == null) {
            return false;
        }

        return getUriModule(uriA).equalsIgnoreCase(getUriModule(uriB));

    }

    /**
     * Validates the URI.
     * @param atlasUri URI
     */
    protected static void validateUri(String atlasUri) {
        if (!atlasUri.startsWith("atlas:")) {
            throw new IllegalStateException(
                    "Invalid atlas uri " + atlasUri + " does not begin with 'atlas:': " + atlasUri);
        }

        if (countCharacters(atlasUri, '?') > 1) {
            throw new IllegalStateException("Invalid atlas uri " + atlasUri + " multiple '?' characters: " + atlasUri);
        }
    }

    /**
     * Parses the URI and return a list of the parts.
     * @param atlasUri URI
     * @return a list of the URI parts
     */
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

    /**
     * Gets the module part of the URI.
     * @param atlasUri URI
     * @return module part
     */
    public static String getUriModule(String atlasUri) {
        List<String> uriA = AtlasUtil.getUriPartsAsArray(atlasUri);
        if (uriA == null || uriA.size() < 2 || isEmpty(uriA.get(1))) {
            return null;
        }

        return uriA.get(1);
    }

    /**
     * Gets the data type part of the URI.
     * @param atlasUri URI
     * @return data part
     */
    public static String getUriDataType(String atlasUri) {
        List<String> uriA = AtlasUtil.getUriPartsAsArray(atlasUri);
        if (uriA == null || uriA.size() < 3 || isEmpty(uriA.get(2))) {
            return null;
        }

        return uriA.get(2);
    }

    /**
     * Gets the version part of the URI.
     * @param atlasUri URI
     * @return version part
     */
    public static String getUriModuleVersion(String atlasUri) {
        List<String> uriA = AtlasUtil.getUriPartsAsArray(atlasUri);
        if (uriA == null || uriA.size() < 4 || isEmpty(uriA.get(3))) {
            return null;
        }

        return uriA.get(3);
    }

    /**
     * Gets the parameter value in the URI.
     * @param atlasUri URI
     * @param key parameter key
     * @return parameter value
     */
    public static String getUriParameterValue(String atlasUri, String key) {
        Map<String, String> params = getUriParameters(atlasUri);
        if (params == null || params.isEmpty()) {
            return null;
        }

        return params.get(key);
    }

    /**
     * Gets the URI parameters.
     * @param atlasUri URI
     * @return map of the URI parameters
     */
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

    /**
     * Counts the specified character in the URI.
     * @param text text
     * @param match character to count
     * @return count
     */
    public static int countCharacters(String text, char match) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == match) {
                count++;
            }
        }
        return count;
    }

    /**
     * Adds the Audit into the session.
     * @param session session
     * @param field field
     * @param message message
     * @param status audit status
     * @param value value
     */
    public static void addAudit(AtlasInternalSession session, Field field,
            String message, AuditStatus status, String value) {
        String docId = field != null ? field.getDocId() : null;
        String docName = session != null ? getDocumentNameById(session, docId) : null;
        String path = field != null ? field.getPath() : null;
        session.getAudits().getAudit().add(
                createAudit(status, docId, docName, path, value, message));
    }

    /**
     * Adds the Audit into the session.
     * @param session session
     * @param docId Document ID
     * @param message message
     * @param status audit status
     * @param value value
     */
    public static void addAudit(AtlasInternalSession session, String docId,
            String message, AuditStatus status, String value) {
        String docName = session != null ? getDocumentNameById(session, docId) : null;
        session.getAudits().getAudit().add(
                createAudit(status, docId, docName, null, value, message));
    }

    /**
     * Creates the Audit.
     * @param status audit status
     * @param docId Document ID
     * @param docName Document name
     * @param path field path
     * @param value value
     * @param message message
     * @return audit
     */
    public static Audit createAudit(AuditStatus status, String docId, String docName,
            String path, String value, String message) {
        Audit audit = new Audit();
        audit.setDocId(docId);
        audit.setDocName(docName);
        audit.setMessage(message);
        audit.setPath(path);
        audit.setStatus(status);
        audit.setValue(value);
        return audit;
    }

    /**
     * Converts from the Validation to the Audit and add into the session.
     * @param session session
     * @param validation validation
     */
    public static void addAudit(AtlasSession session, Validation validation) {
        Audit audit = new Audit();
        audit.setDocId(validation.getDocId());
        audit.setDocName(validation.getDocName());
        audit.setMessage(validation.getMessage());
        audit.setStatus(AtlasUtil.toAuditStatus(validation.getStatus()));
        session.getAudits().getAudit().add(audit);
    }

    /**
     * Adds the list of Audit into the session.
     * @param session session
     * @param field field
     * @param audits a list of audit
     */
    public static void addAudits(AtlasInternalSession session, Field field, List<Audit> audits) {
        String docId = field.getDocId();
        String docName = getDocumentNameById(session, docId);
        for (Audit audit: audits) {
            audit.setDocId(docId);
            audit.setDocName(docName);
            session.getAudits().getAudit().add(audit);
        }
    }

    /**
     * Converts from the validation status to the audit status.
     * @param vstatus validation status
     * @return audit status
     */
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

    /**
     * Creates a string expression from the validation.
     * @param validation validation
     * @return string representation of the validation
     */
    public static String validationToString(Validation validation) {
        String output = "[Validation ";
    
        if (validation == null) {
            return output + ">null< ]";
        }
    
        if (validation.getScope() != null) {
            output = output + " scope=" + validation.getScope();
        }
        if (validation.getId() != null) {
            output = output + " id=" + validation.getId();
        }
        if (validation.getStatus() != null) {
            output = output + " status=" + validation.getStatus().value();
        }
        if (validation.getMessage() != null) {
            output = output + " msg=" + validation.getMessage();
        }
    
        return output + "]";
    }

    /**
     * Gets the Document name of the one has the specified Document ID.
     * @param session session
     * @param docId Document ID
     * @return Document name
     */
    public static String getDocumentNameById(AtlasInternalSession session, String docId) {
        if (session == null || docId == null) {
            return null;
        }
        AtlasModule module = session.resolveModule(docId);
        return module != null ? module.getDocName() : null;
    }

    /**
     * Gets the classpath resource.
     * @param scannedPath path
     * @return resource URL
     */
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

    /**
     * Gets the combined message from the chained causes of the specified Throwable.
     * @param t throwable
     * @return combined message
     */
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

    /**
     * Escapes the special characters for URI.
     * @param source string to escape
     * @return escaped
     */
    public static String escapeForUri(String source) {
        if (source == null) {
            return null;
        }
        return source.replaceAll(Pattern.quote("!"), "%21")
                     .replaceAll(Pattern.quote("#"), "%23")
                     .replaceAll(Pattern.quote("$"), "%24")
                     .replaceAll(Pattern.quote("&"), "%26")
                     .replaceAll(Pattern.quote("'"), "%27")
                     .replaceAll(Pattern.quote("("), "%28")
                     .replaceAll(Pattern.quote(")"), "%29")
                     .replaceAll(Pattern.quote("*"), "%2A")
                     .replaceAll(Pattern.quote("+"), "%2B")
                     .replaceAll(Pattern.quote(","), "%2C")
                     .replaceAll(Pattern.quote("/"), "%2F")
                     .replaceAll(Pattern.quote(":"), "%3A")
                     .replaceAll(Pattern.quote(";"), "%3B")
                     .replaceAll(Pattern.quote("="), "%3D")
                     .replaceAll(Pattern.quote("?"), "%3F")
                     .replaceAll(Pattern.quote("@"), "%40")
                     .replaceAll(Pattern.quote("["), "%5B")
                     .replaceAll(Pattern.quote("]"), "%5D");
    }

    /**
     * Unescapes the special characters in the URI.
     * @param uri URI
     * @return unescaped
     */
    public static String unescapeFromUri(String uri) {
        if (uri == null) {
            return null;
        }
        return uri.replaceAll("%21", "!")
                  .replaceAll("%23", "#")
                  .replaceAll("%24", "$")
                  .replaceAll("%26", "&")
                  .replaceAll("%27", "'")
                  .replaceAll("%28", "(")
                  .replaceAll("%29", ")")
                  .replaceAll("%2A", "*")
                  .replaceAll("%2B", "+")
                  .replaceAll("%2C", ",")
                  .replaceAll("%2F", "/")
                  .replaceAll("%3A", ":")
                  .replaceAll("%3B", ";")
                  .replaceAll("%3D", "=")
                  .replaceAll("%3F", "?")
                  .replaceAll("%40", "@")
                  .replaceAll("%5B", "[")
                  .replaceAll("%5D", "]");
    }

    /**
     * Delete specified directory and the contents in it.
     * @see #deleteDirectoryContents
     * @param targetDir The target directory to remove
     */
    public static void deleteDirectory(File targetDir) {
        File[] allContents = targetDir.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                if (!file.isDirectory()) {
                    file.delete();
                } else {
                    deleteDirectory(file);
                }
            }
        }
        targetDir.delete();
        return;
    }

    /**
     * Delete all contents in the specified directory.
     * @see #deleteDirectory
     * @param targetDir The target directory to remove the contents
     */
    public static void deleteDirectoryContents(File targetDir) {
        File[] allContents = targetDir.listFiles();
        if (allContents != null) {
            for (File element : allContents) {
                if (element.isFile()) {
                    element.delete();
                } else if (element.isDirectory()) {
                    deleteDirectory(element);
                }
            }
        }
        return;
    }

    /**
     * Copies the one file to the other.
     * @param sourcePath source file path
     * @param destPath target file path
     * @throws IOException unexpected error
     */
    public static void copyFile(Path sourcePath, Path destPath) throws IOException {
        File source = new File(sourcePath.toString());
        File dest = new File(destPath.toString());

        if (!dest.exists()) {
            dest.createNewFile();
        }
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(dest);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
        finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Exclude fields which are not on requested paths.
     *
     * If includePaths is null or empty it does nothing and all fields are included.
     *
     * @param document document
     * @param includePaths paths
     */
    public static void excludeNotRequestedFields(Document document, List<String> includePaths) {
        if (includePaths == null || includePaths.isEmpty()) {
            return;
        }

        List<Field> fields = document.getFields().getField();
        List<String> paths = new ArrayList<>();
        for (String includePath: includePaths) {
            if (!includePath.endsWith("/")) {
                includePath += "/";
            }
            paths.add(includePath);
        }
        includePaths = paths;

        Deque<List<? extends Field>> stack = new ArrayDeque<>();
        stack.push(fields);
        while(!stack.isEmpty()) {
            List<? extends Field> fieldsToCheck = stack.pop();
            for (Iterator<? extends Field> it = fieldsToCheck.iterator(); it.hasNext(); ) {
                Field field = it.next();
                String path = field.getPath();
                boolean includeField = path.lastIndexOf("/") == 0; //always include root level
                if (!includeField) {
                    for (String includePath : includePaths) {
                        int lastSeparator = path.lastIndexOf("/");
                        String parentPath = path.substring(0, lastSeparator);
                        parentPath += "/";
                        if (includePath.startsWith(parentPath)) {
                            includeField = true;
                            break;
                        }
                    }

                }
                if (includeField) {
                    if (field instanceof ComplexType) {
                        ComplexType complexTypeField = (ComplexType) field;
                        if (complexTypeField.getChildFields() != null) {
                            List<? extends Field> subfields = complexTypeField.getChildFields();
                            stack.push(subfields); //analyze subfields
                        }
                    }
                } else {
                    it.remove();
                }

            }
        }
    }
}
