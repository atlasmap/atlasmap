/*
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

import java.util.List;

public class SourceCollectionsClass extends BaseCollectionsClass {

    private static final long serialVersionUID = 4700965723489983082L;

    private List<SourceContact> contactList;
    private List<SourceAddress> addressList;
    private List<SourceOrder> orderList;

    public List<SourceContact> getContactList() {
        return contactList;
    }

    public void setContactList(List<SourceContact> contactList) {
        this.contactList = contactList;
    }

    public List<SourceAddress> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<SourceAddress> addressList) {
        this.addressList = addressList;
    }

    public List<SourceOrder> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<SourceOrder> orderList) {
        this.orderList = orderList;
    }

}
