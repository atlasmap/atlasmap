package io.atlasmap.mock.v2;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.java.test.SourceFlatPrimitiveClass;
import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.ActionDetail;
import io.atlasmap.v2.Actions;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Mappings;

public class OverloadedFieldActionsTest {
    
    private static DefaultAtlasContextFactory atlasContextFactory = null;
    
    @BeforeClass
    public static void setupClass() throws Exception {
        atlasContextFactory = DefaultAtlasContextFactory.getInstance();
    }
    
    @Test
    public void testListActions() throws Exception {
        List<ActionDetail> actions = DefaultAtlasContextFactory.getInstance().getFieldActionService().listActionDetails();
        
        Integer found = 0;
        for (ActionDetail d : actions) {
            if(d.getName().equals("DayOfWeek")) {
                found++;
            }
        }
        assertEquals(Integer.valueOf(2), found);
    }
    
    @Test
    public void testMappingDayOfWeekString() throws Exception {
        
        AtlasContext context = atlasContextFactory.createContext(generateMappingDayOfWeek(String.class));
        AtlasSession session = context.createSession();
        SourceFlatPrimitiveClass src = new SourceFlatPrimitiveClass();
        src.setBoxedStringField("mon");
        session.setInput(src);
        
        context.process(session);
        
        Object tgt = session.getOutput();
        assertNotNull(tgt);
        assertTrue(tgt.getClass().isAssignableFrom(TargetFlatPrimitiveClass.class));
        
        System.out.println(((TargetFlatPrimitiveClass)tgt).getBoxedStringField());
    }

    @Test
    public void testMappingDayOfWeekInteger() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(generateMappingDayOfWeek(Integer.class));
        AtlasSession session = context.createSession();
        SourceFlatPrimitiveClass src = new SourceFlatPrimitiveClass();
        src.setIntField(1);
        session.setInput(src);
        
        context.process(session);
        
        Object tgt = session.getOutput();
        assertNotNull(tgt);
        assertTrue(tgt.getClass().isAssignableFrom(TargetFlatPrimitiveClass.class));
        
        System.out.println(((TargetFlatPrimitiveClass)tgt).getBoxedStringField());
    }
    
    protected AtlasMapping generateMappingDayOfWeek(Class<?> clazz) {
        AtlasMapping m = new AtlasMapping();
        DataSource s = new DataSource();
        s.setDataSourceType(DataSourceType.SOURCE);
        s.setUri("atlas:java?className=io.atlasmap.java.test.SourceFlatPrimitiveClass");
        DataSource t = new DataSource();
        t.setDataSourceType(DataSourceType.TARGET);
        t.setUri("atlas:java?className=io.atlasmap.java.test.TargetFlatPrimitiveClass");
        
        m.getDataSource().add(s);
        m.getDataSource().add(t);
        
        
        Mapping mfm = AtlasModelFactory.createMapping(MappingType.MAP);
        JavaField srcF = new JavaField();
        Actions acts = new Actions();
        srcF.setActions(acts);

        JavaField tgtF = new JavaField();
        
        if(clazz.isAssignableFrom(String.class)) {
            srcF.setPath("boxedStringField");
            srcF.getActions().getActions().add(new DayOfWeekString());
            tgtF.setPath("boxedStringField");

        } else if(clazz.isAssignableFrom(Integer.class)){
            srcF.setPath("intField");
            srcF.getActions().getActions().add(new DayOfWeekInteger());
            tgtF.setPath("boxedStringField");
        }
        
        mfm.getInputField().add(srcF);
        mfm.getOutputField().add(tgtF);
        
        Mappings maps = new Mappings();
        maps.getMapping().add(mfm);
        
        m.setMappings(maps);
        
        return m;
    }
}
