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

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;
import com.fasterxml.jackson.module.jsonSchema.factories.WrapperFactory;
import com.fasterxml.jackson.module.jsonSchema.types.SimpleTypeSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;

public class AtlasSchemaFactoryWrapper extends SchemaFactoryWrapper {

    public AtlasSchemaFactoryWrapper() {
        super(new AtlasSchemaFactoryWrapper.AtlasWrapperFactory());
        schemaProvider = new JsonSchemaFactory() {

            @JsonProperty
            protected FieldType atlasFieldType;

            @SuppressWarnings("unused")
            public FieldType getAtlasFieldType() {
                return atlasFieldType;
            }

            @SuppressWarnings("unused")
            public void setAtlasFieldType(FieldType atlasFieldType) {
                this.atlasFieldType = atlasFieldType;
            }

            @Override
            public StringSchema stringSchema() {
                return new ExtendedStringSchema();
            }
        };
    }

    private static class AtlasWrapperFactory extends WrapperFactory {
        private AtlasWrapperFactory() {
        }

        public SchemaFactoryWrapper getWrapper(SerializerProvider p) {
            SchemaFactoryWrapper wrapper = new AtlasSchemaFactoryWrapper();
            if (p != null) {
                wrapper.setProvider(p);
            }

            return wrapper;
        }

        public SchemaFactoryWrapper getWrapper(SerializerProvider p, VisitorContext rvc) {
            SchemaFactoryWrapper wrapper = new AtlasSchemaFactoryWrapper();
            if (p != null) {
                wrapper.setProvider(p);
            }
            wrapper.setVisitorContext(rvc);
            return wrapper;
        }
    }

    interface ExtendedJsonSchema {
        @JsonAnyGetter HashMap<String, Object> getMetadata();

        @JsonAnySetter
        default void setMetadata(String name, Object value) {
            getMetadata().put(name, value);
        }
    }

    private static class ExtendedStringSchema extends StringSchema implements ExtendedJsonSchema {
        HashMap<String, Object> metadata = new HashMap<>();
        @JsonAnyGetter
        @Override
        public HashMap<String, Object> getMetadata() {
            return metadata;
        }

        @Override
        public void enrichWithBeanProperty(BeanProperty beanProperty) {
            enrichMetadata(this, beanProperty, metadata);
            super.enrichWithBeanProperty(beanProperty);
        }

    }

    private static void enrichMetadata(SimpleTypeSchema schema, BeanProperty property, HashMap<String, Object> metadata) {
        AtlasActionProperty atlasField = property.getAnnotation(AtlasActionProperty.class);
        if (atlasField != null) {
            schema.setTitle(atlasField.title());
            metadata.put("atlas-field-type", atlasField.type());
            metadata.put("atlas-collection-type", atlasField.collectionType());
        }
    }
}
