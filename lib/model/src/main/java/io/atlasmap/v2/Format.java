package io.atlasmap.v2;

import java.util.Locale;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionParameter;

public class Format extends Action implements AtlasFieldAction {

    private static final long serialVersionUID = 1L;

    @AtlasFieldActionParameter
    private String template;

    @AtlasFieldActionInfo(name = "Format", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public String format(Object input) {
        if (getTemplate() == null) {
            throw new IllegalArgumentException("Format must be specified with a template");
        }

        return String.format(Locale.ROOT, getTemplate(), input);
    }

    /**
     * Gets the value of the template property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the value of the template property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTemplate(String value) {
        this.template = value;
    }

}
