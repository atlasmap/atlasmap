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
package io.atlasmap.api;

public class AtlasValidationException extends AtlasException {

	private static final long serialVersionUID = 6537018220259702613L;
	
	public AtlasValidationException() { super(); }
	public AtlasValidationException(String message, Throwable cause) { super(message, cause); }
	public AtlasValidationException(String message) { super(message); }
	public AtlasValidationException(Throwable cause) { super(cause); }
}
