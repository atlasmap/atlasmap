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

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import io.atlasmap.api.v3.Message;
import io.atlasmap.api.v3.Message.Scope;
import io.atlasmap.api.v3.Message.Status;
import io.atlasmap.api.v3.Parameter;
import io.atlasmap.api.v3.Transformation;
import io.atlasmap.spi.v3.util.AtlasException;
import io.atlasmap.spi.v3.util.AtlasRuntimeException;
import io.atlasmap.spi.v3.util.I18n;
import io.atlasmap.spi.v3.util.VerifyArgument;

/**
 *
 */
public class BaseParameter implements Parameter {

    public static final char FIELD_REFERENCE_INDICATOR = '/';
    public static final char PROPERTY_REFERENCE_INDICATOR = ':';
    public static final char ESCAPE_CHAR = FIELD_REFERENCE_INDICATOR;

    final SerializedImage serializedImage = new SerializedImage();
    private final BaseTransformation transformation;
    private final String name;
    private final Role role;
    private final boolean valueRequired;
    private String stringValue;
    private StringValueType stringValueType;
    private DataHandler handler;
    private String path;
    private Object value;
    private final String description;
    private final boolean cloneable;
    private Boolean cloned;

    /**
     * Note: {@link I18n} is automatically performed on name and description.
     *
     * @param transformation
     * @param name
     * @param role
     * @param valueRequired
     * @param cloneable
     * @param description
     */
    public BaseParameter(BaseTransformation transformation, String name, Role role, boolean valueRequired, boolean cloneable,
                         String description) {
        this(transformation, name, role, valueRequired, cloneable, description, null);
    }

    private BaseParameter(BaseTransformation transformation, String name, Role role, boolean valueRequired, boolean cloneable,
                          String description, Boolean cloned) {
        VerifyArgument.isNotNull("transformation", transformation);
        VerifyArgument.isNotEmpty("name", name);
        VerifyArgument.isNotNull("role", role);
        VerifyArgument.isNotEmpty("description", description);
        this.transformation = transformation;
        this.name = I18n.localize(name);
        this.role = role;
        this.valueRequired = role == Role.OUTPUT || valueRequired;
        this.description = I18n.localize(description);
        this.cloneable = cloneable;
        this.cloned = cloned;
        serializedImage.name = this.name;
        serializedImage.cloned = cloned;
    }

    /**
     * @see Parameter#name()
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * @see Parameter#valueRequired()
     */
    @Override
    public boolean valueRequired() {
        return valueRequired;
    }

    /**
     * @see Parameter#stringValue()
     */
    @Override
    public String stringValue() {
        return stringValue;
    }

    /**
     * @see Parameter#setStringValue(String)
     */
    @Override
    public void setStringValue(String stringValue) throws AtlasException {
        if (!stringValue.equals(this.stringValue)) {
            if (stringValueType == StringValueType.PROPERTY_REFERENCE) {
                if (role == Role.INPUT) {
                    transformation.support.removeReferenceToOutputPropertyReference(this.stringValue, this);
                } else {
                    transformation.support.removeOutputProperty(this.stringValue);
                }
            } else if (stringValueType == StringValueType.FIELD_REFERENCE && role == Role.OUTPUT) {
                handler.clearMessages(this);
                handler.setValue(path, null, this); // TODO set to whatever 'unaltered' value is instead
                path = null;
                value = null;
                transformation.support.removeTargetFieldReference(stringValue, this);
            }
        }
        setStringValueType(stringValue);
        if (stringValueType == StringValueType.CONSTANT) {
            setConstant(stringValue);
        } else if (stringValueType == StringValueType.FIELD_REFERENCE) {
            setFieldReference(stringValue);
        } else { // Property reference
            setPropertyReference(stringValue);
        }
        this.stringValue = stringValue;
        serializedImage.stringValue = stringValue;
        validate();
        transformation.executeIfComplete();
    }

    /**
     * @see Parameter#description()
     */
    @Override
    public String description() {
        return description;
    }

    /**
     * @see Parameter#value()
     */
    @Override
    public Object value() {
        return value;
    }

    /**
     * @see Parameter#role()
     */
    @Override
    public Role role() {
        return role;
    }

    /**
     * @see Parameter#cloneable()
     */
    @Override
    public boolean cloneable() {
        return cloneable;
    }

    /**
     * @see Parameter#cloned()
     */
    @Override
    public boolean cloned() {
        return cloned;
    }

    /**
     * @see Parameter#transformation()
     */
    @Override
    public Transformation transformation() {
        return transformation;
    }

    /**
     * @see Parameter#messages()
     */
    @Override
    public Set<Message> messages() {
        Set<Message> messages = transformation.support.documentMessages().stream()
                                                                         .filter(message -> (message.scope() == Scope.PARAMETER
                                                                                             || message.scope() == Scope.DATA_HANDLER)
                                                                                            && message.context() == this)
                                                                         .collect(Collectors.toSet());
        return Collections.unmodifiableSet(messages);
    }

    /**
     * @see Parameter#hasErrors()
     */
    @Override
    public boolean hasErrors() {
        return messages().stream().anyMatch(message -> message.status() == Status.ERROR);
    }

    /**
     * @see Parameter#hasWarnings()
     */
    @Override
    public boolean hasWarnings() {
        return messages().stream().anyMatch(message -> message.status() == Status.WARNING);
    }

    public DataHandler dataHandler() {
        return handler;
    }

    public void setOutputValue(Object value) {
        if (role != Role.OUTPUT) {
            throw new AtlasRuntimeException("setOutputValue() may only be called on a parameter with role $s" + Role.OUTPUT);
        }
        this.value = value;
    }

    public StringValueType stringValueType() {
        return stringValueType;
    }

    String path() {
        return path;
    }

    BaseParameter cloneParameter(BaseTransformation transformation) {
        return new BaseParameter(transformation, name, role, false, cloneable, description, true);
    }

    void setValue(Object value) {
        this.value = value;
    }

    void validate() {
        transformation.support.clearMessages(Scope.PARAMETER, this);
        if (valueRequired() && (stringValue() == null || stringValue().isEmpty())) {
            transformation.support.addMessage(Status.ERROR, Scope.PARAMETER, this,
                                              "A value is required");
        }
        transformation.validate();
    }

    private void setStringValueType(String stringValue) throws AtlasException {
        stringValueType = StringValueType.CONSTANT;
        if (stringValue != null && !stringValue.trim().isEmpty()) {
            char chr = stringValue.charAt(0);
            if (chr == FIELD_REFERENCE_INDICATOR) {
                stringValueType = StringValueType.FIELD_REFERENCE;
                if (stringValue.length() > 1) {
                    // Determine if char is really an escape char
                    chr = stringValue.charAt(1);
                    if (chr == FIELD_REFERENCE_INDICATOR || chr == PROPERTY_REFERENCE_INDICATOR) {
                        stringValueType = StringValueType.CONSTANT;
                        stringValue = stringValue.substring(1);
                    }
                }
            } else if (chr == PROPERTY_REFERENCE_INDICATOR) {
                stringValueType = StringValueType.PROPERTY_REFERENCE;
            }
        }
        if (stringValueType == StringValueType.CONSTANT && role == Role.OUTPUT) {
            throw new AtlasException("Constant '%s' may not be set on output parameter %s", stringValue, name);
        }
    }

    private void setConstant(String stringValue) {
        value = stringValue;
    }

    private void setFieldReference(String stringValue) throws AtlasException {
        int ndx = stringValue.indexOf('/', 1);
        if (ndx < 2) {
            throw new AtlasException("The field reference must start with a data document ID, followed by a field reference: %s", stringValue);
        }
        String docId = stringValue.substring(1, ndx);
        handler = transformation.support.handler(docId);
        if (handler == null) {
            throw new AtlasException("The field reference contains a reference to an invalid data document: %s in %s", docId, stringValue);
        }
        path = stringValue.substring(ndx + 1);
        handler.clearMessages(this);
        if (role == Role.INPUT) {
            value = handler.value(path);
        } else {
            transformation.support.addTargetFieldReference(stringValue, this);
        }
    }

    private void setPropertyReference(String stringValue) throws AtlasException {
        if (role == Role.INPUT) {
            BaseParameter parameter = transformation.support.parameterWithOutputProperty(stringValue);
            if (parameter == null) {
                throw new AtlasException("No output property exists named %s", stringValue);
            }
            value = parameter.value;
            transformation.support.addReferenceToOutputProperty(stringValue, this);
        } else {
            transformation.support.addOutputProperty(stringValue, this);
        }
    }

    public enum StringValueType {
        CONSTANT,
        PROPERTY_REFERENCE,
        FIELD_REFERENCE
    }

    public static class SerializedImage {
        public String name;
        public String stringValue;
        public Boolean cloned;
    }
}
