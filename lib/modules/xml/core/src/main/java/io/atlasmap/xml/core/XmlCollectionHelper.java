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
package io.atlasmap.xml.core;

import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.DefaultAtlasCollectionHelper;
import io.atlasmap.spi.AtlasFieldActionService;

public class XmlCollectionHelper extends DefaultAtlasCollectionHelper {

    public XmlCollectionHelper(AtlasFieldActionService fieldActionService) {
        super(fieldActionService);
    }

    @Override
    protected AtlasPath createTargetAtlasPath(String path) {
        return new XmlPath(path);
    }

}
