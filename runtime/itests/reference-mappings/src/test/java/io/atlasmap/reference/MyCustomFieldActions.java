package io.atlasmap.reference;

import java.util.Iterator;
import java.util.List;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldType;

public class MyCustomFieldActions implements AtlasFieldAction {

    @AtlasFieldActionInfo(name = "Concat", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public static String concat(Object input) {
        @SuppressWarnings("unchecked")
        Iterator<String> list = ((List<String>)input).iterator();
        StringBuilder buf = new StringBuilder();
        buf.append(list.next());
        while (list.hasNext()) {
            buf.append("-").append(list.next());
        }
        return buf.toString();
    }

}
