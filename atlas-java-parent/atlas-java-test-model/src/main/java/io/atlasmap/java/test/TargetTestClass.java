package io.atlasmap.java.test;

public class TargetTestClass {
	private String name;
	private String fullAddress;
	private String Department;
	private TargetAddress address;
	private TargetContact contact;
	private TargetOrder order;
	private TargetOrderArray orderArray;
	private TestListOrders listOrders;
	private TargetFlatPrimitiveClass primitives;
	private StateEnumClassLong statesLong;
	private StateEnumClassShort statesShort;
	private EmptyComplexField emptyComplexField;
	
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
		return Department;
	}
	public void setDepartment(String department) {
		Department = department;
	}
	public EmptyComplexField getEmptyComplexField() {
		return emptyComplexField;
	}
	public void setEmptyComplexField(EmptyComplexField emptyComplexField) {
		this.emptyComplexField = emptyComplexField;
	}				
}
