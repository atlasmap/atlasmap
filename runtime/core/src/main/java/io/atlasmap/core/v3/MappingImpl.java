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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import io.atlasmap.api.v3.Mapping;
import io.atlasmap.api.v3.Message;
import io.atlasmap.api.v3.Message.Scope;
import io.atlasmap.api.v3.Message.Status;
import io.atlasmap.api.v3.Parameter;
import io.atlasmap.api.v3.Parameter.Role;
import io.atlasmap.api.v3.Transformation;
import io.atlasmap.api.v3.Transformation.Descriptor;
import io.atlasmap.spi.v3.BaseParameter;
import io.atlasmap.spi.v3.BaseParameter.StringValueType;
import io.atlasmap.spi.v3.BaseTransformation;
import io.atlasmap.spi.v3.DataHandler;
import io.atlasmap.spi.v3.TransformationSupport;
import io.atlasmap.spi.v3.util.AtlasException;
import io.atlasmap.spi.v3.util.AtlasRuntimeException;
import io.atlasmap.spi.v3.util.I18n;
import io.atlasmap.spi.v3.util.VerifyArgument;

public class MappingImpl implements Mapping {

    static final String NAME = I18n.localize("Mapping");

    final SerializedImage serializedImage = new SerializedImage();
    private final Context context;
    private String name = NAME;
    private String description;
    private final List<Transformation> transformations = new ArrayList<>();
    // TODO support cross-mapping property refs
    private final NavigableMap<String, BaseParameter> parametersByOutputProperty = new TreeMap<>();
    private final Map<String, Set<Parameter>> dependentParametersByOutputProperty = new HashMap<>();
    private final Support support = new Support();

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
        return createOutputPropertyNames(addTransformation(descriptor, 0));
    }

    /**
     * @see Mapping#removeTransformation(Transformation)
     */
    @Override
    public void removeTransformation(Transformation transformation) {
        verifyTransformationInMapping("transformation", transformation);
        BaseTransformation baseTransformation = (BaseTransformation)transformation;
        for (Parameter parameter : transformation.parameters()) {
            baseTransformation.removeParameter(parameter);
        }
        transformations.remove(transformation);
        serializedImage.transformations.remove(((BaseTransformation)transformation).serializedImage());
        context.mappingDocument.autoSaveOrSetUnsaved();
        validate();
    }

    /**
     * @see Mapping#moveTransformationBefore(Transformation, Transformation)
     */
    @Override
    public void moveTransformationBefore(Transformation movingTransformation, Transformation transformation) {
        verifyTransformationInMapping("movingTransformation", movingTransformation);
        verifyTransformationInMapping("transformation", transformation);
        transformations.set(transformations.indexOf(transformation), movingTransformation);
        context.mappingDocument.autoSaveOrSetUnsaved();
    }

    /**
     * @see Mapping#moveTransformationAfter(Transformation, Transformation)
     */
    @Override
    public void moveTransformationAfter(Transformation movingTransformation, Transformation transformation) {
        verifyTransformationInMapping("movingTransformation", movingTransformation);
        verifyTransformationInMapping("transformation", transformation);
        transformations.set(transformations.indexOf(transformation) + 1, movingTransformation);
        context.mappingDocument.autoSaveOrSetUnsaved();
    }

    /**
     * @see Mapping#replaceTransformation(Transformation, Descriptor)
     */
    @Override
    public Transformation replaceTransformation(Transformation transformation, Descriptor descriptor) {
        int index = transformations.indexOf(transformation);
        return createOutputPropertyNames(addTransformation(descriptor, index));
    }

    /**
     * @see Mapping#transformations()
     */
    @Override
    public List<Transformation> transformations() {
        return Collections.unmodifiableList(transformations);
    }

    /**
     * @see Mapping#outputPropertyNames()
     */
    @Override
    public NavigableSet<String> outputPropertyNames() {
        return Collections.unmodifiableNavigableSet(parametersByOutputProperty.navigableKeySet());
    }

    /**
     * @see io.atlasmap.api.v3.Mapping#dependentParametersByOutputProperty()
     */
    @Override
    public Map<String, Set<Parameter>> dependentParametersByOutputProperty() {
        return Collections.unmodifiableMap(dependentParametersByOutputProperty);
    }

    /**
     * @see Mapping#messages()
     */
    @Override
    public Set<Message> messages() {
        Set<Message> messages = context.messages.stream().filter(message -> message.scope() == Scope.MAPPING
                                                                            && message.context() == this)
                                                         .collect(Collectors.toSet());
        for (Transformation transformation : transformations) {
            messages.addAll(transformation.messages());
        }
        return Collections.unmodifiableSet(messages);
    }

    /**
     * @see Mapping#hasErrors()
     */
    @Override
    public boolean hasErrors() {
        return messages().stream().anyMatch(message -> message.status() == Status.ERROR);
    }

    /**
     * @see Mapping#hasWarnings()
     */
    @Override
    public boolean hasWarnings() {
        return messages().stream().anyMatch(message -> message.status() == Status.WARNING);
    }

    BaseTransformation addTransformation(Descriptor descriptor, int index) {
        Class<? extends BaseTransformation> transformationClass = ((TransformationDescriptorImpl)descriptor).transformationClass;
        try {
            BaseTransformation transformation = transformationClass.newInstance();
            transformation.setSupport(support);
            transformations.add(index, transformation);
            serializedImage.transformations.add(index, transformation.serializedImage());
            validate();
            return transformation;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new AtlasRuntimeException(e, "Unable to create transformation %s", transformationClass);
        }
    }

    private BaseTransformation createOutputPropertyNames(BaseTransformation transformation) {
        boolean origAutoSaves = context.mappingDocument.autoSaves();
        context.mappingDocument.setAutoSaves(false);
        try {
            int outputName = 1;
            for (Parameter parameter : transformation.parameters()) {
                if (parameter.role() == Role.OUTPUT) {
                    for (Entry<String, BaseParameter> entry : parametersByOutputProperty.entrySet()) {
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
        validate();
        context.mappingDocument.autoSaveOrSetUnsaved();
        return transformation;
    }

    private void validate() {
        context.messages.removeIf(message -> message.scope() == Scope.MAPPING && message.context() == this);
        if (transformations.isEmpty()) {
            context.messages.add(new MessageImpl(Status.ERROR, Scope.MAPPING, this,
                                                 "No transformations have been added"));
        }
        // Verify a transformation exists that maps to a target field and that all results are used
        // TODO move to document level once cross-mapping property refs are supported
        boolean fieldRefFound = false;
        Set<String> unusedOutputNames = new HashSet<>(parametersByOutputProperty.keySet());
        for (Transformation transformation : transformations()) {
            for (Parameter parameter : transformation.parameters()) {
                StringValueType type = ((BaseParameter)parameter).stringValueType();
                if (parameter.role() == Role.INPUT) {
                    if (type == StringValueType.PROPERTY_REFERENCE) {
                        // TODO handle indexes
                        unusedOutputNames.remove(parameter.stringValue());
                    }
                } else if (type == StringValueType.FIELD_REFERENCE) {
                    fieldRefFound = true;
                }
            }
        }
        if (!fieldRefFound) {
            context.messages.add(new MessageImpl(Status.ERROR, Scope.MAPPING, this,
                                                 "None of the transformations in this mapping map to a target field"));
        }
        for (String name : unusedOutputNames) {
            Parameter parameter = parametersByOutputProperty.get(name);
            context.messages.add(new MessageImpl(Status.ERROR, Scope.MAPPING, this,
                                                 "Output property %s for the %s parameter of the %s transformation"
                                                 + " is never used in this mapping",
                                                 name, parameter.name(), parameter.transformation().name()));
        }
        context.mappingDocument.validate();
    }

    private void verifyTransformationInMapping(String name, Transformation transformation) {
        VerifyArgument.isNotNull(name, transformation);
        if (transformation.mapping() != this) {
            throw new AtlasRuntimeException("Mapping %s does not contain the %s transformation", name, transformation.name());
        }
    }

    static class SerializedImage {
        String name;
        String description;
        final List<BaseTransformation.SerializedImage> transformations = new ArrayList<>();
    }

    private class Support implements TransformationSupport {

        @Override
        public Mapping mapping() {
            return MappingImpl.this;
        }

        @Override
        public void autoSave() {
            context.mappingDocument.autoSaveOrSetUnsaved();
        }

        @Override
        public DataHandler handler(String id) {
            for (DataDocumentDescriptor descriptor : context.dataDocumentDescriptors) {
                if (descriptor.id().equals(id)) {
                    return descriptor.handler;
                }
            }
            return null;
        }

        @Override
        public BaseParameter parameterWithOutputProperty(String outputProperty) {
            return parametersByOutputProperty.get(outputProperty);
        }

        @Override
        public void addOutputProperty(String outputProperty, BaseParameter parameter) {
            parametersByOutputProperty.put(outputProperty, parameter);
            dependentParametersByOutputProperty.computeIfAbsent(outputProperty, reference -> new HashSet<>());
        }

        @Override
        public void removeOutputProperty(String outputProperty) {
            for (Parameter parameter : dependentParametersByOutputProperty.get(outputProperty)) {
                try {
                    parameter.setStringValue(null);
                } catch (AtlasException e) {
                    // This should never occur
                    throw new AtlasRuntimeException(e, "Unable to remove the %s parameter as having a dependency to the %s property",
                                                    parameter.name(), outputProperty);
                }
            }
            parametersByOutputProperty.remove(outputProperty);
            dependentParametersByOutputProperty.remove(outputProperty);
        }

        @Override
        public void addReferenceToOutputProperty(String outputProperty, BaseParameter parameter) {
            dependentParametersByOutputProperty.get(outputProperty).add(parameter);
        }

        @Override
        public void removeReferenceToOutputPropertyReference(String outputProperty, BaseParameter parameter) {
            dependentParametersByOutputProperty.get(outputProperty).remove(parameter);
        }

        @Override
        public void clearMessages(Scope scope, Object context) {
            MappingImpl.this.context.messages.removeIf(message -> message.scope() == scope && message.context() == context);
        }

        @Override
        public void addMessage(Status status, Scope scope, Object context, String message, Object... arguments) {
            MappingImpl.this.context.messages.add(new MessageImpl(status, scope, context, message, arguments));
        }

        @Override
        public Set<Message> documentMessages() {
            return context.messages;
        }

        @Override
        public void addTargetFieldReference(String targetFieldReference, BaseParameter parameter) throws AtlasException {
            context.mappingDocument.addTargetFieldReference(targetFieldReference, parameter);
        }

        @Override
        public void removeTargetFieldReference(String targetFieldPath, BaseParameter parameter) {
            context.mappingDocument.removeTargetFieldReference(targetFieldPath, parameter);
        }

        @Override
        public void validate() {
            MappingImpl.this.validate();
        }
    }
}
