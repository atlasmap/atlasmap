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
