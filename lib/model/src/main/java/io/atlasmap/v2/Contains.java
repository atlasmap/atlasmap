package io.atlasmap.v2;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionParameter;

public class Contains extends Action implements AtlasFieldAction {

    private static final long serialVersionUID = 1L;

    @AtlasFieldActionParameter
    private String value;

    @AtlasFieldActionInfo(name = "Contains", sourceType = FieldType.ANY, targetType = FieldType.BOOLEAN, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public boolean contains(Object input) {
        if (input == null) {
            return getValue() == null;
        }

        if (input instanceof Collection) {
            return collectionContains((Collection<?>)input);
        }
        if (input.getClass().isArray()) {
            return collectionContains(Arrays.asList((Object[])input));
        }
        if (input instanceof Map<?, ?>) {
            if (collectionContains(((Map<?, ?>)input).values())) {
                return true;
            }
            return collectionContains(((Map<?, ?>)input).keySet());
        }
        if (getValue() == null) {
            return false;
        }
        return input.toString().contains(getValue());
    }

    /**
     * Gets the value of the value property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setValue(String value) {
        this.value = value;
    }

    private boolean collectionContains(Collection<?> collection) {
        for (Object item : collection) {
            if (item == null) {
                if (getValue() == null) {
                    return true;
                }
            } else if (item.toString().equals(getValue())) {
                return true;
            }
        }
        return false;
    }
}
