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

import java.lang.reflect.Constructor;
import java.util.List;

import io.atlasmap.mxbean.AtlasModuleInfoMXBean;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.spi.AtlasModuleInfo;

public class DefaultAtlasModuleInfo implements AtlasModuleInfo, AtlasModuleInfoMXBean {
    private String name;
    private String uri;
    private Boolean sourceSupported;
    private Boolean targetSupported;
    private Class<AtlasModule> moduleClass;
    private Constructor<AtlasModule> constructor;
    private List<String> formats;
    private List<String> packageNames;

    public DefaultAtlasModuleInfo(String name, String uri, Class<AtlasModule> moduleClass, Constructor<AtlasModule> constructor,
            List<String> formats, List<String> packageNames) {
        this.name = name;
        this.uri = uri;
        this.moduleClass = moduleClass;
        this.constructor = constructor;
        this.formats = formats;
        this.packageNames = packageNames;
    }

    @Override
    public String getModuleClassName() {
        if (moduleClass != null) {
            return moduleClass.getName();
        }
        return null;
    }

    public Constructor<AtlasModule> getConstructor() {
        return constructor;
    }

    public List<String> getFormats() {
        return formats;
    }

    @Override
    public Class<AtlasModule> getModuleClass() {
        return moduleClass;
    }

    @Override
    public String[] getDataFormats() {
        if (formats != null) {
            return formats.toArray(new String[formats.size()]);
        }
        return new String[0];
    }

    @Override
    public String[] getPackageNames() {
        if (packageNames == null || packageNames.size() < 1) {
            return new String[0];
        }

        return packageNames.toArray(new String[packageNames.size()]);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public Boolean isSourceSupported() {
        return sourceSupported;
    }

    @Override
    public Boolean isTargetSupported() {
        return targetSupported;
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getVersion() {
        return this.getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String toString() {
        return "DefaultAtlasModuleInfo [name=" + name + ", uri=" + uri + ", sourceSupported=" + sourceSupported
                + ", targetSupported=" + targetSupported + ", moduleClass=" + moduleClass + ", constructor="
                + constructor + ", formats=" + formats + ", packageNames=" + packageNames + "]";
    }
}
