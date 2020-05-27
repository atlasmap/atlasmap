package io.atlasmap.itests.reference.json_to_json;

import io.atlasmap.api.AtlasSession;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonTestHelper {

    static void addInputMappings(AtlasSession session, Action... mappings) {
        addMappings(((Mapping) session.getMapping().getMappings().getMapping().get(0)).getInputField().get(0), mappings);
    }

    static void addMappings(Field f, Action... mappings) {
        ArrayList<Action> l = new ArrayList<>();
        for (Action m : mappings) {
            l.add(m);
        }
        if (f.getActions() == null) {
            f.setActions(l);
        } else {
            f.getActions().addAll(l);
        }
    }

    static JsonField createJsonStringField(String path) {
        JsonField field = new JsonField();
        field.setPath(path);
        Pattern p = Pattern.compile(".*/(\\w+)<?>?");
        Matcher m = p.matcher(path);
        if (m.matches()) {
            field.setName(m.group(1));
        }
        field.setFieldType(FieldType.STRING);
        return field;
    }

    static JsonField addInputStringField(AtlasSession session, String path) {
        if (session.getMapping().getMappings().getMapping().isEmpty()) {
            session.getMapping().getMappings().getMapping().add(new Mapping());
        }

        JsonField f = createJsonStringField(path);
        ((Mapping) session.getMapping().getMappings().getMapping().get(0)).getInputField().add(f);
        return f;
    }

    static JsonField addOutputStringField(AtlasSession session, String path) {
        if (session.getMapping().getMappings().getMapping().isEmpty()) {
            session.getMapping().getMappings().getMapping().add(new Mapping());
        }

        JsonField f = createJsonStringField(path);
        ((Mapping) session.getMapping().getMappings().getMapping().get(0)).getOutputField().add(f);
        return f;
    }
}
