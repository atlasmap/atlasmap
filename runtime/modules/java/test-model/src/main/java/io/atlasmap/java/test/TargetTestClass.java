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

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

public class TargetTestClass {
    private String name;
    private String fullAddress;
    private String department;
    private TargetAddress address;
    private TargetContact contact;
    private TargetOrder order;
    private TargetOrderArray orderArray;
    private TestListOrders listOrders;
    private TargetFlatPrimitiveClass primitives;
    private StateEnumClassLong statesLong;
    private StateEnumClassShort statesShort;
    private EmptyComplexField emptyComplexField;
    private LinkedList<TargetContact> contactList;
    private TargetContact[] contactArray;
    private Date created;

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public TargetAddress getAddress() {
        return address;
    }

    public void setAddress(TargetAddress address) {
        this.address = address;
    }

    public TargetContact getContact() {
        return contact;
    }

    public void setContact(TargetContact contact) {
        this.contact = contact;
    }

    public TargetOrder getOrder() {
        return order;
    }

    public void setOrder(TargetOrder order) {
        this.order = order;
    }

    public TargetOrderArray getOrderArray() {
        return orderArray;
    }

    public void setOrderArray(TargetOrderArray orderArray) {
        this.orderArray = orderArray;
    }

    public TestListOrders getListOrders() {
        return listOrders;
    }

    public void setListOrders(TestListOrders listOrders) {
        this.listOrders = listOrders;
    }

    public TargetFlatPrimitiveClass getPrimitives() {
        return primitives;
    }

    public void setPrimitives(TargetFlatPrimitiveClass primitives) {
        this.primitives = primitives;
    }

    public StateEnumClassLong getStatesLong() {
        return statesLong;
    }

    public void setStatesLong(StateEnumClassLong statesLong) {
        this.statesLong = statesLong;
    }

    public StateEnumClassShort getStatesShort() {
        return statesShort;
    }

    public void setStatesShort(StateEnumClassShort statesShort) {
        this.statesShort = statesShort;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public EmptyComplexField getEmptyComplexField() {
        return emptyComplexField;
    }

    public void setEmptyComplexField(EmptyComplexField emptyComplexField) {
        this.emptyComplexField = emptyComplexField;
    }

    public LinkedList<TargetContact> getContactList() {
        return contactList;
    }

    public void setContactList(LinkedList<TargetContact> contactList) {
        this.contactList = contactList;
    }

    public TargetContact[] getContactArray() {
        return contactArray;
    }

    public void setContactArray(TargetContact[] contactArray) {
        this.contactArray = contactArray;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((department == null) ? 0 : department.hashCode());
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + ((contact == null) ? 0 : contact.hashCode());
        result = prime * result + Arrays.hashCode(contactArray);
        result = prime * result + ((contactList == null) ? 0 : contactList.hashCode());
        result = prime * result + ((emptyComplexField == null) ? 0 : emptyComplexField.hashCode());
        result = prime * result + ((fullAddress == null) ? 0 : fullAddress.hashCode());
        result = prime * result + ((listOrders == null) ? 0 : listOrders.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((order == null) ? 0 : order.hashCode());
        result = prime * result + ((orderArray == null) ? 0 : orderArray.hashCode());
        result = prime * result + ((primitives == null) ? 0 : primitives.hashCode());
        result = prime * result + ((statesLong == null) ? 0 : statesLong.hashCode());
        result = prime * result + ((statesShort == null) ? 0 : statesShort.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        TargetTestClass other = (TargetTestClass) obj;
        if (department == null) {
            if (other.department != null) {
                return false;
            }
        } else if (!department.equals(other.department)) {
            return false;
        }
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
        if (contact == null) {
            if (other.contact != null) {
                return false;
            }
        } else if (!contact.equals(other.contact)) {
            return false;
        }
        if (!Arrays.equals(contactArray, other.contactArray)) {
            return false;
        }
        if (contactList == null) {
            if (other.contactList != null) {
                return false;
            }
        } else if (!contactList.equals(other.contactList)) {
            return false;
        }
        if (emptyComplexField == null) {
            if (other.emptyComplexField != null) {
                return false;
            }
        } else if (!emptyComplexField.equals(other.emptyComplexField)) {
            return false;
        }
        if (fullAddress == null) {
            if (other.fullAddress != null) {
                return false;
            }
        } else if (!fullAddress.equals(other.fullAddress)) {
            return false;
        }
        if (listOrders == null) {
            if (other.listOrders != null) {
                return false;
            }
        } else if (!listOrders.equals(other.listOrders)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (order == null) {
            if (other.order != null) {
                return false;
            }
        } else if (!order.equals(other.order)) {
            return false;
        }
        if (orderArray == null) {
            if (other.orderArray != null) {
                return false;
            }
        } else if (!orderArray.equals(other.orderArray)) {
            return false;
        }
        if (primitives == null) {
            if (other.primitives != null) {
                return false;
            }
        } else if (!primitives.equals(other.primitives)) {
            return false;
        }
        if (statesLong != other.statesLong) {
            return false;
        }

        return statesShort != other.statesShort;
    }

    @Override
    public String toString() {
        return "TargetTestClass [name=" + name + ", fullAddress=" + fullAddress + ", Department=" + department
                + ", address=" + address + ", contact=" + contact + ", order=" + order + ", orderArray=" + orderArray
                + ", listOrders=" + listOrders + ", primitives=" + primitives + ", statesLong=" + statesLong
                + ", statesShort=" + statesShort + ", emptyComplexField=" + emptyComplexField + ", contactsList="
                + contactList + ", contactsArray=" + Arrays.toString(contactArray) + "]";
    }
}
