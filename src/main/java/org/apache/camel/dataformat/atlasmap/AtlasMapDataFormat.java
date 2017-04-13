/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.dataformat.atlasmap;

import java.io.File;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spi.DataFormatName;
import org.apache.camel.support.ServiceSupport;

import com.mediadriver.atlas.api.v2.AtlasContext;
import com.mediadriver.atlas.api.v2.AtlasContextFactory;
import com.mediadriver.atlas.api.v2.AtlasSession;
import com.mediadriver.atlas.core.v2.DefaultAtlasContextFactory;

public class AtlasMapDataFormat extends ServiceSupport implements DataFormat, DataFormatName {

	private String mappingFile = "atlasmapping.xml";
	private AtlasContextFactory atlasContextFactory = null;
	private AtlasContext atlasContext = null;
	
	@Override
	public String getDataFormatName() {
		return "atlasmap";
	}

	@Override
	public void marshal(Exchange exchange, Object inputObject, OutputStream outputStream) throws Exception {
		
		if(atlasContextFactory == null) {
			atlasContextFactory = DefaultAtlasContextFactory.getInstance();
		}
		
		if(atlasContext == null) {
			atlasContext = atlasContextFactory.createContext(new File("src/test/resources/atlasmapping.xml"));
		}
		
		AtlasSession session = atlasContext.createSession();
		session.setInput(inputObject);
		atlasContext.process(session);
		Object target = session.getOutput();
		ObjectOutputStream oos = new ObjectOutputStream(outputStream);
		oos.writeObject(target);
	}

	@Override
	public Object unmarshal(Exchange arg0, InputStream arg1) throws Exception {
		throw new Exception("Unmarshal is not supported");
	}

	@Override
	protected void doStart() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub
		
	}

	public String getMappingFile() {
		return mappingFile;
	}

	public void setMappingFile(String mappingFile) {
		this.mappingFile = mappingFile;
	}

	
}
