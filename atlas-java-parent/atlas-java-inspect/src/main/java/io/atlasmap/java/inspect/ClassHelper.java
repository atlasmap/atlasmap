/**
 * Copyright (C) 2017 Red Hat, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.java.inspect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassHelper {
    
    public static List<String> getterMethodNames(String fieldName) {
        List<String> opts = new ArrayList<String>();
        opts.add(getMethodNameFromFieldName(fieldName));
        opts.add(isMethodNameFromFieldName(fieldName));
        return opts;
    }
    
    public static String setMethodNameFromFieldName(String fieldName) {
        return "set" + StringUtil.capitalizeFirstLetter(fieldName);
    }
    
    public static String getMethodNameFromFieldName(String fieldName) {
        return "get" + StringUtil.capitalizeFirstLetter(fieldName);
    }
    
    public static String isMethodNameFromFieldName(String fieldName) {
        return "is" + StringUtil.capitalizeFirstLetter(fieldName);
    }
    
    public static Method detectGetterMethod(Class<?> clazz, String methodName) throws NoSuchMethodException {

        Method[] methods = clazz.getMethods();
        
        for(Method method : methods) {
            if(method.getName().equals(methodName)) {
                if(method.getParameterCount() == 0) {
                    return method;
                }
            }
        }
        
        throw new NoSuchMethodException(String.format("No matching getter method for class=%s method=%s", clazz.getName(), methodName));    
    }
    
    public static Method detectSetterMethod(Class<?> clazz, String methodName, Class<?> paramType) throws NoSuchMethodException {
        List<Method> candidates = new ArrayList<Method>();
        
        if(paramType == null) {
            Method[] methods = clazz.getMethods();
            for(Method method : methods) {
                if(method.getName().equals(methodName)) {
                    if(method.getParameterCount() == 1) {
                        candidates.add(method);
                    }
                }
            }
            
            if(candidates.size() == 0) {
                throw new NoSuchMethodException(String.format("No matching setter found for class=%s method=%s", clazz.getName(), methodName));
            }
            
            if(candidates.size() == 1) {
                return candidates.get(0);
            } 

            Method getter = null;
            Class<?> returnType = null;
            for(String prefix : Arrays.asList("get", "is")) {
                try {
                    getter = detectGetterMethod(clazz, methodName.replace("set", prefix));
                    returnType = getter.getReturnType();
                    break;
                } catch (NoSuchMethodException nsme) {
                    // System.out.println("\t\t could not find getter " + methodName.replace("set", prefix));
                }
            }
                
            // Solid match
            for(Method candidate : candidates) {
                // Getter and setter w/ same returnType & paramType
                if(candidate.getParameterTypes()[0].equals(returnType)) {
                    return candidate;
                }
            }
                
            // Not as good of a match .. find one with a matching converter
            for(Method candidate : candidates) {
                /* 
                if(candidate.getParameterTypes()[0].equals(String.class)) {
                    return candidate;
                }
                 */
            }
            
            // Yikes! User should specify type, or provide a converter
            throw new NoSuchMethodException(String.format("Unable to auto-detect setter class=%s method=%s", clazz.getName(), methodName));
        }
        return clazz.getMethod(methodName, paramType);
    }
  
    public static Object parentObjectForPath(Object targetObject, JavaPath javaPath) throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if(targetObject == null) {
            return null;
        }
                
        if(javaPath == null) { 
            return targetObject;
        }
    
        if(!javaPath.hasParent() && !javaPath.hasCollection()) {
            return targetObject;
        }
        
        Object parentObject = targetObject;
        JavaPath parentPath = javaPath.getLastSegmentParentPath();
        
        if(parentPath == null) {
            parentPath = javaPath;
        }
        
        for(String parentField : parentPath.getSegments()) {
            List<String> getters = getterMethodNames(JavaPath.cleanPathSegment(parentField));
            Method getterMethod = null;
            for(String getter : getters) {
                try {
                    getterMethod = detectGetterMethod(parentObject.getClass(), getter);
                    break;
                } catch (NoSuchMethodException e) {
                    // exhaust options
                }
            }
            
            if(getterMethod == null) {
                throw new NoSuchMethodException("Unable to detect getter method for " + parentField);
            }
            
            getterMethod.setAccessible(true);
            parentObject = getterMethod.invoke(parentObject);
        }
        
        return parentObject;
    }
}
