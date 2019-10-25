package io.atlasmap.maven.test;

public class EnumTestClass {

    private StateEnumClassLong statesLong;
    private StateEnumClassShort statesShort;

    public enum StateEnumClassLong {
        Alabama, Arizona, California, Colorado, Florida, Massachusetts, NewHampshire, NewYork, Texas, Virginia
    }

    public enum StateEnumClassShort {
        AL, AZ, CA, CO, FL, MA, NH, NY, TX, VA
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

}

