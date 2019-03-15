package io.atlasmap.itests.concurrency;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import twitter4j.Status;
import twitter4j.User;

@Ignore(value = "Integration Test")
public class ConcurrencyChaosMonkeyTest {

    private static final Logger LOG = LoggerFactory.getLogger(ConcurrencyChaosMonkeyTest.class);
    private DefaultAtlasContextFactory atlasContextFactory = null;

    @Before
    public void setUp() {
        atlasContextFactory = DefaultAtlasContextFactory.getInstance();
    }

    @After
    public void tearDown() {
        atlasContextFactory = null;
    }

    // one thread, many contexts
    @Test
    public void chaosMonkeyTestManyContexts() throws Exception {
        long startTime = System.nanoTime();

        URI mappingURI = generateMappingURI();
        Status twitterStatus = generateTwitterStatus();
        for (int i = 0; i < 256; i++) {

            Thread chaosMonkeyThread = new Thread("ChaosMonkeyThread-" + i) {
                public void run() {
                    LOG.info(this.getName() + " starting.");

                    for (int j = 0; j < 100000; j++) {

                        try {
                            AtlasContext context = atlasContextFactory.createContext(mappingURI);
                            AtlasSession session = context.createSession();
                            session.setDefaultSourceDocument(twitterStatus);
                            context.process(session);

                            Random rand = new Random(System.currentTimeMillis() % 13);
                            int randSleep;
                            randSleep = rand.nextInt(20000);
                            Thread.sleep(randSleep);

                            context.process(session);

                            Thread.sleep(randSleep);
                        } catch (Throwable e) {
                            LOG.error("ERROR", e);
                        }
                    }

                    LOG.info(this.getName() + " thread completed.");
                }
            };

            chaosMonkeyThread.start();
        }

        Thread.sleep(600000L);
        long difference = System.nanoTime() - startTime;

        LOG.info(String.format("Total time: %d minutes to process 100000 mappings with one context per execution",
                TimeUnit.NANOSECONDS.toMinutes(difference)));

    }

    // many threads, one context
    @Test
    public void chaosMonkeyTestManyThreads() throws Exception {
        long startTime = System.nanoTime();

        URI mappingURI = generateMappingURI();
        Status twitterStatus = generateTwitterStatus();
        AtlasContext context = atlasContextFactory.createContext(mappingURI);
        for (int i = 0; i < 256; i++) {
            Thread chaosMonkeyThread = new Thread("ChaosMonkeyThread-" + i) {
                public void run() {
                    LOG.info(this.getName() + " starting.");
                    for (int j = 0; j < 100000; j++) {

                        try {

                            AtlasSession session = context.createSession();
                            session.setDefaultSourceDocument(twitterStatus);
                            context.process(session);

                            Random rand = new Random(System.currentTimeMillis() % 13);
                            int randSleep;
                            randSleep = rand.nextInt(20000);
                            Thread.sleep(randSleep);

                            context.process(session);

                            Thread.sleep(randSleep);
                        } catch (Throwable e) {
                            LOG.error("ERROR", e);
                        }
                    }

                    LOG.info(this.getName() + " thread completed.");
                }
            };

            chaosMonkeyThread.start();
        }

        Thread.sleep(600000L);
        long difference = System.nanoTime() - startTime;

        LOG.info(String.format(
                "Total time: %d minutes to process 100000 mappings with one context shared with 256 threads",
                TimeUnit.NANOSECONDS.toMinutes(difference)));

    }

    protected URI generateMappingURI() throws Exception {
        AtlasMapping mapping = AtlasModelFactory.createAtlasMapping();

        mapping.setName("mockMapping");

        DataSource src = new DataSource();
        src.setDataSourceType(DataSourceType.SOURCE);
        src.setUri("atlas:java?className=twitter4j.Status");

        DataSource tgt = new DataSource();
        tgt.setDataSourceType(DataSourceType.TARGET);
        tgt.setUri("atlas:java?className=org.apache.camel.salesforce.dto.Contact");

        mapping.getDataSource().add(src);
        mapping.getDataSource().add(tgt);

        Mapping sepMapping = AtlasModelFactory.createMapping(MappingType.SEPARATE);
        JavaField jNameField = new JavaField();
        jNameField.setName("Name");
        jNameField.setPath("User.name");
        jNameField.setGetMethod("getName");
        jNameField.setFieldType(FieldType.STRING);

        JavaField jFirstNameField = new JavaField();
        jFirstNameField.setName("FirstName");
        jFirstNameField.setPath("FirstName");
        jFirstNameField.setSetMethod("setFirstName");
        jFirstNameField.setFieldType(FieldType.STRING);
        jFirstNameField.setIndex(0);

        JavaField jLastNameField = new JavaField();
        jLastNameField.setName("LastName");
        jLastNameField.setPath("LastName");
        jLastNameField.setSetMethod("setLastName");
        jLastNameField.setFieldType(FieldType.STRING);
        jLastNameField.setIndex(1);

        sepMapping.getInputField().add(jNameField);
        sepMapping.getOutputField().add(jFirstNameField);
        sepMapping.getOutputField().add(jLastNameField);
        mapping.getMappings().getMapping().add(sepMapping);

        Mapping textDescMapping = AtlasModelFactory.createMapping(MappingType.MAP);
        JavaField jTextField = new JavaField();
        jTextField.setName("Text");
        jTextField.setPath("Text");
        jTextField.setGetMethod("getText");
        jTextField.setFieldType(FieldType.STRING);

        JavaField jDescField = new JavaField();
        jDescField.setName("Description");
        jDescField.setPath("description");
        jDescField.setSetMethod("setDescription");
        jDescField.setFieldType(FieldType.STRING);

        textDescMapping.getInputField().add(jTextField);
        textDescMapping.getOutputField().add(jDescField);
        mapping.getMappings().getMapping().add(textDescMapping);

        Mapping screenTitleMapping = AtlasModelFactory.createMapping(MappingType.MAP);
        JavaField jScreenField = new JavaField();
        jScreenField.setName("ScreenName");
        jScreenField.setPath("User.screenName");
        jScreenField.setGetMethod("getScreenName");
        jScreenField.setFieldType(FieldType.STRING);

        JavaField jTitleField = new JavaField();
        jTitleField.setName("Title");
        jTitleField.setPath("Title");
        jTitleField.setSetMethod("setTitle");
        jTitleField.setFieldType(FieldType.STRING);

        screenTitleMapping.getInputField().add(jScreenField);
        screenTitleMapping.getOutputField().add(jTitleField);
        mapping.getMappings().getMapping().add(screenTitleMapping);

        File mappingFile = new File("target/junit-atlasmapping.xml");
        atlasContextFactory.getMappingService().saveMappingAsFile(mapping, mappingFile);

        return mappingFile.toURI();
    }

    protected Status generateTwitterStatus() {
        Status status = mock(Status.class);
        User user = mock(User.class);
        when(user.getName()).thenReturn("Bob Vila");
        when(user.getScreenName()).thenReturn("bobvila1982");
        when(status.getUser()).thenReturn(user);
        when(status.getText()).thenReturn("Let's build a house!");
        return status;
    }
}
