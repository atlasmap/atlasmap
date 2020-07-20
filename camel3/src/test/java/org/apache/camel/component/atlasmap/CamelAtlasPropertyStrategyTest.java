package org.apache.camel.component.atlasmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.DefaultMessage;
import org.junit.Test;

import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasSession;
import io.atlasmap.v2.PropertyField;

public class CamelAtlasPropertyStrategyTest {

    @Test
    public void testRead() throws Exception {
        CamelAtlasPropertyStrategy strategy = new CamelAtlasPropertyStrategy();
        DefaultCamelContext context = new DefaultCamelContext();
        DefaultExchange exchange = new DefaultExchange(context);
        exchange.setProperty("testProp", "testProp-exchangeProperty");
        strategy.setExchange(exchange);
        DefaultMessage currentMessage = new DefaultMessage(exchange);
        currentMessage.setHeader("testProp", "testProp-currentMessage");
        strategy.setCurrentSourceMessage(currentMessage);
        DefaultMessage message1 = new DefaultMessage(exchange);
        message1.setHeader("testProp", "testProp-message1");
        strategy.setSourceMessage("Doc1", message1);
        DefaultMessage message2 = new DefaultMessage(exchange);
        message2.setHeader("testProp", "testProp-message2");
        strategy.setSourceMessage("Doc2", message2);
        AtlasSession session = mock(AtlasSession.class);
        when(session.getAtlasPropertyStrategy()).thenReturn(strategy);
        PropertyField propertyField = new PropertyField();
        propertyField.setName("testProp");
        strategy.readProperty(session, propertyField);
        assertEquals("testProp-currentMessage", propertyField.getValue());
        propertyField.setScope(CamelAtlasPropertyStrategy.SCOPE_EXCHANGE_PROPERTY);
        strategy.readProperty(session, propertyField);
        assertEquals("testProp-exchangeProperty", propertyField.getValue());
        propertyField.setScope("Doc1");
        strategy.readProperty(session, propertyField);
        assertEquals("testProp-message1", propertyField.getValue());
        propertyField.setScope("Doc2");
        strategy.readProperty(session, propertyField);
        assertEquals("testProp-message2", propertyField.getValue());
    }

    @Test
    public void testWrite() throws Exception {
        CamelAtlasPropertyStrategy strategy = new CamelAtlasPropertyStrategy();
        DefaultCamelContext context = new DefaultCamelContext();
        DefaultExchange exchange = new DefaultExchange(context);
        strategy.setExchange(exchange);
        DefaultMessage message = new DefaultMessage(exchange);
        strategy.setTargetMessage(message);
        PropertyField propertyField = new PropertyField();
        propertyField.setName("testProp-message");
        propertyField.setValue("testValue");
        AtlasSession session = mock(DefaultAtlasSession.class);
        when(session.getAtlasPropertyStrategy()).thenReturn(strategy);
        strategy.writeProperty(session, propertyField);
        propertyField.setName("testProp-exchange");
        propertyField.setScope(CamelAtlasPropertyStrategy.SCOPE_EXCHANGE_PROPERTY);
        strategy.writeProperty(session, propertyField);
        assertEquals("testValue", message.getHeader("testProp-message"));
        assertNull(message.getHeader("testProp-exchange"));
        assertNull(exchange.getProperty("testProp-message"));
        assertEquals("testValue", exchange.getProperty("testProp-exchange"));
    }

}
