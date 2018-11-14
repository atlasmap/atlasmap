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
package io.atlasmap.mxbean;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;

public interface AtlasModuleMXBean {
    public String getUuid();

    public String getName();

    public String getClassName();

    public String getVersion();

    public String[] getDataFormats();

    public String[] getPackageNames();

    public String getModeName();

    public boolean isSourceSupported();

    public boolean isTargetSupported();

    public Boolean isStatisticsEnabled();

    public void setStatisticsEnabled(boolean enabled);

    public long getSourceCount();

    public long getSourceErrorCount();

    public long getSourceSuccessCount();

    public long getSourceMinExecutionTime();

    public long getSourceMaxExecutionTime();

    public long getSourceTotalExecutionTime();

    public long getTargetCount();

    public long getTargetErrorCount();

    public long getTargetSuccessCount();

    public long getTargetMinExecutionTime();

    public long getTargetMaxExecutionTime();

    public long getTargetTotalExecutionTime();

    public TabularData readAndResetStatistics() throws OpenDataException;

}
