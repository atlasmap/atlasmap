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

        public String getSomeField() {
            return someField;
        }
        public void setSomeField(String someField) {
            this.someField = someField;
        }

        public SomeNestedClass() {

        }

        public SomeNestedClass(String someField) {
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
    }

    private String someField;
    private SomeNestedClass[] someArray;
    private SomeNestedClass[] someRenamedArray;

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
}
