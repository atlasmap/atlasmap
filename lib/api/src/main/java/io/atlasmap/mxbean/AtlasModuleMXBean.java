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
package io.atlasmap.mxbean;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;

/**
 * The {@link io.atlasmap.spi.AtlasModule} MBean.
 */
public interface AtlasModuleMXBean {

    /**
     * Gets the UUID.
     * @return UUID
     */
    String getUuid();

    /**
     * Gets the name.
     * @return name
     */
    String getName();

    /**
     * Gets the class name.
     * @return class name
     */
    String getClassName();

    /**
     * Gets the version.
     * @return version
     */
    String getVersion();

    /**
     * Gets the data formats.
     * @return data formats.
     */
    String[] getDataFormats();

    /**
     * Gets the package names.
     * @return package names.
     */
    String[] getPackageNames();

    /**
     * Gets the mode name.
     * @return mode name
     */
    String getModeName();

    /**
     * Gets if it supports to be a source Document.
     * @return true if supported
     */
    boolean isSourceSupported();

    /**
     * Gets if it supports to be a target Document.
     * @return true if supported
     */
    boolean isTargetSupported();

    /**
     * Gets if statistics is enabled.
     * @return true if enabled
     */
    Boolean isStatisticsEnabled();

    /**
     * Sets if statistics is enabled.
     * @param enabled true if enable
     */
    void setStatisticsEnabled(boolean enabled);

    /**
     * Gets source count.
     * @return source count
     */
    long getSourceCount();

    /**
     * Gets source error count.
     * @return source error count
     */
    long getSourceErrorCount();

    /**
     * Gets source success count.
     * @return source success count.
     */
    long getSourceSuccessCount();

    /**
     * Gets source minumum execution time.
     * @return source minumum execution time.
     */
    long getSourceMinExecutionTime();

    /**
     * Gets source maximum execution time.
     * @return source maximum execution time.
     */
    long getSourceMaxExecutionTime();

    /**
     * Gets source total execution time.
     * @return source total execution time
     */
    long getSourceTotalExecutionTime();

    /**
     * Gets target count.
     * @return target count
     */
    long getTargetCount();

    /**
     * Gets target error count.
     * @return target error count
     */
    long getTargetErrorCount();

    /**
     * Gets target success count.
     * @return target success count
     */
    long getTargetSuccessCount();

    /**
     * Gets target minumum execution time.
     * @return target minumum execution time
     */
    long getTargetMinExecutionTime();

    /**
     * Gets target maximum execution time.
     * @return target maximum execution time
     */
    long getTargetMaxExecutionTime();

    /**
     * Gets target total execution time.
     * @return target total execution time.
     */
    long getTargetTotalExecutionTime();

    /**
     * Reads and resets the statistics.
     * @return statistics
     * @throws OpenDataException unexpected error
     */
    TabularData readAndResetStatistics() throws OpenDataException;

}
