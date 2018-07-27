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
package io.atlasmap.spi.v3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.atlasmap.api.v3.Parameter;
import io.atlasmap.api.v3.ParameterRole;
import io.atlasmap.api.v3.Transformation;
import io.atlasmap.spi.v3.BaseParameter.StringValueType;
import io.atlasmap.spi.v3.util.AtlasException;
import io.atlasmap.spi.v3.util.AtlasRuntimeException;
import io.atlasmap.spi.v3.util.I18n;
import io.atlasmap.spi.v3.util.VerifyArgument;

/**
 *
 */
public abstract class BaseTransformation implements Transformation {

    private final String name;
    private final String description;
    private final List<Parameter> parameters = new ArrayList<>();
    private MappingSupport support;
    private boolean complete;
    private final Set<BaseParameter> dependencies = new HashSet<>();
    private final Set<BaseParameter> dependents = new HashSet<>();
    private final SerializedImage serializedImage = new SerializedImage();

    /**
     * Note: {@link I18n} is automatically performed on name.
     *
     * @param name The name of this transformation
     * @param description A description of this transformation
     */
    public BaseTransformation(String name, String description) {
        VerifyArgument.isNotEmpty("name", name);
        VerifyArgument.isNotEmpty("description", description);
        this.name = I18n.localize(name);
        this.description = I18n.localize(description);
        serializedImage.className = getClass().getName();
    }

    /**
     * @see Transformation#name()
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * @see Transformation#description()
     */
    @Override
    public String description() {
        return description;
    }

    /**
     * @see Transformation#parameters()
     */
    @Override
    public List<Parameter> parameters() {
        return Collections.unmodifiableList(parameters);
    }

    /**
     * @see io.atlasmap.api.v3.Transformation#parameter(java.lang.String)
     */
    @Override
    public Parameter parameter(String name) {
        VerifyArgument.isNotEmpty("name", name);
        Parameter matchingParameter = null;
        for (Parameter parameter : parameters) {
            if (name.equals(parameter.name())) {
                if (matchingParameter == null) {
                    matchingParameter = parameter;
                } else {
                    return null;
                }
            }
        }
        return matchingParameter;
    }

    /**
     * @see Transformation#cloneParameter(Parameter)
     */
    @Override
    public Parameter cloneParameter(Parameter parameter) {
        if (!parameter.cloneable()) {
            throw new AtlasRuntimeException("The %s parameter is not cloneable", parameter.name());
        }
        BaseParameter clone = ((BaseParameter)parameter).cloneParameter(this);
        if (clone.valueRequired()) {
            throw new AtlasRuntimeException("Cloned parameters can not have required values");
        }
        if (!clone.cloned()) {
            throw new AtlasRuntimeException("Cloned parameters must be marked as cloned");
        }
        int ndx = parameters.indexOf(parameter) + 1;
        parameters.add(ndx, clone);
        serializedImage.parameters.add(ndx, clone.serializedImage);
        return clone;
    }

    /**
     * @see Transformation#complete()
     */
    @Override
    public boolean complete() {
        return complete;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(name);
        for (Parameter parameter : parameters) {
            builder.append(' ');
            builder.append(parameter.name().toLowerCase());
            builder.append(' ');
            builder.append(parameter.stringValue());
        }
        return builder.toString();
    }

    public void setSupport(MappingSupport support) {
        this.support = support;
    }

    /**
     * For internal use only; may not be accessed by subclasses.
     * @return This transformation's serialized image
     */
    public SerializedImage serializedImage() {
        if (!Thread.currentThread().getStackTrace()[1].getClassName().startsWith("io.atlasmap.")) {
            throw new IllegalAccessError();
        }
        return serializedImage;
    }

    protected BaseParameter addParameter(BaseParameter parameter) {
        parameters.add(parameter);
        serializedImage.parameters.add(parameter.serializedImage);
        return parameter;
    }

    protected abstract void execute() throws AtlasException;

    DataHandler handler(String id) {
        return support.handler(id);
    }

    void executeIfComplete() throws AtlasException {
        complete = true;
        for (Parameter parameter : parameters) {
            if (parameter.valueRequired() && parameter.stringValue() == null) {
                complete = false;
                support.autoSave();
                return;
            }
        }
        execute();
        for (Parameter parameter : parameters) {
            if (parameter.role() == ParameterRole.OUTPUT) {
                BaseParameter targetParameter = (BaseParameter)parameter;
                if (targetParameter.dataHandler() != null) {
                    targetParameter.dataHandler().setValue(targetParameter.path(), targetParameter.value());
                } else if (targetParameter.stringValueType() == StringValueType.PROPERTY_REFERENCE) {
                    for (BaseParameter dependentParameter : dependents) {
                        if (dependentParameter.role() == ParameterRole.INPUT
                            && dependentParameter.stringValueType() == StringValueType.PROPERTY_REFERENCE
                            && dependentParameter.stringValue().equals(parameter.stringValue())) {
                            dependentParameter.setValue(parameter.value());
                            dependentParameter.transformation.executeIfComplete();
                        }
                    }
                }
            }
        }
        support.autoSave();
    }

    Object result(String propertyName) {
        return support.value(propertyName);
    }

    void addDependency(String propertyName, BaseParameter dependentParameter) {
        BaseParameter parameter = support.parameterWithOutputName(propertyName);
        dependencies.add(parameter);
        parameter.transformation.dependents.add(dependentParameter);
    }

    void setOutputProperty(String outputName, BaseParameter parameter) {
        support.setOutputProperty(outputName, parameter);
    }

    public static class SerializedImage {
        public String className;
        public final List<BaseParameter.SerializedImage> parameters = new ArrayList<>();
    }
}
