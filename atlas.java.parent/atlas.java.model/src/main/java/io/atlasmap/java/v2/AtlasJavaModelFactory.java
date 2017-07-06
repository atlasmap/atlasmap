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
package io.atlasmap.java.v2;

public class AtlasJavaModelFactory {
	
	public static final String URI_FORMAT = "atlas:java?className=%s";

	public static JavaClass createJavaClass() {
		JavaClass javaClass = new JavaClass();
		javaClass.setJavaEnumFields(new JavaEnumFields());
		javaClass.setJavaFields(new JavaFields());
		return javaClass;
	}
	
	public static JavaField createJavaField() {
		JavaField javaField = new JavaField();
		javaField.setModifiers(new ModifierList());
		return javaField;
	}
}
