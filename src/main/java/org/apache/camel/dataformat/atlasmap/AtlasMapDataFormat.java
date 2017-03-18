package org.apache.camel.dataformat.atlasmap;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spi.DataFormatName;
import org.apache.camel.support.ServiceSupport;

public class AtlasMapDataFormat extends ServiceSupport implements DataFormat, DataFormatName {

	@Override
	public String getDataFormatName() {
		return "atlasmap";
	}

	@Override
	public void marshal(Exchange arg0, Object arg1, OutputStream arg2) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object unmarshal(Exchange arg0, InputStream arg1) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doStart() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
