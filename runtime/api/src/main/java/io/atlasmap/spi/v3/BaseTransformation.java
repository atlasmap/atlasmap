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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.atlasmap.api.v3.Mapping;
import io.atlasmap.api.v3.Message;
import io.atlasmap.api.v3.Message.Scope;
import io.atlasmap.api.v3.Message.Status;
import io.atlasmap.api.v3.Parameter;
import io.atlasmap.api.v3.Parameter.Role;
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

    private static final String DIGITS = "(\\p{Digit}+)";
    private static final String BINARY_DIGITS = "([01_]+)";
    private static final String HEX_DIGITS = "(\\p{XDigit}+)";
    private static final String EXP = "[eE][+-]?"+DIGITS;
    protected static final String NUMBER_REGEX =
         "([+-]?(" +
         "((("+DIGITS+"(\\.)?("+DIGITS+"?)("+EXP+")?)|" +
         "(\\.("+DIGITS+")("+EXP+")?)|" +
         "((" +
         "(0[xX]" + HEX_DIGITS + "(\\.)?)|" +
         "(0[xX]" + HEX_DIGITS + "?(\\.)" + HEX_DIGITS + ")" +
         ")[pP][+-]?" + DIGITS + "))|" +
         "(0[bB]" + BINARY_DIGITS + ")|" +
         "[fFdD]?)))";

    TransformationSupport support;
    private final String name;
    private final String description;
    private final List<Parameter> parameters = new ArrayList<>();
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
        verifyParameterInTransformation("parameter", parameter);
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
        validate();
        support.autoSave();
        return clone;
    }

    /**
     * @see io.atlasmap.api.v3.Transformation#removeClonedParameter(io.atlasmap.api.v3.Parameter)
     */
    @Override
    public void removeClonedParameter(Parameter parameter) throws AtlasException {
        verifyParameterInTransformation("parameter", parameter);
        if (!parameter.cloned()) {
            throw new AtlasException("The %s parameter is not a clone", parameter.name());
        }
        removeParameter(parameter);
        validate();
        support.autoSave();
    }

    /**
     * @see Transformation#mapping()
     */
    @Override
    public Mapping mapping() {
        return support.mapping();
    }

    /**
     * @see Transformation#messages()
     */
    @Override
    public Set<Message> messages() {
        Set<Message> messages = support.documentMessages().stream().filter(message -> message.scope() == Scope.TRANSFORMATION
                                                                                      && message.context() == this)
                                                                   .collect(Collectors.toSet());
        for (Parameter parameter : parameters) {
            messages.addAll(parameter.messages());
        }
        return Collections.unmodifiableSet(messages);
    }

    /**
     * @see Transformation#hasErrors()
     */
    @Override
    public boolean hasErrors() {
        return messages().stream().anyMatch(message -> message.status() == Status.ERROR);
    }

    /**
     * @see Transformation#hasWarnings()
     */
    @Override
    public boolean hasWarnings() {
        return messages().stream().anyMatch(message -> message.status() == Status.WARNING);
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

    /**
     * <strong>Warning:</strong> Must never be called by subclasses
     *
     * @param support
     */
    public void setSupport(TransformationSupport support) {
        this.support = support;
        parameters.forEach(parameter -> ((BaseParameter)parameter).validate());
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

    public void removeParameter(Parameter parameter) {
        try {
            parameter.setStringValue(null);
        } catch (AtlasException e) {
            // This should never occur
            throw new AtlasRuntimeException(e, "Unable to remove the %s parameter", parameter.name());
        }
        parameters.remove(parameter);
        serializedImage.parameters.remove(((BaseParameter)parameter).serializedImage);
    }

    protected BaseParameter addParameter(BaseParameter parameter) {
        parameters.add(parameter);
        serializedImage.parameters.add(parameter.serializedImage);
        return parameter;
    }

    protected abstract void execute() throws AtlasException;

    protected void addMessage(Status status, Object context, String message, Object... arguments) {
        VerifyArgument.isNotNull("status", status);
        VerifyArgument.isNotNull("context", context);
        VerifyArgument.isNotEmpty("message", message);
        support.addMessage(status, Scope.TRANSFORMATION, context, message, arguments);
    }

    void executeIfComplete() throws AtlasException {
        if (hasErrors()) {
            parameters.stream()
                      .filter(parameter -> parameter.role() == Role.OUTPUT)
                      .forEach(parameter -> ((BaseParameter)parameter).setOutputValue(null));
        } else {
            support.clearMessages(Scope.TRANSFORMATION, this);
            for (Parameter parameter : parameters) {
                support.clearMessages(Scope.TRANSFORMATION, parameter);
            }
            execute();
            for (Parameter parameter : parameters) {
                if (parameter.role() == Role.OUTPUT) {
                    BaseParameter outputParameter = (BaseParameter)parameter;
                    if (outputParameter.dataHandler() != null) {
                        outputParameter.dataHandler().clearMessages(outputParameter);
                        outputParameter.dataHandler().setValue(outputParameter.path(), outputParameter.value(), outputParameter);
                    } else if (outputParameter.stringValueType() == StringValueType.PROPERTY_REFERENCE) {
                        Set<Parameter> dependentParameters
                            = mapping().dependentParametersByOutputProperty().get(outputParameter.stringValue());
                        for (Parameter dependentParameter : dependentParameters) {
                            ((BaseParameter)dependentParameter).setValue(parameter.value());
                            ((BaseTransformation)dependentParameter.transformation()).executeIfComplete();
                        }
                    }
                }
            }
        }
        support.autoSave();
    }

    void validate() {
        support.clearMessages(Scope.TRANSFORMATION, this);
        for (Parameter parameter : parameters) {
            if (parameter.valueRequired() && (parameter.stringValue() == null || parameter.stringValue().isEmpty())) {
                support.addMessage(Status.ERROR, Scope.PARAMETER, parameter,
                                   "A value is required for the %s parameter",
                                   parameter.name());
            }
        }
        support.validate();
    }

    private void verifyParameterInTransformation(String name, Parameter parameter) {
        VerifyArgument.isNotNull(name, parameter);
        if (parameter.transformation() != this) {
            throw new AtlasRuntimeException("The %s transformation does not contain the %s parameter", name, parameter.name());
        }
    }

    public static class SerializedImage {
        public String className;
        public final List<BaseParameter.SerializedImage> parameters = new ArrayList<>();
    }
}
