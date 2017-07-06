package io.atlasmap.java.test;

import java.util.List;

public abstract class BaseOrderList {

    protected List<BaseOrder> orders;
    protected Integer numberOrders;
    protected Integer orderBatchNumber;

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

    public List<BaseOrder> getOrders() {
        return orders;
    }

    public void setOrders(List<BaseOrder> orders) {
        this.orders = orders;
    }
    
}
