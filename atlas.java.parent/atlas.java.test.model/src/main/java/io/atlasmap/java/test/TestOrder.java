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

import java.io.Serializable;

public class TestOrder implements Serializable {

	private static final long serialVersionUID = 1798627832357917681L;
	
	private TestContact testContact;
	private TestAddress testAddress;
	
	public TestContact getTestContact() {
		return testContact;
	}
	public void setTestContact(TestContact testContact) {
		this.testContact = testContact;
	}
	public TestAddress getTestAddress() {
		return testAddress;
	}
	public void setTestAddress(TestAddress testAddress) {
		this.testAddress = testAddress;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((testAddress == null) ? 0 : testAddress.hashCode());
		result = prime * result + ((testContact == null) ? 0 : testContact.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestOrder other = (TestOrder) obj;
		if (testAddress == null) {
			if (other.testAddress != null)
				return false;
		} else if (!testAddress.equals(other.testAddress))
			return false;
		if (testContact == null) {
			if (other.testContact != null)
				return false;
		} else if (!testContact.equals(other.testContact))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "TestOrder [testContact=" + testContact.toString() + ", testAddress=" + testAddress.toString() + "]";
	}
}
