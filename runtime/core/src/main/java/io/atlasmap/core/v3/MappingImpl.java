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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;

import io.atlasmap.api.v3.Mapping;
import io.atlasmap.api.v3.Message.Scope;
import io.atlasmap.api.v3.Message.Status;
import io.atlasmap.api.v3.Parameter;
import io.atlasmap.api.v3.Parameter.Role;
import io.atlasmap.api.v3.Transformation;
import io.atlasmap.api.v3.Transformation.Descriptor;
import io.atlasmap.spi.v3.BaseParameter;
import io.atlasmap.spi.v3.BaseTransformation;
import io.atlasmap.spi.v3.DataHandler;
import io.atlasmap.spi.v3.MappingSupport;
import io.atlasmap.spi.v3.util.AtlasException;
import io.atlasmap.spi.v3.util.AtlasRuntimeException;
import io.atlasmap.spi.v3.util.I18n;
import io.atlasmap.spi.v3.util.VerifyArgument;

public class MappingImpl implements Mapping {

    static final String NAME = I18n.localize("Mapping");

    final List<Transformation> transformations = new ArrayList<>();
    final NavigableMap<String, BaseParameter> parametersByOutputName = new TreeMap<>();
    final SerializedImage serializedImage = new SerializedImage();
    private final Context context;
    private String name = NAME;
    private String description;
    // TODO Verify only one transformation has target parameters that reference target fields
    private Transformation targetFieldTransformation = null;

    MappingImpl(Context context) {
        this.context = context;
        serializedImage.name = name;
        validate();
    }

    /**
     * @see Mapping#name()
     */
    @Override
    public String name() {
        if (name == null) {
            name = NAME;
        }
        return name;
    }

    /**
     * @see Mapping#setName(String)
     */
    @Override
    public void setName(String name) {
        this.name = name == null || name.trim().isEmpty() ? NAME : I18n.localize(name.trim());
        serializedImage.name = this.name;
        context.mappingDocument.autoSaveOrSetUnsaved();
    }

    /**
     * @see Mapping#description()
     */
    @Override
    public String description() {
        return description;
    }

    /**
     * @see Mapping#setDescription(String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description == null || description.trim().isEmpty() ? null : I18n.localize(description);
        serializedImage.description = this.description;
        context.mappingDocument.autoSaveOrSetUnsaved();
    }

    /**
     * @see Mapping#addTransformation(Descriptor)
     */
    @Override
    public Transformation addTransformation(Descriptor descriptor) {
        VerifyArgument.isNotNull("descriptor", descriptor);
        BaseTransformation transformation = addTransformation(descriptor, 0);
        if (transformations.size() > 1) {
            boolean origAutoSaves = context.mappingDocument.autoSaves();
            context.mappingDocument.setAutoSaves(false);
            try {
                int outputName = 1;
                for (Parameter parameter : transformation.parameters()) {
                    if (parameter.role() == Role.OUTPUT) {
                        for (Entry<String, BaseParameter> entry : parametersByOutputName.entrySet()) {
                            try {
                                if (Integer.parseInt(entry.getKey()) == outputName) {
                                    outputName++;
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                        try {
                            parameter.setStringValue(BaseParameter.PROPERTY_REFERENCE_INDICATOR + String.valueOf(outputName));
                        } catch (AtlasException e) {
                            throw new AtlasRuntimeException(e, "Unable to set output property %s for %s parameter in %s transformation",
                                                            outputName, parameter.name(), transformation.name());
                        }
                        outputName++;
                    }
                }
            } finally {
                context.mappingDocument.setAutoSaves(origAutoSaves);
            }
        }
        context.mappingDocument.autoSaveOrSetUnsaved();
        validate();
        return transformation;
    }

    /**
     * @see Mapping#removeTransformation(Transformation)
     */
    @Override
    public void removeTransformation(Transformation transformation) {
        transformations.remove(transformation);
        serializedImage.transformations.remove(((BaseTransformation)transformation).serializedImage());
        // TODO remove output names and references to those (ask how to handle dependencies; add answer type as parameter to remove)
        context.mappingDocument.autoSaveOrSetUnsaved();
        validate();
    }

    /**
     * @see Mapping#moveTransformationBefore(Transformation, Transformation)
     */
    @Override
    public void moveTransformationBefore(Transformation transformation, Transformation beforeTransformation) {
        // TODO implement
        context.mappingDocument.autoSaveOrSetUnsaved();
    }

    /**
     * @see Mapping#replaceTransformation(Transformation, Transformation)
     */
    @Override
    public void replaceTransformation(Transformation transformation, Transformation withTransformation) {
        // TODO implement
        context.mappingDocument.autoSaveOrSetUnsaved();
        validate();
    }

    /**
     * @see Mapping#transformations()
     */
    @Override
    public List<Transformation> transformations() {
        return Collections.unmodifiableList(transformations);
    }

    /**
     * @see Mapping#properties()
     */
    @Override
    public NavigableSet<String> properties() {
        return Collections.unmodifiableNavigableSet(parametersByOutputName.navigableKeySet());
    }

    BaseTransformation addTransformation(Descriptor descriptor, int index) {
        Class<? extends BaseTransformation> transformationClass = ((TransformationDescriptorImpl)descriptor).transformationClass;
        try {
            BaseTransformation transformation = transformationClass.newInstance();
            transformation.setSupport(new Support());
            transformations.add(index, transformation);
            serializedImage.transformations.add(index, transformation.serializedImage());
            validate();
            return transformation;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new AtlasRuntimeException(e, "Unable to create transformation %s", transformationClass);
        }
    }

    private void validate() {
        context.messages.removeIf(message -> message.scope() == Scope.MAPPING);
        if (transformations.isEmpty()) {
            context.messages.add(new MessageImpl(Status.ERROR, Scope.MAPPING, this, "No transformations have been added to this mapping"));
        }
    }

    static class SerializedImage {
        String name;
        String description;
        final List<BaseTransformation.SerializedImage> transformations = new ArrayList<>();
    }

    private class Support implements MappingSupport {

        @Override
        public void autoSave() {
            context.mappingDocument.autoSaveOrSetUnsaved();
        }

        @Override
        public DataHandler handler(String id) {
            VerifyArgument.isNotEmpty("id", id);
            for (DataDocumentDescriptor descriptor : context.dataDocumentDescriptors) {
                if (descriptor.id().equals(id)) {
                    return descriptor.handler;
                }
            }
            return null;
        }

        /**
         * @see MappingSupport#value(String)
         */
        @Override
        public Object value(String propertyName) {
            VerifyArgument.isNotEmpty("propertyName", propertyName);
            return parametersByOutputName.get(propertyName).value();
        }

        /**
         * @see MappingSupport#parameterWithOutputName(String)
         */
        @Override
        public BaseParameter parameterWithOutputName(String outputName) {
            VerifyArgument.isNotEmpty("outputName", outputName);
            return parametersByOutputName.get(outputName);
        }

        /**
         * @see MappingSupport#setOutputProperty(String, BaseParameter)
         */
        @Override
        public void setOutputProperty(String outputName, BaseParameter parameter) {
            VerifyArgument.isNotEmpty("outputName", outputName);
            VerifyArgument.isNotNull("parameter", parameter);
            // TODO handle replacing output name
            parametersByOutputName.put(outputName, parameter);
        }

        /**
         * @see MappingSupport#clearExecutionMessages(Object)
         */
        @Override
        public void clearExecutionMessages(Object context) {
            VerifyArgument.isNotNull("context", context);
            MappingImpl.this.context.messages.removeIf(message -> message.scope() == Scope.EXECUTION && message.context() == context);
        }

        /**
         * @see MappingSupport#addMessage(Status, Scope, Object, String, Object[])
         */
        @Override
        public void addMessage(Status status, Scope scope, Object context, String message, Object... arguments) {
            MappingImpl.this.context.messages.add(new MessageImpl(status, scope, context, message, arguments));
        }
    }
}
