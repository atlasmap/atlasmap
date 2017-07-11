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
package io.atlasmap.reference.javaToJava;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService;
import io.atlasmap.core.DefaultAtlasContext;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.java.test.BaseOrderList;
import io.atlasmap.java.test.SourceAddress;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.SourceOrder;
import io.atlasmap.java.test.SourceOrderList;
import io.atlasmap.java.test.TargetOrderList;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaCollection;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.reference.AtlasTestUtil;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Mappings;

public class JavaJavaComplexListTest extends AtlasMappingBaseTest {

    @Test
    public void testGenerateListMappingBasic() throws Exception {
        AtlasMapping a = AtlasModelFactory.createAtlasMapping();
        a.setName("JavaJavaComplexListBase");
        DataSource s = new DataSource();
        s.setDataSourceType(DataSourceType.SOURCE);
        s.setUri("atlas:java?className=io.atlasmap.java.test.SourceOrderList");
        
        DataSource t = new DataSource();
        t.setDataSourceType(DataSourceType.TARGET);
        t.setUri("atlas:java?className=io.atlasmap.java.test.TargetOrderList");
        
        JavaField f1 = AtlasJavaModelFactory.createJavaField();
        f1.setPath("numberOrders");
        f1.setModifiers(null);
        
        JavaField f2 = AtlasJavaModelFactory.createJavaField();
        f2.setPath("orderBatchNumber");
        f2.setModifiers(null);
        
        Mapping m1 = AtlasModelFactory.createMapping(MappingType.MAP);
        m1.getInputField().add(f1);
        m1.getOutputField().add(f1);
        
        Mapping m2 = AtlasModelFactory.createMapping(MappingType.MAP);
        m2.getInputField().add(f2);
        m2.getOutputField().add(f2);
        
        JavaCollection cm = new JavaCollection();
        cm.setMappingType(MappingType.COLLECTION);
        cm.setCollectionType(CollectionType.LIST);
        
        JavaField f3 = AtlasJavaModelFactory.createJavaField();
        f3.setPath("orders<>.orderId");
        f3.setModifiers(null);
                
        Mapping m3 = AtlasModelFactory.createMapping(MappingType.MAP);
        m3.getInputField().add(f3);
        m3.getOutputField().add(f3);
               
        if(cm.getMappings() == null) {
            cm.setMappings(new Mappings());
        }
        
        cm.getMappings().getMapping().add(m3);
                
        a.getDataSource().addAll(Arrays.asList(s, t));
        a.getMappings().getMapping().addAll(Arrays.asList(m1, m2, cm));
        
        AtlasMappingService atlasMappingService = new AtlasMappingService(Arrays.asList("io.atlasmap.v2", "io.atlasmap.java.v2", "io.atlasmap.xml.v2"));
        atlasMappingService.saveMappingAsFile(a, new File("src/test/resources/javaToJava/atlasmapping-complex-list-autodetect-base.xml"));
    }
    
    @Test
    public void testProcessJavaJavaComplexAutoDetectBaseTest() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-complex-list-autodetect-base.xml"));
        ((DefaultAtlasContext)context).setNewProcessFlow(true);
        AtlasSession session = context.createSession();
        BaseOrderList sourceOrderList = AtlasTestUtil.generateOrderListClass(SourceOrderList.class, SourceOrder.class, SourceAddress.class, SourceContact.class);
        session.setInput(sourceOrderList);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetOrderList);
        AtlasTestUtil.validateOrderList((TargetOrderList)object);
    }
    
    @Test
    public void testProcessJavaJavaComplexAutoDetectFullTest() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-complex-list-autodetect-full.xml"));
        ((DefaultAtlasContext)context).setNewProcessFlow(true);
        AtlasSession session = context.createSession();
        BaseOrderList sourceOrderList = AtlasTestUtil.generateOrderListClass(SourceOrderList.class, SourceOrder.class, SourceAddress.class, SourceContact.class);
        session.setInput(sourceOrderList);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetOrderList);
        AtlasTestUtil.validateOrderList((TargetOrderList)object);
    }

}
