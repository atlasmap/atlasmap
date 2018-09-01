package io.atlasmap.v2;

public interface FieldAction {

    default String getDisplayName() {
        // TODO display name should be more human readable one instead of class name
        return this.getClass().getSimpleName();
    }
}
