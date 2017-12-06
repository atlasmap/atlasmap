package io.atlasmap.reference;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;

import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.v2.Audit;

public abstract class AtlasMappingBaseTest {

    public static final List<String> FLAT_FIELDS = Arrays.asList("intField", "shortField", "longField", "doubleField",
            "floatField", "booleanField", "charField", "byteField", "boxedBooleanField", "boxedByteField",
            "boxedCharField", "boxedDoubleField", "boxedFloatField", "boxedIntField", "boxedLongField",
            "boxedStringField");

    protected AtlasContextFactory atlasContextFactory = null;

    @Before
    public void setUp() {
        atlasContextFactory = DefaultAtlasContextFactory.getInstance();
    }

    @After
    public void tearDown() {
        atlasContextFactory = null;
    }

    protected String printAudit(AtlasSession session) {
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
