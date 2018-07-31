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

import java.io.File;

import io.atlasmap.api.v3.MappingDocument;
import io.atlasmap.spi.v3.util.AtlasException;
import io.atlasmap.spi.v3.util.VerifyArgument;

// TODO GUI
// TODO GUI auto combine/separate on multiple selection

// TODO don't call autosave if not really modified
// TODO combine/split
// TODO mark validation warnings as ignored

// TODO global properties
// TODO Separate runtime and modeler into their own projects
// TODO i18n
// TODO java handler fields as getter/setter, name in method, etc.
// TODO undo/redo
// TODO new mapping for additional transformations with target field(s)?
// TODO target field as source (implies dependency management)
// TODO JSON, XML handlers
// TODO unions (SML choice)
// TODO if transformation
// TODO test old customer giant XML
// TODO add more transformations
// TODO collections, maps, arrays
// TODO test everything to everything
// TODO auto-map
// TODO provide handler-specific type info in parameter api, save in mapping file
public class Atlas {

    private final Context context;

    public Atlas(File mappingFile) throws AtlasException {
        VerifyArgument.isNotNull("mappingFile", mappingFile);
        context = new Context(mappingFile);
    }

    public MappingDocument mappingDocument() {
        return context.mappingDocument;
    }
}
