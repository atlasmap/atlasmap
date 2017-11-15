package io.atlasmap.v2;

public interface FieldAction {

    default String getDisplayName() {
        return this.getClass().getSimpleName();
    }
}
