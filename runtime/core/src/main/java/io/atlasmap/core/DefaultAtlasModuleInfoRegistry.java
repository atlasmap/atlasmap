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

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.spi.AtlasModuleInfo;
import io.atlasmap.spi.AtlasModuleInfoRegistry;

public class DefaultAtlasModuleInfoRegistry implements AtlasModuleInfoRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultAtlasModuleInfoRegistry.class);
    private final String jmxObjectNamePrefix;
    private final Set<AtlasModuleInfo> moduleInfos = new HashSet<>();

    public DefaultAtlasModuleInfoRegistry(DefaultAtlasContextFactory factory) {
        jmxObjectNamePrefix = factory.getJmxObjectName() + ",modules=AvailableModules,moduleName=";
    }

    @Override
    public AtlasModuleInfo lookupByUri(String uri) {
        if (uri == null) {
            return null;
        }
        for (AtlasModuleInfo module : moduleInfos) {
            if (uri.startsWith(module.getUri())) {
                return module;
            }
        }
        return null;
    }

    @Override
    public Set<AtlasModuleInfo> getAll() {
        return Collections.unmodifiableSet(moduleInfos);
    }

    @Override
    public void register(AtlasModuleInfo module) {
        moduleInfos.add(module);
        registerModuleJmx(module);
    }

    @Override
    public int size() {
        return moduleInfos.size();
    }

    @Override
    public synchronized void unregisterAll() {
        for (AtlasModuleInfo info : moduleInfos) {
            unregisterModuleJmx(info);
        }
        moduleInfos.clear();
    }

    private void registerModuleJmx(AtlasModuleInfo module) {
        try {
            String n = jmxObjectNamePrefix + module.getName();
            ManagementFactory.getPlatformMBeanServer().registerMBean(module, new ObjectName(n));

            if (LOG.isDebugEnabled()) {
                LOG.debug("Registered AtlasModule '" + module.getName() + "' with JMX");
            }
        } catch (Exception e) {
            LOG.warn("Unable to register AtlasModule '" + module.getName() + "' with JMX", e);
        }
    }

    private void unregisterModuleJmx(AtlasModuleInfo module) {
        try {
            String n = jmxObjectNamePrefix + module.getName();
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(n));
        } catch (Exception e) {
            LOG.warn("Unable to unregister module '" + module.getName() + "' from JMX");
        }
    }

}
