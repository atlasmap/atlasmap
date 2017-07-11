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

public abstract class BaseOrder implements Serializable {

	private static final long serialVersionUID = 1798627832357917681L;
	
	private BaseContact contact;
	private BaseAddress address;
	private Integer orderId;
	
	public Integer getOrderId() {
        return orderId;
    }
    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }
    public BaseContact getContact() {
		return contact;
	}
	public void setContact(BaseContact contact) {
		this.contact = contact;
	}
	public BaseAddress getAddress() {
		return address;
	}
	public void setAddress(BaseAddress address) {
		this.address = address;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((contact == null) ? 0 : contact.hashCode());
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
		BaseOrder other = (BaseOrder) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (contact == null) {
			if (other.contact != null)
				return false;
		} else if (!contact.equals(other.contact))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Order [orderId=" + orderId + " contact=" + (contact == null ? "" : contact.toString()) + ", address=" + (address == null ? "" : address.toString()) + "]";
	}
}
