/**
 * Copyright (C) 2018 Red Hat, Inc.
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
package io.atlasmap.core.v3;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import io.atlasmap.api.v3.Message;
import io.atlasmap.api.v3.Parameter;
import io.atlasmap.api.v3.Parameter.Role;
import io.atlasmap.spi.v3.BaseTransformation;
import io.atlasmap.spi.v3.DataHandler;
import io.atlasmap.spi.v3.util.AtlasException;
import io.atlasmap.spi.v3.util.AtlasRuntimeException;

/**
 *
 */
class Context {

    private static final String META_FILE_PATH_PREFIX = "META-INF/services/io.atlasmap.";
    static final String DATA_HANDLER_META_FILE_PATH = META_FILE_PATH_PREFIX + "datahandler";
    static final String TRANSFORMATIONS_META_FILE_PATH = META_FILE_PATH_PREFIX + "transformations";

    final Set<Message> messages = new HashSet<>();
    final Set<Class<? extends DataHandler>> dataHandlerClasses = new HashSet<>();
    final MappingDocumentImpl mappingDocument;
    final Set<TransformationDescriptorImpl> transformationDescriptors = new TreeSet<>();
    final Set<DataDocumentDescriptor> dataDocumentDescriptors = new HashSet<>();

    Context(File mappingFile) throws AtlasException {
        loadDataHandlers(DATA_HANDLER_META_FILE_PATH);
        loadTransformations(TRANSFORMATIONS_META_FILE_PATH);
        mappingDocument = new MappingDocumentImpl(this, mappingFile);
        if (mappingFile.exists()) {
            mappingDocument.load();
        }
    }

    void loadDataHandlers(String metaFilePath) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        Enumeration<URL> urls;
        try {
            urls = classLoader.getResources(metaFilePath);
        } catch (IOException e) {
            throw new AtlasRuntimeException(e, "Unable to find data handler meta files at %s", metaFilePath);
        }
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                for (String className = reader.readLine(); className != null; className = reader.readLine()) {
                    className = className.trim();
                    if (className.isEmpty()) {
                        continue;
                    }
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (!isSubclass(clazz, DataHandler.class)) {
                            throw new AtlasRuntimeException("Class %s in meta file %s does not subclass %s",
                                                            className, url, DataHandler.class);
                        }
                        @SuppressWarnings("unchecked")
                        Class<? extends DataHandler> handlerClass = (Class<? extends DataHandler>)clazz;
                        try {
                            DataHandler handler = handlerClass.getDeclaredConstructor().newInstance();
                            String[] formats = handler.supportedDataFormats();
                            if (formats == null || formats.length == 0) {
                                throw new AtlasRuntimeException("Data handler %s supports no data formats", handlerClass);
                            }
                        } catch (NoSuchMethodException e) {
                            throw new AtlasRuntimeException(e, "No default constructor found for data handler %s", handlerClass);
                        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                | InvocationTargetException | SecurityException e) {
                            throw new AtlasRuntimeException(e, "Unable to create data handler %s", handlerClass);
                        }
                        dataHandlerClasses.add(handlerClass);
                    } catch (ClassNotFoundException e) {
                        throw new AtlasRuntimeException(e, "Unable to load class %s from meta file %s", className, url);
                    }
                }
            } catch (IOException e) {
                throw new AtlasRuntimeException(e, "Unable to read meta file %s", url);
            }
        }
    }

    void loadTransformations(String metaFilePath) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        Enumeration<URL> urls;
        try {
            urls = classLoader.getResources(metaFilePath);
        } catch (IOException e) {
            throw new AtlasRuntimeException(e, "Unable to find transformations meta files at %s", metaFilePath);
        }
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                for (String className = reader.readLine(); className != null; className = reader.readLine()) {
                    className = className.trim();
                    if (className.isEmpty()) {
                        continue;
                    }
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (!isSubclass(clazz, BaseTransformation.class)) {
                            throw new AtlasRuntimeException("Class %s in meta file %s does not subclass %s",
                                                            className, url, BaseTransformation.class);
                        }
                        @SuppressWarnings("unchecked")
                        Class<BaseTransformation> transformationClass = (Class<BaseTransformation>)clazz;
                        try {
                            BaseTransformation transformation = transformationClass.getDeclaredConstructor().newInstance();
                            // Verify transformation has at least one parameter with a target role
                            boolean targetRoleFound = false;
                            for (Parameter parameter : transformation.parameters()) {
                                if (parameter.role() == Role.OUTPUT) {
                                    targetRoleFound = true;
                                    break;
                                }
                            }
                            if (!targetRoleFound) {
                                throw new AtlasRuntimeException("Class %s in meta file %s does not have a parameter with a %s role",
                                                                transformationClass, url, Role.OUTPUT);
                            }
                            transformationDescriptors.add(new TransformationDescriptorImpl(transformation.name(),
                                                                                           transformation.description(),
                                                                                           transformationClass));
                        } catch (NoSuchMethodException e) {
                            throw new AtlasRuntimeException(e, "No default constructor found for transformation %s", transformationClass);
                        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                | InvocationTargetException | SecurityException e) {
                            throw new AtlasRuntimeException(e, "Unable to create transformation: %s", transformationClass);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new AtlasRuntimeException(e, "Unable to load class %s from meta file %s", className, url);
                    }
                }
            } catch (IOException e) {
                throw new AtlasRuntimeException(e, "Unable to read meta file %s", url);
            }
        }
    }

    private boolean isSubclass(Class<?> clazz, Class<?> ofClass) {
        if (clazz == Object.class) {
            return false;
        }
        Class<?> superClass = clazz.getSuperclass();
        return superClass == ofClass || isSubclass(superClass, ofClass);
    }
}
