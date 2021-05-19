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
package io.atlasmap.v2;

import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ModelTestUtil {

    public static List<Action> getAllOOTBActions() throws Exception {
        List<Action> answer = new ArrayList<>();
        ClassLoader cl = Action.class.getClassLoader();
        Enumeration<URL> resources = cl.getResources("io/atlasmap/v2");
        while(resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            Path p = Paths.get(resource.toURI());
            if (!Files.isDirectory(p)) {
                continue;
            }
            Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        if (file.getFileName().toString().endsWith(".class")) {
                            String className = file.getFileName().toString();
                            className = className.substring(0, className.length() - 6);
                            Class<?> clazz = Class.forName("io.atlasmap.v2." + className);
                            if (Action.class.isAssignableFrom(clazz)) {
                                answer.add((Action)clazz.newInstance());
                            }
                        }
                    } catch (Exception e) {}
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        return answer;
    }

}
