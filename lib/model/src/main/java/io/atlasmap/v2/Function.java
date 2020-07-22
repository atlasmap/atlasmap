package io.atlasmap.v2;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Function {

    @JsonIgnore
    default String getDisplayName() {
        // TODO display name should be more human readable one instead of class name
        return this.getClass().getSimpleName();
    }
}
