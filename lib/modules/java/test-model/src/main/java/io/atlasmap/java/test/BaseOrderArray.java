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

import java.util.Arrays;

public abstract class BaseOrderArray {

    private BaseOrder[] orders;
    private Integer numberOrders;
    private Integer orderBatchNumber;

    public Integer getOrderBatchNumber() {
        return orderBatchNumber;
    }

    public void setOrderBatchNumber(Integer orderBatchNumber) {
        this.orderBatchNumber = orderBatchNumber;
    }

    public Integer getNumberOrders() {
        return numberOrders;
    }

    public void setNumberOrders(Integer numberOrders) {
        this.numberOrders = numberOrders;
    }

    public BaseOrder[] getOrders() {
        return orders;
    }

    public void setOrders(BaseOrder[] orders) {
        this.orders = orders;
    }

    @Override
    public String toString() {
        return "BaseOrderArray [orders=" + Arrays.toString(orders) + ", numberOrders=" + numberOrders
                + ", orderBatchNumber=" + orderBatchNumber + "]";
    }

}
