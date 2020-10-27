package io.atlasmap.java.inspect;

public class InnerClass {

    private String someString;

    public String getSomeString() {
        return someString;
    }

    public void setSomeString(String s) {
        someString = s;
    }

    public class TheInnerClass {
        private String someInnerString;

        public String getSomeInnerString() {
            return someInnerString;
        }

        public void setSomeInnerString(String s) {
            someInnerString = s;
        }
    }

}
