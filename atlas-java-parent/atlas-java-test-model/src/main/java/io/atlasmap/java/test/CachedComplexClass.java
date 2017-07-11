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
package io.atlasmap.java.test;

public class CachedComplexClass {

	private BaseOrder testOrder;
	private BaseAddress primaryAddress;
	private BaseContact primaryContact;
	
	public BaseOrder getTestOrder() {
		return testOrder;
	}
	public void setTestOrder(BaseOrder testOrder) {
		this.testOrder = testOrder;
	}
	public BaseAddress getPrimaryAddress() {
		return primaryAddress;
	}
	public void setPrimaryAddress(BaseAddress primaryAddress) {
		this.primaryAddress = primaryAddress;
	}
	public BaseContact getPrimaryContact() {
		return primaryContact;
	}
	public void setPrimaryContact(BaseContact primaryContact) {
		this.primaryContact = primaryContact;
	}
}
