package io.atlasmap.java.inspect;

import java.util.List;

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
