/*
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
package io.atlasmap.itests.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.ADMArchiveHandler;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Mapping;

public class NestedCollectionJavaTest {

    @Test
    public void testSamePaths1stLevelCollection() throws Exception {
        TargetClass target = processNestedJavaCollection(Arrays.asList("1-1"));
        assert1stLevelCollection(target);
    }

    @Test
    public void testSamePaths1stAnd2ndLevelNestedCollection() throws Exception {
        TargetClass target = processNestedJavaCollection(Arrays.asList("1-1", "2-2"));
        assert1stLevelCollection(target);
        assert2ndLevelCollection(target);
    }

    @Test
    public void testSamePaths2ndLevelNestedCollection() throws Exception {
        TargetClass target = processNestedJavaCollection(Arrays.asList("1-1", "2-2"));
        assert2ndLevelCollection(target);
    }

    @Test
    public void testSamePaths1stAnd2ndAnd3rdLevelNestedCollection() throws Exception {
        TargetClass target = processNestedJavaCollection(Arrays.asList("1-1", "2-2", "3-3"));
        assert1stLevelCollection(target);
        assert2ndLevelCollection(target);
        assert3rdLevelCollection(target);
    }

    @Test
    public void testSamePaths2ndAnd3rdLevelNestedCollection() throws Exception {
        TargetClass target = processNestedJavaCollection(Arrays.asList("2-2", "3-3"));
        assert2ndLevelCollection(target);
        assert3rdLevelCollection(target);
    }

    @Test
    public void testSamePaths1stAnd3rdLevelNestedCollection() throws Exception {
        TargetClass target = processNestedJavaCollection(Arrays.asList("1-1", "3-3"));
        assert1stLevelCollection(target);
        assert3rdLevelCollection(target);

    }

    @Test
    public void testSamePaths3rdLevelNestedCollection() throws Exception {
        TargetClass target = processNestedJavaCollection(Arrays.asList("3-3"));
        assert3rdLevelCollection(target);
    }

    @Test
    public void testRenamedPaths3rdLevelNestedCollection() throws Exception {
        TargetClass target = processNestedJavaCollection(Arrays.asList("3-3renamed"));
        assert3rdLevelRenamedCollection(target);
    }

    @Test
    public void testSamePaths1stAndRenamedPaths3rdLevelNestedCollection() throws Exception {
        TargetClass target = processNestedJavaCollection(Arrays.asList("1-1", "3-3renamed"));
        assert1stLevelCollection(target);
        assert3rdLevelRenamedCollection(target);
    }

    @Test
    public void testSamePaths1stAnd2nAndRenamedPaths3rdLevelNestedCollection() throws Exception {
        TargetClass target = processNestedJavaCollection(Arrays.asList("1-1", "2-2", "3-3renamed"));
        assert1stLevelCollection(target);
        assert2ndLevelCollection(target);
        assert3rdLevelRenamedCollection(target);
    }

    @Test
    public void testSamePaths1stAnd2ndAnd3rdAndRenamedPaths3rdLevelNestedCollection() throws Exception {
        TargetClass target = processNestedJavaCollection(Arrays.asList("1-1", "2-2", "3-3", "3-3renamed"));
        assert1stLevelCollection(target);
        assert2ndLevelCollection(target);
        assert3rdLevelCollection(target);
        assert3rdLevelRenamedCollection(target);
    }

    @Test
    public void testAsymmetricPaths1stAnd2ndAnd3rdLevelNestedCollectionToSingleCollection() throws Exception {
        TargetClass target = processNestedJavaCollection(Arrays.asList("3-1"));

        assertEquals(5, target.getSomeArray().length);
        assertEquals("array000", target.getSomeArray()[0].getSomeField());
        assertEquals("array001", target.getSomeArray()[1].getSomeField());
        assertEquals("array002", target.getSomeArray()[2].getSomeField());
        assertEquals("array100", target.getSomeArray()[3].getSomeField());
        assertEquals("array101", target.getSomeArray()[4].getSomeField());
        assertNull(target.getSomeField());
        assertNull(target.getSomeArray()[0].getSomeArray());
        assertNull(target.getSomeArray()[1].getSomeArray());
        assertNull(target.getSomeArray()[2].getSomeArray());
        assertNull(target.getSomeArray()[3].getSomeArray());
        assertNull(target.getSomeArray()[4].getSomeArray());
    }

    @Test
    public void testAsymmetricPaths2ndLevelNestedCollectionTo4LevelsNestedCollection() throws Exception {
        TargetClass target = processNestedJavaCollection(Arrays.asList("2-4"));

        assertEquals(1, target.getSomeArray().length);
        assertEquals(1, target.getSomeArray()[0].getSomeArray().length);
        BaseClass.SomeNestedClass[] nestedArray = target.getSomeArray()[0].getSomeArray()[0].getSomeArray();
        TargetClass nestedTarget = new TargetClass();
        nestedTarget.setSomeArray(nestedArray);
        assert2ndLevelCollection(nestedTarget);
    }

    @Test
    public void test3To2LevelNestedCollection() throws Exception {
        TargetClass target = processNestedJavaCollection(Arrays.asList("3-2"));

        assertEquals(2, target.getSomeArray().length);
        assertEquals(3, target.getSomeArray()[0].getSomeArray().length);
        assertEquals("array000", target.getSomeArray()[0].getSomeArray()[0].getSomeField());
        assertEquals("array001", target.getSomeArray()[0].getSomeArray()[1].getSomeField());
        assertEquals("array002", target.getSomeArray()[0].getSomeArray()[2].getSomeField());
        assertEquals(2, target.getSomeArray()[1].getSomeArray().length);
        assertEquals("array100", target.getSomeArray()[1].getSomeArray()[0].getSomeField());
        assertEquals("array101", target.getSomeArray()[1].getSomeArray()[1].getSomeField());
    }

    @Test
    public void test4To2LevelNestedCollection() throws Exception {
        TargetClass target = processNestedJavaCollection(Arrays.asList("4-2"));

        assertEquals(2, target.getSomeArray().length);
        assertNull(target.getSomeArray()[0]);
        assertEquals(3, target.getSomeArray()[1].getSomeArray().length);
        assertEquals("array1000", target.getSomeArray()[1].getSomeArray()[0].getSomeField());
        assertEquals("array1001", target.getSomeArray()[1].getSomeArray()[1].getSomeField());
        assertEquals("array1010", target.getSomeArray()[1].getSomeArray()[2].getSomeField());
    }

    private void assert1stLevelCollection(TargetClass target) {
        assertEquals(3, target.getSomeArray().length);
        assertEquals("array0", target.getSomeArray()[0].getSomeField());
        assertEquals("array1", target.getSomeArray()[1].getSomeField());
        assertEquals("array2", target.getSomeArray()[2].getSomeField());
    }

    private void assert2ndLevelCollection(TargetClass target) {
        assertEquals(2, target.getSomeArray()[0].getSomeArray().length);
        assertEquals("array00", target.getSomeArray()[0].getSomeArray()[0].getSomeField());
        assertEquals("array01", target.getSomeArray()[0].getSomeArray()[1].getSomeField());

        assertEquals(1, target.getSomeArray()[1].getSomeArray().length);
        assertEquals("array10", target.getSomeArray()[1].getSomeArray()[0].getSomeField());
    }

    private void assert3rdLevelCollection(TargetClass target) {
        assertEquals(3, target.getSomeArray()[0].getSomeArray()[0].getSomeArray().length);
        assertEquals("array000", target.getSomeArray()[0].getSomeArray()[0].getSomeArray()[0].getSomeField());
        assertEquals("array001", target.getSomeArray()[0].getSomeArray()[0].getSomeArray()[1].getSomeField());
        assertEquals("array002", target.getSomeArray()[0].getSomeArray()[0].getSomeArray()[2].getSomeField());

        assertEquals(2, target.getSomeArray()[1].getSomeArray()[0].getSomeArray().length);
        assertEquals("array100", target.getSomeArray()[1].getSomeArray()[0].getSomeArray()[0].getSomeField());
        assertEquals("array101", target.getSomeArray()[1].getSomeArray()[0].getSomeArray()[1].getSomeField());
    }

    private void assert3rdLevelRenamedCollection(TargetClass target) {
        assertEquals(3, target.getSomeRenamedArray()[0].getSomeArray()[0].getSomeRenamedArray().length);
        assertEquals("array000", target.getSomeRenamedArray()[0].getSomeArray()[0].getSomeRenamedArray()[0].getSomeField());
        assertEquals("array001", target.getSomeRenamedArray()[0].getSomeArray()[0].getSomeRenamedArray()[1].getSomeField());
        assertEquals("array002", target.getSomeRenamedArray()[0].getSomeArray()[0].getSomeRenamedArray()[2].getSomeField());

        assertEquals(2, target.getSomeRenamedArray()[1].getSomeArray()[0].getSomeRenamedArray().length);
        assertEquals("array100", target.getSomeRenamedArray()[1].getSomeArray()[0].getSomeRenamedArray()[0].getSomeField());
        assertEquals("array101", target.getSomeRenamedArray()[1].getSomeArray()[0].getSomeRenamedArray()[1].getSomeField());
    }

    private TargetClass processNestedJavaCollection(List<String> mappingsToProcess) throws AtlasException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("mappings/atlasmapping-nested-collection-java.json");
        ADMArchiveHandler admHandler = new ADMArchiveHandler(Thread.currentThread().getContextClassLoader());
        admHandler.load(AtlasContextFactory.Format.JSON, in);
        AtlasMapping mapping = admHandler.getMappingDefinition();
        mapping.getMappings().getMapping().removeIf(m -> !mappingsToProcess.contains(((Mapping) m).getId()));
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        SourceClass sc = newSourceClass();
        session.setSourceDocument("io.atlasmap.itests.core.SourceClass", sc);
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        return (TargetClass) session.getTargetDocument("io.atlasmap.itests.core.TargetClass");
    }

    private SourceClass newSourceClass() {
        SourceClass sc = new SourceClass();
        sc.setSomeField("field");

        BaseClass.SomeNestedClass array0 = new BaseClass.SomeNestedClass("array0");
        BaseClass.SomeNestedClass array1 = new BaseClass.SomeNestedClass("array1");
        BaseClass.SomeNestedClass array2 = new BaseClass.SomeNestedClass("array2");

        BaseClass.SomeNestedClass[] someArray = new BaseClass.SomeNestedClass[] {
            array0, array1, array2
        };
        sc.setSomeArray(someArray);

        BaseClass.SomeNestedClass array00 = new BaseClass.SomeNestedClass("array00");
        array00.setSomeArray(new BaseClass.SomeNestedClass[] { new BaseClass.SomeNestedClass("array000"),
            new BaseClass.SomeNestedClass("array001"), new BaseClass.SomeNestedClass("array002")});

        array0.setSomeArray(new BaseClass.SomeNestedClass[] {array00, new BaseClass.SomeNestedClass("array01")});

        BaseClass.SomeNestedClass array10 = new BaseClass.SomeNestedClass("array10");
        array1.setSomeArray(new BaseClass.SomeNestedClass[] { array10 });

        array2.setSomeArray(new BaseClass.SomeNestedClass[0]);

        BaseClass.SomeNestedClass array100 = new BaseClass.SomeNestedClass("array100");
        BaseClass.SomeNestedClass array101 = new BaseClass.SomeNestedClass("array101");

        array10.setSomeArray(new BaseClass.SomeNestedClass[] { array100, array101});

        array100.setSomeArray(new BaseClass.SomeNestedClass[] { new BaseClass.SomeNestedClass("array1000"),
            new BaseClass.SomeNestedClass("array1001") });

        array101.setSomeArray(new BaseClass.SomeNestedClass[] { new BaseClass.SomeNestedClass("array1010") });

        return sc;
    }

}
