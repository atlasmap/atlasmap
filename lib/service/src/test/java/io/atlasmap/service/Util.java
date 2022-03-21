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
package io.atlasmap.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.ws.rs.core.UriInfo;

import io.atlasmap.v2.Audit;
import io.atlasmap.v2.Audits;

public class Util {
    
    static UriInfo generateTestUriInfo(String baseUri, String absoluteUri) throws Exception {
        return new TestUriInfo(new URI(baseUri), new URI(absoluteUri));
    }

    static String printAudit(Audits audits) {
        StringBuilder buf = new StringBuilder("Audits: ");
        for (Audit a : audits.getAudit()) {
            buf.append('[');
            buf.append(a.getStatus());
            buf.append(", message=");
            buf.append(a.getMessage());
            buf.append("], ");
        }
        return buf.toString();
    }

    static void createJarFile(String dir, String jarPath, boolean skipModel, boolean skipProcessor) throws Exception {
        new File(dir).mkdirs();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int answer = compiler.run(System.in, System.out, System.err,
                "-d", dir,
                "src/test/resources/upload/io/atlasmap/service/my/MyCustomMappingBuilder.java",
                "src/test/resources/upload/io/atlasmap/service/my/MyFieldActions.java",
                "src/test/resources/upload/io/atlasmap/service/my/MyFieldActionsModel.java");
        assertEquals(0, answer);
        File jarFile = new File(jarPath);
        if (jarFile.exists()) {
            jarFile.delete();
        }
        JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jarFile));
        jarOut.putNextEntry(new JarEntry("io/"));
        jarOut.closeEntry();
        jarOut.putNextEntry(new JarEntry("io/atlasmap/"));
        jarOut.closeEntry();
        jarOut.putNextEntry(new JarEntry("io/atlasmap/service/"));
        jarOut.closeEntry();
        jarOut.putNextEntry(new JarEntry("io/atlasmap/service/my/"));
        jarOut.closeEntry();
        JarEntry classEntry = new JarEntry("io/atlasmap/service/my/MyFieldActions.class");
        jarOut.putNextEntry(classEntry);
        byte[] buffer = new byte[1024];
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(dir + "/io/atlasmap/service/my/MyFieldActions.class"));
        int count = -1;
        while ((count = in.read(buffer)) != -1) {
            jarOut.write(buffer, 0, count);
        }
        in.close();
        jarOut.closeEntry();
        classEntry = new JarEntry("io/atlasmap/service/my/MyFieldActionsModel.class");
        jarOut.putNextEntry(classEntry);
        in = new BufferedInputStream(new FileInputStream(dir + "/io/atlasmap/service/my/MyFieldActionsModel.class"));
        count = -1;
        while ((count = in.read(buffer)) != -1) {
            jarOut.write(buffer, 0, count);
        }
        in.close();
        jarOut.closeEntry();
        classEntry = new JarEntry("io/atlasmap/service/my/MyCustomMappingBuilder.class");
        jarOut.putNextEntry(classEntry);
        in = new BufferedInputStream(new FileInputStream(dir + "/io/atlasmap/service/my/MyCustomMappingBuilder.class"));
        count = -1;
        while ((count = in.read(buffer)) != -1) {
            jarOut.write(buffer, 0, count);
        }
        in.close();
        jarOut.closeEntry();

        jarOut.putNextEntry(new JarEntry("META-INF/"));
        jarOut.closeEntry();
        jarOut.putNextEntry(new JarEntry("META-INF/services/"));
        jarOut.closeEntry();
        if (!skipProcessor) {
            JarEntry svcEntry = new JarEntry("META-INF/services/io.atlasmap.spi.AtlasFieldAction");
            jarOut.putNextEntry(svcEntry);
            in = new BufferedInputStream(new FileInputStream("src/test/resources/upload/META-INF/services/io.atlasmap.spi.AtlasFieldAction"));
            while ((count = in.read(buffer)) != -1) {
                jarOut.write(buffer, 0, count);
            }
            in.close();
            jarOut.closeEntry();
        }
        if (!skipModel) {
            JarEntry svcEntry = new JarEntry("META-INF/services/io.atlasmap.v2.Action");
            jarOut.putNextEntry(svcEntry);
            in = new BufferedInputStream(new FileInputStream("src/test/resources/upload/META-INF/services/io.atlasmap.v2.Action"));
            while ((count = in.read(buffer)) != -1) {
                jarOut.write(buffer, 0, count);
            }
            in.close();
            jarOut.closeEntry();
        }
        jarOut.close();
    }

}
