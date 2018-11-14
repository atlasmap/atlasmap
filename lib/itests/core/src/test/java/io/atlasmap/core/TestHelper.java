package io.atlasmap.core;

import io.atlasmap.api.AtlasSession;
import io.atlasmap.v2.Audit;

public class TestHelper {

    public static String printAudit(AtlasSession session) {
        StringBuilder buf = new StringBuilder("Audits: ");
        for (Audit a : session.getAudits().getAudit()) {
            buf.append('[');
            buf.append(a.getStatus());
            buf.append(", message=");
            buf.append(a.getMessage());
            buf.append("], ");
        }
        return buf.toString();
    }

}
