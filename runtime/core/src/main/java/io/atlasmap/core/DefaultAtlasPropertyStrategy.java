/**
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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasUnsupportedException;
import io.atlasmap.spi.AtlasPropertyStrategy;
import io.atlasmap.spi.AtlasPropertyType;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Property;
import io.atlasmap.v2.PropertyField;

public class DefaultAtlasPropertyStrategy implements AtlasPropertyStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultAtlasPropertyStrategy.class);

    private boolean environmentPropertiesEnabled = true;
    private boolean systemPropertiesEnabled = true;
    private boolean mappingDefinedPropertiesEnabled = true;
    private boolean runtimePropertiesEnabled = true;

    private List<AtlasPropertyType> propertyOrder = Arrays.asList(
            AtlasPropertyType.RUNTIME_PROPERTIES,
            AtlasPropertyType.JAVA_SYSTEM_PROPERTIES,
            AtlasPropertyType.ENVIRONMENT_VARIABLES,
            AtlasPropertyType.MAPPING_DEFINED_PROPERTIES);

    private AtlasConversionService atlasConversionService = null;

    @Override
    public void processPropertyField(AtlasMapping atlasMapping, PropertyField propertyField,
            Map<String, Object> runtimeProperties) throws AtlasUnsupportedException, AtlasConversionException {
        if (propertyField == null || propertyField.getName() == null || propertyField.getName().trim().length() == 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Null or empty PropertyField specified popertyField=%s",
                        AtlasModelFactory.toString(propertyField)));
            }
            return;
        }

        for (AtlasPropertyType propType : getPropertyOrder()) {
            switch (propType) {
            case RUNTIME_PROPERTIES:
                if (processRuntimeProperties(propertyField, runtimeProperties)) {
                    return;
                }
                break;
            case JAVA_SYSTEM_PROPERTIES:
                if (processJavaSystemProperty(propertyField)) {
                    return;
                }
                break;
            case ENVIRONMENT_VARIABLES:
                if (processEnvironmentVariable(propertyField)) {
                    return;
                }
                break;
            case MAPPING_DEFINED_PROPERTIES:
                if (processMappingDefinedProperties(propertyField, atlasMapping)) {
                    return;
                }
                break;
            default:
                throw new AtlasUnsupportedException(
                        String.format("Unsupported PropertyType detected type=%s for field=%s", propType,
                                AtlasModelFactory.toString(propertyField)));
            }
        }
    }

    protected boolean processEnvironmentVariable(PropertyField propertyField)
        throws AtlasConversionException {

        if (!isEnvironmentPropertiesEnabled()) {
            return false;
        }

        try {
            if (System.getenv(propertyField.getName()) != null) {
                Object propertyValue = System.getenv(propertyField.getName());
                if (getAtlasConversionService() != null && propertyField.getFieldType() != null) {
                    propertyField.setValue(getAtlasConversionService().convertType(propertyValue,
                            null, atlasConversionService.classFromFieldType(propertyField.getFieldType()), null));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format(
                                "Assigned environment variable property for property field name=%s value=%s",
                                propertyField.getName(), propertyField.getValue()));
                    }
                } else {
                    propertyField.setValue(propertyValue);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format(
                                "Assigned environment variable for property field name=%s value=%s",
                                propertyField.getName(), propertyField.getValue()));
                    }
                }
                return true;
            }
        } catch (SecurityException e) {
            LOG.error(String.format("SecurityException while processing environment variable for propertyField=%s",
                    AtlasModelFactory.toString(propertyField)), e);
        }
        return false;
    }

    protected boolean processJavaSystemProperty(PropertyField propertyField)
        throws AtlasConversionException {

        if (!isSystemPropertiesEnabled()) {
            return false;
        }

        try {
            if (System.getProperty(propertyField.getName()) != null) {
                Object propertyValue = System.getProperty(propertyField.getName());
                if (getAtlasConversionService() != null && propertyField.getFieldType() != null) {
                    propertyField.setValue(getAtlasConversionService().convertType(propertyValue,
                            null, atlasConversionService.classFromFieldType(propertyField.getFieldType()), null));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format(
                                "Assigned Java system property for property field name=%s value=%s",
                                propertyField.getName(), propertyField.getValue()));
                    }
                } else {
                    propertyField.setValue(propertyValue);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format(
                                "Assigned Java system property for property field name=%s value=%s",
                                propertyField.getName(), propertyField.getValue()));
                    }
                }
                return true;
            }
        } catch (SecurityException e) {
            LOG.error(String.format("SecurityException while processing Java system property for propertyField=%s",
                    AtlasModelFactory.toString(propertyField)), e);
        }
        return false;
    }

    protected boolean processMappingDefinedProperties(PropertyField propertyField, AtlasMapping atlasMapping)
            throws AtlasConversionException {

        if (!isMappingDefinedPropertiesEnabled()) {
            return false;
        }

        if (atlasMapping == null || atlasMapping.getProperties() == null
                || atlasMapping.getProperties().getProperty() == null
                || atlasMapping.getProperties().getProperty().isEmpty()) {
            return false;
        }

        for (Property prop : atlasMapping.getProperties().getProperty()) {
            if (propertyField.getName().equals(prop.getName())) {
                if (getAtlasConversionService() != null
                        && (propertyField.getFieldType() != null || prop.getFieldType() != null)) {
                    propertyField.setValue(getAtlasConversionService().convertType(prop.getValue(), FieldType.STRING,
                            (propertyField.getFieldType() != null ? propertyField.getFieldType()
                                    : prop.getFieldType())));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                                String.format("Assigned Mapping defined property for property field name=%s value=%s",
                                        propertyField.getName(), propertyField.getValue()));
                    }
                } else {
                    propertyField.setValue(prop.getValue());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                                String.format("Assigned Mapping defined property for property field name=%s value=%s",
                                        propertyField.getName(), propertyField.getValue()));
                    }
                }
                return true;
            }
        }
        return false;
    }

    protected boolean processRuntimeProperties(PropertyField propertyField, Map<String, Object> runtimeProperties)
            throws AtlasConversionException {

        if (!isRuntimePropertiesEnabled() || runtimeProperties == null || runtimeProperties.isEmpty()) {
            return false;
        }

        String key = propertyField.getName();
        if (key == null || key.isEmpty() || !runtimeProperties.containsKey(key)) {
            return false;
        }

        if (getAtlasConversionService() != null && propertyField.getFieldType() != null) {
            propertyField.setValue(getAtlasConversionService().convertType(runtimeProperties.get(key),
                    null, atlasConversionService.classFromFieldType(propertyField.getFieldType()), null));
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format(
                        "Assigned Runtime defined property for property field name=%s value=%s",
                        propertyField.getName(), propertyField.getValue()));
            }
        } else {
            propertyField.setValue(runtimeProperties.get(key));
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format(
                        "Assigned Runtime defined property for property field name=%s value=%s",
                        propertyField.getName(), propertyField.getValue()));
            }
        }
        return true;
    }

    public void setPropertyOrderValue(List<String> propertyValues) {
        List<AtlasPropertyType> tmp = null;

        for (String v : propertyValues) {
            if (tmp == null) {
                tmp = new LinkedList<AtlasPropertyType>();
            }

            try {
                tmp.add(AtlasPropertyType.fromValue(v));
            } catch (IllegalArgumentException e) {
                LOG.error(String.format("Invalid AtlasPropertyType specified '%s'", v));
            }
        }

        propertyOrder = null;
        propertyOrder = tmp;
    }

    public boolean isEnvironmentPropertiesEnabled() {
        return environmentPropertiesEnabled;
    }

    public void setEnvironmentPropertiesEnabled(boolean environmentPropertiesEnabled) {
        this.environmentPropertiesEnabled = environmentPropertiesEnabled;
    }

    public boolean isSystemPropertiesEnabled() {
        return systemPropertiesEnabled;
    }

    public void setSystemPropertiesEnabled(boolean systemPropertiesEnabled) {
        this.systemPropertiesEnabled = systemPropertiesEnabled;
    }

    public boolean isMappingDefinedPropertiesEnabled() {
        return mappingDefinedPropertiesEnabled;
    }

    public void setMappingDefinedPropertiesEnabled(boolean mappingDefinedPropertiesEnabled) {
        this.mappingDefinedPropertiesEnabled = mappingDefinedPropertiesEnabled;
    }

    public boolean isRuntimePropertiesEnabled() {
        return runtimePropertiesEnabled;
    }

    public void setRuntimePropertiesEnabled(boolean runtimePropertiesEnabled) {
        this.runtimePropertiesEnabled = runtimePropertiesEnabled;
    }

    public List<AtlasPropertyType> getPropertyOrder() {
        return propertyOrder;
    }

    public void setPropertyOrder(List<AtlasPropertyType> propertyOrder) {
        this.propertyOrder = propertyOrder;
    }

    public AtlasConversionService getAtlasConversionService() {
        return atlasConversionService;
    }

    public void setAtlasConversionService(AtlasConversionService atlasConversionService) {
        this.atlasConversionService = atlasConversionService;
    }

}
