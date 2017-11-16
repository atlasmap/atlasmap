package org.apache.camel.component.atlasmap;

import static org.apache.camel.component.atlasmap.AtlasEndpoint.isSourceXmlOrJson;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;

public class AtlasEndpointTest {

    @Test
    public void shouldDetectIfSourceIsXmlOrJson() {
        final AtlasMapping mapping = new AtlasMapping();

        assertFalse("With no datasources no XML or JSON sources should be found", isSourceXmlOrJson(mapping));

        final List<DataSource> dataSources = mapping.getDataSource();
        final DataSource dataSource = new DataSource();
        dataSource.setDataSourceType(DataSourceType.SOURCE);
        dataSources.add(dataSource);

        assertFalse("Source datasource without uri should not be detected as XML or JSON", isSourceXmlOrJson(mapping));

        dataSource.setUri("atlas:java:some.Type");
        assertFalse("Java source datasource should not be detected as XML or JSON", isSourceXmlOrJson(mapping));

        dataSource.setUri("atlas:json:SomeType");
        assertTrue("JSON data source should be detected", isSourceXmlOrJson(mapping));

        dataSource.setDataSourceType(DataSourceType.TARGET);
        assertFalse("JSON data source target should not be detected as XML or JSON", isSourceXmlOrJson(mapping));

        dataSource.setDataSourceType(DataSourceType.SOURCE);
        dataSource.setUri("atlas:xml?complexType=ns:SomeElement");
        assertTrue("XML data source should be detected", isSourceXmlOrJson(mapping));

        dataSource.setDataSourceType(DataSourceType.TARGET);
        assertFalse("XML data source target should not be detected as XML or JSON", isSourceXmlOrJson(mapping));
    }
}
