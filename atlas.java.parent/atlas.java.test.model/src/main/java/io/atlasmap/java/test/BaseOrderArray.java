package io.atlasmap.java.test;

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
    
}
