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
package io.atlasmap.itests.core;

public abstract class BaseClass {

    static class SomeNestedClass {
        private String someField;
        private SomeNestedClass[] someArray;
        private SomeNestedClass[] someRenamedArray;
        private SomeNestedClass[] yetAnotherArray;
        private int someInt = 0;
        private int[] someIntArray;
        private double someDouble = 0.0;
        private double[] someDoubleArray;

        public SomeNestedClass() {

        }

        public SomeNestedClass(String someField) {
            this.someField = someField;
        }

        public String getSomeField() {
            return someField;
        }

        public void setSomeField(String someField) {
            this.someField = someField;
        }

        public SomeNestedClass[] getSomeArray() {
            return someArray;
        }

        public void setSomeArray(SomeNestedClass[] someArray) {
            this.someArray = someArray;
        }

        public SomeNestedClass[] getSomeRenamedArray() {
            return someRenamedArray;
        }

        public void setSomeRenamedArray(SomeNestedClass[] someRenamedArray) {
            this.someRenamedArray = someRenamedArray;
        }

        public SomeNestedClass[] getYetAnotherArray() {
            return yetAnotherArray;
        }

        public void setYetAnotherArray(SomeNestedClass[] yetAnotherArray) {
            this.yetAnotherArray = yetAnotherArray;
        }

        public int getSomeInt() {
            return someInt;
        }

        public void setSomeInt(int someInt) {
            this.someInt = someInt;
        }

        public int[] getSomeIntArray() {
            return someIntArray;
        }

        public void setSomeIntArray(int[] someIntArray) {
            this.someIntArray = someIntArray;
        }

        public double getSomeDouble() {
            return someDouble;
        }

        public void setSomeDouble(double someDouble) {
            this.someDouble = someDouble;
        }

        public double[] getSomeDoubleArray() {
            return someDoubleArray;
        }

        public void setSomeDoubleArray(double[] someDoubleArray) {
            this.someDoubleArray = someDoubleArray;
        }
    }

    private String someField;
    private String someAnotherField;
    private SomeNestedClass[] someArray;
    private SomeNestedClass[] someRenamedArray;
    private SomeNestedClass[] yetAnotherArray;
    private int someInt = 0;
    private int[] someIntArray;
    private double someDouble = 0.0;
    private double[] someDoubleArray;

    public String getSomeField() {
        return someField;
    }

    public void setSomeField(String someField) {
        this.someField = someField;
    }

    public String getSomeAnotherField() {
        return someAnotherField;
    }

    public void setSomeAnotherField(String someAnotherField) {
        this.someAnotherField = someAnotherField;
    }

    public SomeNestedClass[] getSomeArray() {
        return someArray;
    }

    public void setSomeArray(SomeNestedClass[] someArray) {
        this.someArray = someArray;
    }

    public SomeNestedClass[] getSomeRenamedArray() {
        return someRenamedArray;
    }

    public void setSomeRenamedArray(SomeNestedClass[] someRenamedArray) {
        this.someRenamedArray = someRenamedArray;
    }

    public SomeNestedClass[] getYetAnotherArray() {
        return yetAnotherArray;
    }

    public void setYetAnotherArray(SomeNestedClass[] yetAnotherArray) {
        this.yetAnotherArray = yetAnotherArray;
    }

    public int getSomeInt() {
        return someInt;
    }

    public void setSomeInt(int someInt) {
        this.someInt = someInt;
    }

    public int[] getSomeIntArray() {
        return someIntArray;
    }

    public void setSomeIntArray(int[] someIntArray) {
        this.someIntArray = someIntArray;
    }

    public double getSomeDouble() {
        return someDouble;
    }

    public void setSomeDouble(double someDouble) {
        this.someDouble = someDouble;
    }

    public double[] getSomeDoubleArray() {
        return someDoubleArray;
    }

    public void setSomeDoubleArray(double[] someDoubleArray) {
        this.someDoubleArray = someDoubleArray;
    }
}
