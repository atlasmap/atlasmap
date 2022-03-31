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
package io.atlasmap.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.DocumentKey;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.Mapping;

/**
 * The API for handling {@link AtlasMapping} which represents the AtlasMap Mapping Definition.
 * TODO Migrate {@link AtlasMapping} handling to this, mostly from {@link DefaultAtlasContext},
 * and possibly from io.atlasmap.service.MappingService
 */
public class AtlasMappingHandler {

    private AtlasMapping mapping;

    /**
     * A constructor.
     * @param mapping Mapping Definition
     */
    public AtlasMappingHandler(AtlasMapping mapping) {
        this.mapping = mapping;
    }

    /**
     * Removes all references to the specified Document in the mapping definition.
     * It removes the DataSource entry as well as the mappings that refer to the Document.
     * @param docKey the indentifier of the Document to be removed
     */
    public void removeDocumentReference(DocumentKey docKey) {
        Optional<DataSource> dataSource = mapping.getDataSource().stream().filter(ds ->
            ds.getDataSourceType() == docKey.getDataSourceType() && ds.getId().equals(docKey.getDocumentId())
            ).findAny();
        if (dataSource.isPresent()) {
            mapping.getDataSource().remove(dataSource.get());
        }
        List<BaseMapping> mappings = mapping.getMappings().getMapping();
        removeDocumentReferenceFromMappings(mappings, docKey);
    }

    private void removeDocumentReferenceFromMappings(List<BaseMapping> mappings, DocumentKey docKey) {
        List<Mapping> toDelete = new ArrayList<>();
        for (BaseMapping m : mappings) {
            if (m instanceof Mapping) {
                Mapping mm = (Mapping)m;
                List<Field> fields;
                if (docKey.getDataSourceType() == DataSourceType.SOURCE) {
                    if (mm.getInputFieldGroup() != null) {
                        FieldGroup fg = mm.getInputFieldGroup();
                        if (fg.getPath() != null) {
                            fields = Collections.singletonList(fg);
                        } else {
                            fields = fg.getField();
                        }
                    } else {
                        fields = mm.getInputField();
                    }
                } else {
                    fields = mm.getOutputField();
                }
                for (Field f : fields) {
                    if (docKey.getDocumentId().equals(f.getDocId())) {
                        toDelete.add(mm);
                        break;
                    }
                }
            } else if (m instanceof Collection) {
                removeDocumentReferenceFromMappings(((Collection)m).getMappings().getMapping(), docKey);
            }
        }
        mappings.removeAll(toDelete);
    }

    /**
     * Gets the {@link AtlasMapping}.
     * @return AtlasMapping
     */
    public AtlasMapping getAtlasMapping() {
        return this.mapping;
    }

}
