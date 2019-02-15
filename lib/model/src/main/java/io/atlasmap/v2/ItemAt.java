package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionParameter;

public class ItemAt extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionParameter(type = FieldType.INTEGER)
    private Integer index;

    @AtlasFieldActionInfo(name = "ItemAt", sourceType = FieldType.ANY, targetType = FieldType.ANY, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public Object itemAt(Object input) {
        if (input == null) {
            return null;
        }

        Integer index = getIndex() == null ? 0 : getIndex();
        Object[] array = ActionUtil.collection(input).toArray(new Object[0]);
        if (array.length > index) {
            return array[index];
        }
        throw new ArrayIndexOutOfBoundsException(String.format(
                "Collection '%s' has fewer (%s) than expected (%s)", array, array.length, index));
    }

    /**
     * Gets the value of the index property.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getIndex() {
        return index;
    }

    /**
     * Sets the value of the index property.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setIndex(Integer value) {
        this.index = value;
    }

}
