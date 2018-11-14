package io.atlasmap.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
// import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;
// import java.util.regex.Pattern;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService.AtlasMappingFormat;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.ActionDetail;
import io.atlasmap.v2.Actions;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Capitalize;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Field;
// import io.atlasmap.v2.GenerateUUID;
import io.atlasmap.v2.Length;
import io.atlasmap.v2.Lowercase;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Mappings;
import io.atlasmap.v2.PadStringLeft;
import io.atlasmap.v2.PadStringRight;
import io.atlasmap.v2.SeparateByDash;
import io.atlasmap.v2.SeparateByUnderscore;
import io.atlasmap.v2.SubString;
import io.atlasmap.v2.SubStringAfter;
import io.atlasmap.v2.SubStringBefore;
import io.atlasmap.v2.Trim;
import io.atlasmap.v2.TrimLeft;
import io.atlasmap.v2.TrimRight;
import io.atlasmap.v2.Uppercase;

public abstract class AtlasBaseActionsTest extends AtlasMappingBaseTest {

    protected Field sourceField = null;
    protected Field targetField = null;
    protected String docURI = null;
    protected boolean stringLengthTestResultIsInteger = false;

    @Test
    public void testActions() throws Exception {
        List<ActionDetail> actions = DefaultAtlasContextFactory.getInstance().getFieldActionService()
                .listActionDetails();
        for (ActionDetail d : actions) {
            System.out.println(d.getName());
        }

        this.runActionTest(new Uppercase(), "fname", "FNAME", String.class);
        this.runActionTest(new Lowercase(), "fnAme", "fname", String.class);

        this.runActionTest(new Trim(), " fname ", "fname", String.class);
        this.runActionTest(new TrimLeft(), " fname ", "fname ", String.class);
        this.runActionTest(new TrimRight(), " fname ", " fname", String.class);
        this.runActionTest(new Capitalize(), "fname", "Fname", String.class);
        this.runActionTest(new SeparateByDash(), "f:name", "f-name", String.class);
        this.runActionTest(new SeparateByUnderscore(), "f-na_me", "f_na_me", String.class);

        SubString s = new SubString();
        s.setStartIndex(0);
        s.setEndIndex(3);
        this.runActionTest(s, "12345", "123", String.class);

        SubStringAfter s1 = new SubStringAfter();
        s1.setStartIndex(3);
        s1.setEndIndex(null);
        s1.setMatch("foo");
        this.runActionTest(s1, "foobarblah", "blah", String.class);

        SubStringBefore s2 = new SubStringBefore();
        s2.setStartIndex(3);
        s2.setEndIndex(null);
        s2.setMatch("blah");
        this.runActionTest(s2, "foobarblah", "bar", String.class);

        PadStringRight ps = new PadStringRight();
        ps.setPadCharacter("X");
        ps.setPadCount(5);
        this.runActionTest(ps, "fname", "fnameXXXXX", String.class);

        PadStringLeft pl = new PadStringLeft();
        pl.setPadCharacter("X");
        pl.setPadCount(5);
        this.runActionTest(pl, "fname", "XXXXXfname", String.class);

        /* ref https://github.com/atlasmap/atlasmap/issues/674
        String result = (String) runActionTest(new GenerateUUID(), "fname", null, String.class);
        assertTrue(Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}").matcher(result)
                .matches());
        */
    }

    public Object runActionTest(Action action, String sourceFirstName, Object targetExpected, Class<?> targetClassExpected) throws Exception {
        return this.runActionTestList(Arrays.asList(action), sourceFirstName, targetExpected, targetClassExpected);
    }

    public Object runActionTestList(List<Action> actions, String sourceFirstName, Object targetExpected, Class<?> targetClassExpected) throws Exception {
        System.out.println("Now running test for actions: " + actions);
        System.out.println("Input: " + sourceFirstName);
        System.out.println("Expected output: " + targetExpected);

        Mapping m = new Mapping();
        m.setMappingType(MappingType.MAP);
        m.getInputField().add(this.sourceField);
        m.getOutputField().add(this.targetField);
        if (actions != null) {
            m.getOutputField().get(0).setActions(new Actions());
            m.getOutputField().get(0).getActions().getActions().addAll(actions);
        }

        DataSource src = new DataSource();
        src.setDataSourceType(DataSourceType.SOURCE);
        src.setUri(this.docURI);

        DataSource tgt = new DataSource();
        tgt.setDataSourceType(DataSourceType.TARGET);
        tgt.setUri(this.docURI);

        AtlasMapping atlasMapping = new AtlasMapping();
        atlasMapping.setName("fieldactiontest");
        atlasMapping.setMappings(new Mappings());
        atlasMapping.getMappings().getMapping().add(m);
        atlasMapping.getDataSource().add(src);
        atlasMapping.getDataSource().add(tgt);

        String tmpFile = "target/fieldactions-" + this.getClass().getSimpleName() + "-tmp.txt";
        DefaultAtlasContextFactory.getInstance().getMappingService().saveMappingAsFile(atlasMapping, new File(tmpFile),
                AtlasMappingFormat.XML);

        AtlasContext context = atlasContextFactory.createContext(new File(tmpFile).toURI());
        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(createSource(sourceFirstName));
        context.process(session);

        Object targetActual = session.getDefaultTargetDocument();
        assertNotNull(targetActual);
        targetActual = getTargetValue(targetActual, targetClassExpected);
        if (targetExpected != null) {
            assertEquals(targetExpected, targetActual);
        }

        return targetActual;
    }

    @Test
    public void runLengthTest() throws Exception {
        this.runActionTest(new Length(), "fname", Integer.valueOf(5), Integer.class);
    }

    public abstract Object createSource(String sourceFirstName);

    public abstract Object getTargetValue(Object target, Class<?> targetClassExpected);
}
