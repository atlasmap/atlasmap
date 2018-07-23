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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.atlasmap.api.v3.DocumentRole;
import io.atlasmap.api.v3.Mapping;
import io.atlasmap.api.v3.MappingDocument;
import io.atlasmap.api.v3.Parameter;
import io.atlasmap.api.v3.Transformation;
import io.atlasmap.api.v3.TransformationDescriptor;
import io.atlasmap.core.transformation.MapTransformation;
import io.atlasmap.spi.v3.BaseParameter;
import io.atlasmap.spi.v3.BaseTransformation;
import io.atlasmap.spi.v3.DataHandler;
import io.atlasmap.spi.v3.util.AtlasException;
import io.atlasmap.spi.v3.util.AtlasRuntimeException;
import io.atlasmap.spi.v3.util.I18n;
import io.atlasmap.spi.v3.util.VerifyArgument;

/**
 *
 */
public class MappingDocumentImpl implements MappingDocument {

    private static final String VERSION = "1.0.0";
    private static final ObjectMapper JSON = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                                                               .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
                                                               .setSerializationInclusion(Include.NON_NULL);

    private final Context context;
    private final List<Mapping> mappings = new ArrayList<>();
    private boolean autoSaves = true;
    private final SerializedImage serializedImage = new SerializedImage();

    MappingDocumentImpl(Context context) throws AtlasException {
        this.context = context;
        if (context.mappingFile.exists()) {
            load();
        }
    }

    /**
     * @see MappingDocument#availableDataFormats(DocumentRole)
     */
    @Override
    public String[] availableDataFormats(DocumentRole type) {
        Set<String> formats = new HashSet<>();
        for (Class<? extends DataHandler> handlerClass : context.dataHandlerClasses) {
            try {
                DataHandler handler = handlerClass.getDeclaredConstructor().newInstance();
                formats.addAll(Arrays.asList(handler.supportedDataFormats()));
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new AtlasRuntimeException(e, "Unable to create data handler %s", handlerClass);
            }
        }
        return formats.toArray(new String[formats.size()]);
    }

    /**
     * @see MappingDocument#addDataDocument(String, DocumentRole, String, Object)
     */
    @Override
    public void addDataDocument(String id, DocumentRole role, String dataFormat, Object document) throws AtlasException {
        VerifyArgument.isNotEmpty("id", id);
        VerifyArgument.isNotNull("role", role);
        VerifyArgument.isNotEmpty("dataFormat", dataFormat);
        VerifyArgument.isNotNull("document", document);
        DataDocumentDescriptor descriptor = addDataDocument(id, role, dataFormat);
        descriptor.dataDocument = document;
        descriptor.handler.setDocument(document);
        for (Mapping mapping : mappings) {
            for (Transformation transformation : mapping.transformations()) {
                for (Parameter parameter : transformation.parameters()) {
                    parameter.setStringValue(parameter.stringValue());
                }
            }
        }
        autoSave();
    }

    /**
     * @see MappingDocument#removeDataDocument(String, DocumentRole)
     */
    @Override
    public void removeDataDocument(String id, DocumentRole role) {
        for (DataDocumentDescriptor descriptor : context.dataDocumentDescriptors) {
            if (descriptor.id().equals(id) && descriptor.role().equals(role)) {
                context.dataDocumentDescriptors.remove(descriptor);
                serializedImage.dataDocumentDescriptors.remove(descriptor.serializedImage);
                autoSave();
                return;
            }
        }
        throw new AtlasRuntimeException(I18n.localize("Attempt to remove document that does not exist: id=%s, role=%s", id, role));
    }

    /**
     * @see MappingDocument#addMapping()
     */
    @Override
    public Mapping addMapping() {
        MappingImpl mapping = new MappingImpl(context, this);
        mappings.add(mapping);
        serializedImage.mappings.add(mapping.serializedImage);
        autoSave();
        return mapping;
    }

    /**
     * @see MappingDocument#addMapping(String, String)
     */
    @Override
    public Mapping addMapping(String from, String to) throws AtlasException {
        boolean origAutoSaves = autoSaves;
        autoSaves = false;
        try {
            MappingImpl mapping = (MappingImpl)addMapping();
            TransformationDescriptor mapDescriptor = null;
            for (TransformationDescriptor descriptor : availableTransformationDescriptors()) {
                if (((TransformationDescriptorImpl)descriptor).transformationClass == MapTransformation.class) {
                    mapDescriptor = descriptor;
                    break;
                }
            }
            Transformation mapTransformation = mapping.addTransformation(mapDescriptor);
            for (Parameter parameter : mapTransformation.parameters()) {
                if (MapTransformation.FROM_PARAMETER.equals(parameter.name())) {
                    parameter.setStringValue(from);
                } else if (MapTransformation.TO_PARAMETER.equals(parameter.name())) {
                    parameter.setStringValue(to);
                }
            }
            return mapping;
        } finally {
            autoSaves = origAutoSaves;
            autoSave();
        }
    }

    /**
     * @see MappingDocument#removeMapping(Mapping)
     */
    @Override
    public void removeMapping(Mapping mapping) {
        if (!mappings.remove(mapping)) {
            throw new AtlasRuntimeException("Mapping does not exist: %s", mapping);
        }
        serializedImage.mappings.remove(((MappingImpl)mapping).serializedImage);
        autoSave();
    }

    /**
     * @see MappingDocument#mappings()
     */
    @Override
    public List<Mapping> mappings() {
        return Collections.unmodifiableList(mappings);
    }

    /**
     * @see MappingDocument#availableTransformationDescriptors()
     */
    @Override
    public Set<TransformationDescriptor> availableTransformationDescriptors() {
        return Collections.unmodifiableSet(new TreeSet<>(context.transformationDescriptors));
    }

    /**
     * @see MappingDocument#autoSaves()
     */
    @Override
    public boolean autoSaves() {
        return autoSaves;
    }

    /**
     * @see MappingDocument#setAutoSaves(boolean)
     */
    @Override
    public MappingDocument setAutoSaves(boolean autoSaves) {
        this.autoSaves = autoSaves;
        return this;
    }

    /**
     * @see MappingDocument#save()
     */
    @Override
    public void save() {
        try {
            JSON.writeValue(context.mappingFile, serializedImage);
        } catch (IOException e) {
            throw new AtlasRuntimeException(e, "Unable to save mapping file %s", context.mappingFile);
        }
    }

    void autoSave() {
        if (autoSaves) {
            save();
        }
    }

    private DataHandler handler(DocumentRole role, String dataFormat) {
        for (Class<? extends DataHandler> handlerClass : context.dataHandlerClasses) {
            try {
                DataHandler handler = handlerClass.getDeclaredConstructor().newInstance();
                for (String supportedFormat : handler.supportedDataFormats()) {
                    if (supportedFormat.equals(dataFormat)) {
                        DocumentRole[] roles = handler.supportedRoles();
                        // If no roles are specified, then it is assumed all are supported
                        if (roles == null || roles.length == 0) {
                            return handler;
                        }
                        for (DocumentRole supportedRoll : roles) {
                            if (supportedRoll == role) {
                                return handler;
                            }
                        }
                    }
                }
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new AtlasRuntimeException(e, "Unable to create data handler %s", handlerClass);
            }
        }
        return null;
    }

    private void load() throws AtlasException {
        boolean origAutoSaves = autoSaves;
        autoSaves = false;
        try {
            SerializedImage image = JSON.readValue(context.mappingFile, SerializedImage.class);
            if (!VERSION.equals(image.version)) {
                throw new AtlasRuntimeException("Version %s is not supported for mapping file %s", image.version, context.mappingFile);
            }
            for (DataDocumentDescriptor.SerializedImage descriptorImage : image.dataDocumentDescriptors) {
                addDataDocument(descriptorImage.id, descriptorImage.role, descriptorImage.dataFormat);
            }
            for (MappingImpl.SerializedImage mappingImage : image.mappings) {
                Mapping mapping = addMapping();
                mapping.setName(mappingImage.name);
                mapping.setDescription(mappingImage.description);
                for (BaseTransformation.SerializedImage transformationImage : mappingImage.transformations) {
                    TransformationDescriptor transformationDescriptor = null;
                    for (TransformationDescriptor descriptor : availableTransformationDescriptors()) {
                        TransformationDescriptorImpl availableDescriptor = (TransformationDescriptorImpl)descriptor;
                        if (availableDescriptor.transformationClass.getName().equals(transformationImage.className)) {
                            transformationDescriptor = descriptor;
                            break;
                        }
                    }
                    if (transformationDescriptor == null) {
                        throw new AtlasException("Unable to find transformation class %s loading mapping file %s",
                                                 transformationImage.className, context.mappingFile);
                    }
                    BaseTransformation transformation = ((MappingImpl)mapping).addTransformation(transformationDescriptor, mapping.transformations().size());
                    for (BaseParameter.SerializedImage parameterImage : transformationImage.parameters) {
                        Parameter parameter = parameter(transformation, parameterImage);
                        if (parameter == null) {
                            throw new AtlasException("Unable to find parameter %s in transformation class %s loading mapping file %s",
                                                     parameterImage.name, transformationImage.className, context.mappingFile);
                        }
                        parameter.setStringValue(parameterImage.stringValue);
                    }
                }
            }
        } catch (IOException e) {
            throw new AtlasException(e, "Unable to load mapping file %s", context.mappingFile);
        } finally {
            autoSaves = origAutoSaves;
        }
    }

    private Parameter parameter(Transformation transformation, BaseParameter.SerializedImage parameterImage) throws AtlasException {
        List<Parameter> parameters = transformation.parameters();
        for (int ndx = 0; ndx < parameters.size(); ++ndx) {
            Parameter parameter = parameters.get(ndx);
            if (parameter.name().equals(parameterImage.name)) {
                while (parameter.stringValue() != null
                       && ndx + 1 < parameters.size() && parameters.get(ndx + 1).name().equals(parameterImage.name)) {
                    parameter = parameters.get(++ndx);
                }
                if (parameter.stringValue() != null) {
                    if (parameterImage.cloned == Boolean.TRUE) {
                        throw new AtlasException("Unable to find additional parameter %s"
                                                 + " in transformation class %s loading mapping file %s",
                                                 parameterImage.name, transformation.getClass().getName(), context.mappingFile);
                    }
                    if (!parameter.cloneable()) {
                        throw new AtlasException("Unable to clone parameter %s in transformation class %s loading mapping file %s",
                                                 parameterImage.name, transformation.getClass().getName(), context.mappingFile);
                    }
                    return transformation.cloneParameter(parameter);
                }
                return parameter;
            }
        }
        return null;
    }

    private DataDocumentDescriptor addDataDocument(String id, DocumentRole role, String dataFormat) {
        DataHandler handler = handler(role, dataFormat);
        if (handler == null) {
            throw new AtlasRuntimeException("The %s data format is not supported for a %s document",
                                            dataFormat, role.toString().toLowerCase());
        }
        DataDocumentDescriptor descriptor = new DataDocumentDescriptor(id, role, dataFormat, handler, null);
        context.dataDocumentDescriptors.remove(descriptor);
        context.dataDocumentDescriptors.add(descriptor);
        serializedImage.dataDocumentDescriptors.add(descriptor.serializedImage);
        return descriptor;
    }

    static class SerializedImage {

        String version = VERSION;
        Set<DataDocumentDescriptor.SerializedImage> dataDocumentDescriptors = new HashSet<>();
        List<MappingImpl.SerializedImage> mappings = new ArrayList<>();
    }
}
