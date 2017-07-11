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
package io.atlasmap.v2;

import org.junit.Test;
import io.atlasmap.v2.AtlasModelFactory;
import static org.junit.Assert.*;

public class AtlasModelFactoryTest {
	
	@Test
	public void testCreateSeparateMapping() {
		Mapping fm = AtlasModelFactory.createMapping(MappingType.SEPARATE);
		validateMapping(fm, MappingType.SEPARATE);
	}
	
	@Test
	public void testCreateMapMapping() {
        Mapping fm = AtlasModelFactory.createMapping(MappingType.MAP);
        validateMapping(fm, MappingType.MAP);
	}
	
	@Test
	public void testCreateCombineMapping() {
        Mapping fm = AtlasModelFactory.createMapping(MappingType.COMBINE);
        validateMapping(fm, MappingType.COMBINE);
	}
	
	@Test
	public void testCreateLookupMapping() {
        Mapping fm = AtlasModelFactory.createMapping(MappingType.LOOKUP);
        validateMapping(fm, MappingType.LOOKUP);
	}
	
	protected void validateMapping(Mapping fm, MappingType type) {
	    assertNotNull(fm);
	    assertNotNull(fm.getMappingType());
	    assertEquals(type, fm.getMappingType());
	    assertNull(fm.getAlias());
	    assertNull(fm.getDescription());
	    assertNotNull(fm.getOutputField());
	    assertNotNull(fm.getInputField());
	    assertEquals(new Integer(0), new Integer(fm.getOutputField().size()));
	    assertEquals(new Integer(0), new Integer(fm.getOutputField().size()));
	}
}
