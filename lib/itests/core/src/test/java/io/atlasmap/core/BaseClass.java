package io.atlasmap.core;

public abstract class BaseClass {
    private String someField;
    public String getSomeField() {
        return someField;
    }
    public void setSomeField(String someField) {
        this.someField = someField;
    }
    static class SomeNestedClass {
        private String someField;
        public String getSomeField() {
            return someField;
        }
        public void setSomeField(String someField) {
            this.someField = someField;
        }
    }
}
