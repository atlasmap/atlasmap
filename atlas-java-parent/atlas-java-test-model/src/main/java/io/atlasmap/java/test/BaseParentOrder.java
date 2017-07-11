package io.atlasmap.java.test;

public class BaseParentOrder {
    private BaseOrder order;
    private Integer parentOrderId;
    private Integer agentId;
    
    public BaseOrder getOrder() {
        return order;
    }
    public void setOrder(BaseOrder order) {
        this.order = order;
    }
    public Integer getParentOrderId() {
        return parentOrderId;
    }
    public void setParentOrderId(Integer parentOrderId) {
        this.parentOrderId = parentOrderId;
    }
    public Integer getAgentId() {
        return agentId;
    }
    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }
    
}
