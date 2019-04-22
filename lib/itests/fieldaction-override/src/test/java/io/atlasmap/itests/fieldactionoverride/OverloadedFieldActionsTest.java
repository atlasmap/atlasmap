/**
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.itests.fieldactionoverride;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.java.test.SourceFlatPrimitiveClass;
import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.ActionDetail;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Mappings;

@Ignore("This only worked with JAXB model objects. Now that the model objects are handled only via JSON, it has to be added to ActionsJson{Serializer|Deserializer}.")
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
        assertEquals(Integer.valueOf(3), found);
    }
    
    @Test
    public void testMappingDayOfWeekString() throws Exception {
        
        AtlasContext context = atlasContextFactory.createContext(generateMappingDayOfWeek(String.class));
        AtlasSession session = context.createSession();
        SourceFlatPrimitiveClass src = new SourceFlatPrimitiveClass();
        src.setBoxedStringField("mon");
        session.setDefaultSourceDocument(src);
        
        context.process(session);
        
        Object tgt = session.getDefaultTargetDocument();
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
        session.setDefaultSourceDocument(src);
        
        context.process(session);
        
        Object tgt = session.getDefaultTargetDocument();
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
        ArrayList<Action> acts = new ArrayList<Action>();
        srcF.setActions(acts);

        JavaField tgtF = new JavaField();
        
        if(clazz.isAssignableFrom(String.class)) {
            srcF.setPath("boxedStringField");
            srcF.getActions().add(new DayOfWeekString());
            tgtF.setPath("boxedStringField");

        } else if(clazz.isAssignableFrom(Integer.class)){
            srcF.setPath("intField");
            srcF.getActions().add(new DayOfWeekInteger());
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
