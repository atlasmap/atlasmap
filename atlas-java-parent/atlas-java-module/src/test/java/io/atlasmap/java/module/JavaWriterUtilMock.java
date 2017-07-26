package io.atlasmap.java.module;

import java.util.List;

import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.PathUtil;
import io.atlasmap.core.PathUtil.SegmentContext;
import io.atlasmap.java.test.BaseOrder;
import io.atlasmap.java.test.StateEnumClassLong;
import io.atlasmap.java.test.TargetAddress;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.java.test.TargetOrder;
import io.atlasmap.java.test.TargetOrderArray;
import io.atlasmap.java.test.TargetTestClass;
import io.atlasmap.java.test.TestListOrders;
import io.atlasmap.v2.Field;

public class JavaWriterUtilMock extends JavaWriterUtil {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JavaWriterUtilMock.class);
	
	public JavaWriterUtilMock() {
		super(null);
	}	
	
	/**
     * Retrieve a child object (which may be a complex class or collection class) from the given parentObject.  
     * 
     * @param field - provided for convenience, probably not needed here
     * @param ParentObject - the object to find the child on
     * @param segmentContext - the segment of the field's path that references the child object
     */
	@Override
    public Object getObjectFromParent(Field field, Object parentObject, SegmentContext segmentContext) throws AtlasException {
    	String segment = segmentContext.getSegment();
    	if (logger.isDebugEnabled()) {
    		logger.debug("Retrieving child '" + segmentContext.getSegmentPath() + "'.\n\tparentObject: " + parentObject);
    	}
    	
    	if (parentObject == null) {
    		if (logger.isDebugEnabled()) {
        		logger.debug("Cannot find child '" + segmentContext.getSegmentPath() + "', parent is null.");
        	}
    		return null;
    	}
    	
    	// clean up our segment from something like "@addressLine1" to  "addressLine1".
    	// collection segments like "orders[4]" will be cleaned to "orders"
    	String cleanedSegment = PathUtil.cleanPathSegment(segmentContext.getSegment());
    	Object childObject = null;
    	
    	if (parentObject instanceof TargetAddress && "addressLine1".equals(cleanedSegment)) {
    		childObject = ((TargetAddress)parentObject).getAddressLine1();    		
    	} else if ("orders".equals(cleanedSegment) && parentObject instanceof TestListOrders) {
    		childObject = ((TestListOrders)parentObject).getOrders();
    	} else if ("orders".equals(cleanedSegment) && parentObject instanceof TargetOrderArray) {
    		childObject = ((TargetOrderArray)parentObject).getOrders();
    	} else if ("address".equals(cleanedSegment) && parentObject instanceof TargetOrder) {
    		childObject = ((TargetOrder)parentObject).getAddress();
    	} else if ("address".equals(cleanedSegment) && parentObject instanceof TargetTestClass) {
    		 childObject = ((TargetTestClass)parentObject).getAddress();
    	} else if ("listOrders".equals(cleanedSegment) && parentObject instanceof TargetTestClass) {
    		 childObject = ((TargetTestClass)parentObject).getListOrders();
    	} else if ("orderArray".equals(cleanedSegment) && parentObject instanceof TargetTestClass) {
    		 childObject = ((TargetTestClass)parentObject).getOrderArray();
    	} else if ("contact".equals(cleanedSegment) && parentObject instanceof TargetOrder) {
    		 childObject = ((TargetOrder)parentObject).getContact();
    	} else if ("numberOrders".equals(cleanedSegment) && parentObject instanceof TargetOrderArray) {
    		 childObject = ((TargetOrderArray)parentObject).getNumberOrders();
    	} else if ("primitives".equals(cleanedSegment) && parentObject instanceof TargetTestClass) {
    		 childObject = ((TargetTestClass)parentObject).getPrimitives();
    	} else if ("intArrayField".equals(cleanedSegment) && parentObject instanceof TargetFlatPrimitiveClass) {
    		 childObject = ((TargetFlatPrimitiveClass)parentObject).getIntArrayField();
    	} else if ("boxedStringArrayField".equals(cleanedSegment) && parentObject instanceof TargetFlatPrimitiveClass) {
    		 childObject = ((TargetFlatPrimitiveClass)parentObject).getBoxedStringArrayField();
    	} else if ("contact".equals(cleanedSegment) && parentObject instanceof TargetTestClass) {
    		 childObject = ((TargetTestClass)parentObject).getContact();
    	} else if ("nothing".equals(cleanedSegment) && parentObject instanceof TargetTestClass) {
    		childObject = null;
    	} else {
    		String clz = parentObject.getClass().getSimpleName();
    		String getter = "get" + cleanedSegment.substring(0,1).toUpperCase() + cleanedSegment.substring(1);
    		String fix = "} else if (\"" + cleanedSegment + "\".equals(cleanedSegment) && parentObject instanceof " + clz + ") {\n "
    				+ "childObject = ((" + clz + ")parentObject)." + getter + "();";
    		logger.error(fix);
    		throw new AtlasException("Don't know how to handle get object from parent: " + parentObject + ", segment: " + cleanedSegment);
    	}    	
    	
    	//FIXME: Matt, right? an @ here indicates use the getter, or does @ mean access member?
    	boolean useGetter = PathUtil.isAttributeSegment(segment);
    	if (useGetter) {
    		//FIXME: matt, something like this, but with reflection and what not    		
    		//childObject =  parentObject.getAddressLine1();
    		
    	} else {
    		//FIXME: Matt, something lik this, but with reflection
    		//childObject =  parentObject.addressLine1;
    	}
    	
    	if (logger.isDebugEnabled()) {
    		if (childObject == null) {
    			logger.debug("Could not find child object for path: " + segmentContext.getSegmentPath());
    		} else {
    			logger.debug("Found child object for path '" + segmentContext.getSegmentPath() + "': " + childObject);
    		}
    	}
   
    	//TODO: matt, should we throw an exception here if null?
    	return childObject;
    }
    
	/**
     * Set the given object within the parentObject. 
     * 
     * @param field - provided if we need it, I don't think we will since we already have the value in hand?
     * @param segmentContext - current segment for the field's path, this will be the last segment in the path.
     * @param parentObject - the object we're setting the value in
     * @param childObject - the childObject to set
     */
	@SuppressWarnings({ "unchecked" })
	@Override
    public void setObjectOnParent(Field field, SegmentContext segmentContext, Object parentObject, Object childObject) throws AtlasException {
    	String segment = segmentContext.getSegment();
    	if (logger.isDebugEnabled()) {
    		logger.debug("Setting object '" + segmentContext.getSegmentPath() + "'.\n\tchildObject: " + childObject + "\n\tparentObject: " + parentObject);
    	}
    	
    	//now the cleanedSegment is a cleaned name such as "addressLine1"
    	String cleanedSegment = PathUtil.cleanPathSegment(segment);    	
    	
    	if ("addressLine1".equals(cleanedSegment) && parentObject instanceof TargetAddress) {
    		((TargetAddress) parentObject).setAddressLine1((String)childObject);
    	} else if ("addressLine1".equals(cleanedSegment) && parentObject instanceof TargetAddress) {
        		((TargetAddress) parentObject).setAddressLine1((String)childObject);
    	} else if ("addressLine2".equals(cleanedSegment) && parentObject instanceof TargetAddress) {
    		((TargetAddress) parentObject).setAddressLine2((String)childObject);
    	} else if ("city".equals(cleanedSegment) && parentObject instanceof TargetAddress) {
    		((TargetAddress) parentObject).setCity((String)childObject);
    	} else if ("state".equals(cleanedSegment) && parentObject instanceof TargetAddress) {
    		((TargetAddress) parentObject).setState((String)childObject);
    	} else if ("zipCode".equals(cleanedSegment) && parentObject instanceof TargetAddress) {
    		((TargetAddress) parentObject).setZipCode((String)childObject);
    	} else if ("orders".equals(cleanedSegment) && parentObject instanceof TestListOrders) {
    		((TestListOrders) parentObject).setOrders((List<BaseOrder>)childObject);
    	} else if ("orders".equals(cleanedSegment) && parentObject instanceof TargetOrderArray) {
    		((TargetOrderArray) parentObject).setOrders((TargetOrder[])childObject);
    	} else if ("address".equals(cleanedSegment) && parentObject instanceof TargetOrder) {
    		((TargetOrder) parentObject).setAddress((TargetAddress)childObject);
    	} else if ("name".equals(cleanedSegment) && parentObject instanceof TargetTestClass) {
    		 ((TargetTestClass) parentObject).setName((String)childObject);    	
    	} else if ("address".equals(cleanedSegment) && parentObject instanceof TargetTestClass) {
    		 ((TargetTestClass) parentObject).setAddress((TargetAddress)childObject);
    	} else if ("listOrders".equals(cleanedSegment) && parentObject instanceof TargetTestClass) {
    		 ((TargetTestClass) parentObject).setListOrders((TestListOrders)childObject);
    	} else if ("orderId".equals(cleanedSegment) && parentObject instanceof TargetOrder) {
    		 ((TargetOrder) parentObject).setOrderId((Integer)childObject);
    	} else if ("orderArray".equals(cleanedSegment) && parentObject instanceof TargetTestClass) {
    		 ((TargetTestClass) parentObject).setOrderArray((TargetOrderArray)childObject);
    	} else if ("numberOrders".equals(cleanedSegment) && parentObject instanceof TargetOrderArray) {
    		 ((TargetOrderArray) parentObject).setNumberOrders((Integer)childObject);
    	} else if ("contact".equals(cleanedSegment) && parentObject instanceof TargetOrder) {
    		 ((TargetOrder) parentObject).setContact((TargetContact)childObject);
    	} else if ("firstName".equals(cleanedSegment) && parentObject instanceof TargetContact) {
    		 ((TargetContact) parentObject).setFirstName((String)childObject);
    	} else if ("lastName".equals(cleanedSegment) && parentObject instanceof TargetContact) {
    		 ((TargetContact) parentObject).setLastName((String)childObject);
    	 } else if ("primitives".equals(cleanedSegment) && parentObject instanceof TargetTestClass) {
    		 ((TargetTestClass) parentObject).setPrimitives((TargetFlatPrimitiveClass)childObject);
    	 } else if ("intArrayField".equals(cleanedSegment) && parentObject instanceof TargetFlatPrimitiveClass) {
    		 ((TargetFlatPrimitiveClass) parentObject).setIntArrayField((int[])childObject);
    	 } else if ("boxedStringArrayField".equals(cleanedSegment) && parentObject instanceof TargetFlatPrimitiveClass) {
    		 ((TargetFlatPrimitiveClass) parentObject).setBoxedStringArrayField((String[])childObject);
    	 } else if ("statesLong".equals(cleanedSegment) && parentObject instanceof TargetTestClass) {
    		 ((TargetTestClass) parentObject).setStatesLong((StateEnumClassLong)childObject);
    	}  else {
    		String clz = parentObject.getClass().getSimpleName();
    		String clz2 = childObject.getClass().getSimpleName();
    		String setter = "set" + cleanedSegment.substring(0,1).toUpperCase() + cleanedSegment.substring(1);
    		String fix = "} else if (\"" + cleanedSegment + "\".equals(cleanedSegment) && parentObject instanceof " + clz + ") {\n "
    				+ "((" + clz + ") parentObject)." + setter + "((" + clz2 + ")childObject);";
    		logger.error(fix);
    		throw new AtlasException("FIX: " + fix);
    	}
    	    	    
		if (logger.isDebugEnabled()) {
			logger.debug("Object after value written: " + parentObject);
		}
    }     
}
