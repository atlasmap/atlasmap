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

public class TestListOrders {

    private List<BaseOrder> orders;
    private List<Long> orderIds;

    public List<BaseOrder> getOrders() {
        return orders;
    }

    public void setOrders(List<BaseOrder> orders) {
        this.orders = orders;
    }

    public List<Long> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(List<Long> orderIds) {
        this.orderIds = orderIds;
    }

    @Override
    public String toString() {
        return "TestListOrders [orders=" + orders + ", orderIds=" + orderIds + "]";
    }
}
