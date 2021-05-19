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

public interface AtlasModuleMXBean {

    String getUuid();

    String getName();

    String getClassName();

    String getVersion();

    String[] getDataFormats();

    String[] getPackageNames();

    String getModeName();

    boolean isSourceSupported();

    boolean isTargetSupported();

    Boolean isStatisticsEnabled();

    void setStatisticsEnabled(boolean enabled);

    long getSourceCount();

    long getSourceErrorCount();

    long getSourceSuccessCount();

    long getSourceMinExecutionTime();

    long getSourceMaxExecutionTime();

    long getSourceTotalExecutionTime();

    long getTargetCount();

    long getTargetErrorCount();

    long getTargetSuccessCount();

    long getTargetMinExecutionTime();

    long getTargetMaxExecutionTime();

    long getTargetTotalExecutionTime();

    TabularData readAndResetStatistics() throws OpenDataException;

}
