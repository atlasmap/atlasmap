/**
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
package io.atlasmap.java.v2;

import java.util.ArrayList;
import java.util.List;

import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;

public class AtlasJavaModelFactory {

    public static final String URI_FORMAT = "atlas:java?className=%s";

    public static JavaClass createJavaClass() {
        JavaClass javaClass = new JavaClass();
        javaClass.setJavaEnumFields(new JavaEnumFields());
        javaClass.setJavaFields(new JavaFields());
        return javaClass;
    }

    public static JavaField createJavaField() {
        JavaField javaField = new JavaField();
        javaField.setModifiers(new ModifierList());
        return javaField;
    }

    public static FieldGroup cloneFieldGroup(FieldGroup group) {
        FieldGroup clone = AtlasModelFactory.copyFieldGroup(group);
        List<Field> newChildren = new ArrayList<>();
        for (Field child : group.getField()) {
            if (child instanceof FieldGroup) {
                newChildren.add(cloneFieldGroup((FieldGroup)child));
            } else {
                newChildren.add(cloneJavaField(child, true));
            }
        }
        clone.getField().addAll(newChildren);
        return clone;
    }

    public static Field cloneJavaField(Field field, boolean withActions) {
        if (field instanceof FieldGroup) {
            FieldGroup clone = AtlasModelFactory.createFieldGroupFrom(field, withActions);
            for (Field f : ((FieldGroup)field).getField()) {
                clone.getField().add(cloneJavaField(f, withActions));
            }
            return clone;
        }
        Field clone = field instanceof JavaEnumField ? new JavaEnumField() : new JavaField();
        copyField(field, clone, withActions);
        return clone;
    }

    public static void copyField(Field from, Field to, boolean withActions) {
        AtlasModelFactory.copyField(from, to, withActions);

        if (from instanceof JavaField && to instanceof JavaField) {
            JavaField fromJava = (JavaField) from;
            JavaField toJava = (JavaField)to;

            // defined by JavaField
            toJava.setAnnotations(fromJava.getAnnotations());
            toJava.setClassName(fromJava.getClassName());
            toJava.setCollectionClassName(fromJava.getCollectionClassName());
            toJava.setGetMethod(fromJava.getGetMethod());
            toJava.setModifiers(fromJava.getModifiers());
            toJava.setName(fromJava.getName());
            toJava.setParameterizedTypes(fromJava.getParameterizedTypes());
            toJava.setPrimitive(fromJava.isPrimitive());
            toJava.setSetMethod(fromJava.getSetMethod());
            toJava.setSynthetic(fromJava.isSynthetic());
            return;
        }
        if (from instanceof JavaEnumField && to instanceof JavaEnumField) {
            JavaEnumField fromEnum = (JavaEnumField) from;
            JavaEnumField toEnum = (JavaEnumField)to;

            // defined by JavaEnumField
            toEnum.setClassName(fromEnum.getClassName());
            toEnum.setName(fromEnum.getName());
            toEnum.setOrdinal(fromEnum.getOrdinal());
            return;
        }
        // TODO: needs to be atlasexception, but that's not a dependency for some reason
        // on this project.
        throw new RuntimeException(String.format(
                "Unsupported field type to copy: from=%s, to=%s", from, to));
    }

}
