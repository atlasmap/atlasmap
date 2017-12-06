package io.atlasmap.core;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.LookupEntry;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;

/**
 * Created by mmelko on 01/11/2017.
 */
public class DefaultAtlasContextTest extends BaseDefaultAtlasContextTest {

    @Test
    public void mapTest() throws AtlasException {
        Mapping m = (Mapping) AtlasModelFactory.createMapping(MappingType.MAP);
        mapping.getMappings().getMapping().add(m);
        populateSourceField(m, FieldType.STRING, "foo");
        prepareTargetField(m, "/target");
        context.process(session);
        Assert.assertFalse(printAudit(session), session.hasErrors());
        Assert.assertEquals("foo", writer.targets.get("/target"));
    }

    @Test
    public void mapNotExistingDocIdTest() throws AtlasException {
        Mapping m = (Mapping) AtlasModelFactory.createMapping(MappingType.MAP);
        mapping.getMappings().getMapping().add(m);
        populateSourceField(m, "docId.not.existing", FieldType.STRING, "foo");
        prepareTargetField(m, "/target");
        context.process(session);
        Assert.assertTrue(printAudit(session), session.hasErrors());
        Assert.assertEquals(1, session.getAudits().getAudit().stream().filter(a -> a.getStatus() == AuditStatus.ERROR).count());
    }

    @Test
    public void combineNonStringFieldsTest() throws AtlasException {
        Mapping m = (Mapping) AtlasModelFactory.createMapping(MappingType.COMBINE);
        mapping.getMappings().getMapping().add(m);
        m.setDelimiter(";");
        populateSourceField(m, FieldType.DATE_TIME, new Date(0), 0);
        populateSourceField(m, FieldType.INTEGER, 1, 1);
        populateSourceField(m, FieldType.DOUBLE, 2d, 2);
        populateSourceField(m, FieldType.FLOAT, 3f, 3);
        populateSourceField(m, FieldType.BOOLEAN, true, 4);
        populateSourceField(m, FieldType.NUMBER, 5, 5); //  not listed as primitive type
        populateSourceField(m, FieldType.SHORT, (short) 6, 6);
        populateSourceField(m, FieldType.STRING, "string", 7);
        populateSourceField(m, FieldType.BYTE, Byte.parseByte("8"), 8);
        populateSourceField(m, FieldType.CHAR, '9', 9);
        populateSourceField(m, FieldType.UNSIGNED_INTEGER, 10, 10);// not listed as primitive type
        prepareTargetField(m, "/target");
        context.process(session);
        Assert.assertFalse(printAudit(session), session.hasErrors());
        Assert.assertEquals(new Date(0).toString() + ";1;2.0;3.0;true;5;6;string;8;9;10", writer.targets.get("/target"));
    }

    @Test
    public void combineNonSupportedObjects() throws AtlasException {
        Mapping m = (Mapping) AtlasModelFactory.createMapping(MappingType.COMBINE);
        mapping.getMappings().getMapping().add(m);
        m.setDelimiter(";");
        populateUnsupportedSourceField(m, "foo", 0);
        populateUnsupportedSourceField(m, "bar", 1);
        prepareTargetField(m, "/target");
        context.process(session);
        Assert.assertFalse(printAudit(session), session.hasErrors());
        Assert.assertEquals("foo;bar", writer.targets.get("/target"));
    }

    @Test
    public void separateTest() throws Exception {
        Mapping m = (Mapping) AtlasModelFactory.createMapping(MappingType.SEPARATE);
        mapping.getMappings().getMapping().add(m);
        m.setDelimiter(";");
        populateSourceField(m, FieldType.STRING,  new Date(0).toString() + ";1;2.0;3.0;true;5;6;string;8;9;10");
        prepareTargetField(m, "/target1", 1);
        prepareTargetField(m, "/target0", 0);
        prepareTargetField(m, "/target10", 10);
        prepareTargetField(m, "/target9", 9);
        prepareTargetField(m, "/target8", 8);
        prepareTargetField(m, "/target7", 7);
        prepareTargetField(m, "/target6", 6);
        prepareTargetField(m, "/target5", 5);
        prepareTargetField(m, "/target4", 4);
        prepareTargetField(m, "/target3", 3);
        prepareTargetField(m, "/target2", 2);
        context.process(session);

        Assert.assertFalse(printAudit(session), session.hasErrors());
        Assert.assertEquals(new Date(0).toString(), writer.targets.get("/target0"));
        Assert.assertEquals("1", writer.targets.get("/target1"));
        Assert.assertEquals("2.0", writer.targets.get("/target2"));
        Assert.assertEquals("3.0", writer.targets.get("/target3"));
        Assert.assertEquals("true", writer.targets.get("/target4"));
        Assert.assertEquals("5", writer.targets.get("/target5"));
        Assert.assertEquals("6", writer.targets.get("/target6"));
        Assert.assertEquals("string", writer.targets.get("/target7"));
        Assert.assertEquals("8", writer.targets.get("/target8"));
        Assert.assertEquals("9", writer.targets.get("/target9"));
        Assert.assertEquals("10", writer.targets.get("/target10"));
    }

    @Test
    public void lookupTableTest() throws Exception {
        LookupTable table = new LookupTable();
        table.setName("table");
        LookupEntry e = new LookupEntry();
        e.setSourceValue("foo");
        e.setTargetValue("bar");
        table.getLookupEntry().add(e);
        context.getLookupTables().put(table.getName(), table);
        Mapping m = (Mapping) AtlasModelFactory.createMapping(MappingType.LOOKUP);
        mapping.getMappings().getMapping().add(m);
        m.setLookupTableName("table");
        populateSourceField(m, FieldType.STRING, "foo");
        prepareTargetField(m, "/target");
        context.process(session);
        Assert.assertFalse(printAudit(session), session.hasErrors());
        Assert.assertEquals("bar", writer.targets.get("/target"));
    }

}
