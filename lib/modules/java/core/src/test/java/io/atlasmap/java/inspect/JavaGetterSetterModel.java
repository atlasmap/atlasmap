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
package io.atlasmap.java.inspect;

import java.util.List;

@SuppressWarnings("unused")
public class JavaGetterSetterModel {

    private String param;
    private String overloadParam;
    private Byte bite;
    private List<Byte> bites;

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getOverloadParam() {
        return this.overloadParam;
    }

    public void setOverloadParam(String overloadParam) {
        this.overloadParam = overloadParam;
    }

    public void setOverloadParam(Integer overloadParam) {
        this.overloadParam = Integer.toString(overloadParam);
    }

    public void setOverloadParam(Integer overloadParam, List<String> param2) {
        this.overloadParam = Integer.toString(overloadParam);
    }

    public void setOverloadParamNoGetter(Byte bite) {
        this.bite = bite;
    }

    public void setOverloadParamNoGetter(List<Byte> bites) {
        this.bites = bites;
    }

    public void setOverloadParamNoMatch(String foo, String bar) {
        this.param = foo;
    }

    public void setOverloadParamNoMatch(String foo, Short bar) {
        this.param = foo;
    }
}
