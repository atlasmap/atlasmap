package org.apache.camel.component.atlasmap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;

public class AtlasEndpointTest {
    private static final Logger LOG = LoggerFactory.getLogger(AtlasEndpointTest.class);

    @Test
    public void testNoDataSource() throws Exception {
        perform(new ArrayList<>(), null, null, false);
    }

    @Test
    public void testDocId() throws Exception {
        List<DataSource> ds = new ArrayList<>();
        DataSource source = new DataSource();
        source.setDataSourceType(DataSourceType.SOURCE);
        source.setId("my-source-doc");
        ds.add(source);
        DataSource target = new DataSource();
        target.setDataSourceType(DataSourceType.TARGET);
        target.setId("my-target-doc");
        ds.add(target);
        perform(ds, "my-source-doc", "my-target-doc", false);
    }

    @Test(expected = ComparisonFailure.class)
    public void noConversionIfNoDataSource() throws Exception {
        perform(new ArrayList<>(), null, null, true);
    }

    @Test(expected = ComparisonFailure.class)
    public void noConversionIfJavaDataSource() throws Exception {
        final List<DataSource> dataSources = new ArrayList<>();
        final DataSource dataSource = new DataSource();
        dataSource.setDataSourceType(DataSourceType.SOURCE);
        dataSource.setUri("atlas:java:some.Type");
        dataSources.add(dataSource);
        perform(dataSources, null, null, true);
    }

    @Test
    public void doConversionIfJsonDataSource() throws Exception {
        final List<DataSource> dataSources = new ArrayList<>();
        final DataSource dataSource = new DataSource();
        dataSource.setDataSourceType(DataSourceType.SOURCE);
        dataSource.setUri("atlas:json:SomeType");
        dataSources.add(dataSource);
        perform(dataSources, null, null, true);
    }

    @Test(expected = ComparisonFailure.class)
    public void noConversionIfJsonTargetDataSource() throws Exception {
        final List<DataSource> dataSources = new ArrayList<>();
        final DataSource dataSource = new DataSource();
        dataSource.setDataSourceType(DataSourceType.TARGET);
        dataSource.setUri("atlas:json:SomeType");
        dataSources.add(dataSource);
        perform(dataSources, null, null, true);
    }

    @Test
    public void doConversionIfXmlDataSource() throws Exception {
        final List<DataSource> dataSources = new ArrayList<>();
        final DataSource dataSource = new DataSource();
        dataSource.setDataSourceType(DataSourceType.SOURCE);
        dataSource.setUri("atlas:xml:SomeType");
        dataSources.add(dataSource);
        perform(dataSources, null, null, true);
    }

    @Test(expected = ComparisonFailure.class)
    public void noConversionIfXmlTargetDataSource() throws Exception {
        final List<DataSource> dataSources = new ArrayList<>();
        final DataSource dataSource = new DataSource();
        dataSource.setDataSourceType(DataSourceType.TARGET);
        dataSource.setUri("atlas:xml:SomeType");
        dataSources.add(dataSource);
        perform(dataSources, null, null, true);
    }

    private void perform(List<DataSource> dataSources, String sourceDocId, String targetDocId, boolean fromStream) throws Exception {
        final AtlasMapping mapping = new AtlasMapping();
        mapping.getDataSource().addAll(dataSources);
        final AtlasContext context = spy(AtlasContext.class);
        final AtlasSession session = spy(AtlasSession.class);
        when(context.createSession()).thenReturn(session);
        when(session.getMapping()).thenReturn(mapping);
        when(session.getAudits()).thenReturn(new Audits());
        final AtlasEndpoint endpoint = new AtlasEndpoint("atlas:test.xml", new AtlasComponent(), "test.xml");
        endpoint.setAtlasContext(context);
        final Exchange exchange = spy(Exchange.class);
        final Message inMessage = spy(Message.class);
        when(inMessage.getBody()).thenReturn(fromStream ? new ByteArrayInputStream("{test}".getBytes()) : "{test}");
        when(inMessage.getBody(String.class)).thenReturn("{test}");
        when(exchange.getIn()).thenReturn(inMessage);
        if (sourceDocId == null) {
            doAnswer(new Answer<Void>() {
                public Void answer(InvocationOnMock invocation) {
                    LOG.debug("setDefaultSourceDocument({})", (String)invocation.getArgument(0).toString());
                    assertEquals("{test}", invocation.getArgument(0).toString());
                    return null;
                }
            }).when(session).setDefaultSourceDocument(any());
        } else {
            doAnswer(new Answer<Void>() {
                public Void answer(InvocationOnMock invocation) {
                    LOG.debug("setSourceDocument({}, {})", invocation.getArgument(0), invocation.getArgument(1));
                    assertEquals(sourceDocId, invocation.getArgument(0));
                    assertEquals("{test}", invocation.getArgument(1));
                    return null;
                }
            }).when(session).setSourceDocument(any(), any());
        }
        final Message outMessage = spy(Message.class);
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                LOG.debug("setBody({})", (String)invocation.getArgument(0));
                assertEquals("<target/>", invocation.getArgument(0));
                return null;
            }
        }).when(outMessage).setBody(any());
        doNothing().when(outMessage).setHeaders(any());
        doNothing().when(outMessage).setAttachments(any());
        if (targetDocId == null) {
            when(session.getDefaultTargetDocument()).thenAnswer(new Answer<Object>() {
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    LOG.debug("getDefaultTargetDocument()");
                    return "<target/>";
                }
            });
        } else {
            when(session.getTargetDocument(any())).thenAnswer(new Answer<Object>() {
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    LOG.debug("getTargetDocument({})", (String)invocation.getArgument(0));
                    assertEquals(targetDocId, invocation.getArgument(0));
                    return "<target/>";
                }
            });
        }
        when(exchange.getOut()).thenReturn(outMessage);
        endpoint.onExchange(exchange);
    }

}
