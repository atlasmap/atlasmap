package io.atlasmap.core.v3;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class TestTargetClass {

    private int targetInteger;
    private double targetDouble;
    private String targetFirstName;
    private String targetLastName;
    private String targetName;
    private Date targetDate;
    private List<TestItem> targetList = new LinkedList<>();

    public int getTargetInteger() {
        return targetInteger;
    }

    public TestTargetClass setTargetInteger(int targetInteger) {
        this.targetInteger = targetInteger;
        return this;
    }

    public double getTargetDouble() {
        return targetDouble;
    }

    public TestTargetClass setTargetDouble(double targetDouble) {
        this.targetDouble = targetDouble;
        return this;
    }

    public String getTargetFirstName() {
        return targetFirstName;
    }

    public TestTargetClass setTargetFirstName(String targetFirstName) {
        this.targetFirstName = targetFirstName;
        return this;
    }

    public String getTargetLastName() {
        return targetLastName;
    }

    public TestTargetClass setTargetLastName(String targetLastName) {
        this.targetLastName = targetLastName;
        return this;
    }

    public String getTargetName() {
        return targetName;
    }

    public TestTargetClass setTargetName(String targetName) {
        this.targetName = targetName;
        return this;
    }

    public Date getTargetDate() {
        return targetDate;
    }

    public TestTargetClass setTargetDate(Date targetDate) {
        this.targetDate = targetDate;
        return this;
    }

    public List<TestItem> getTargetList() {
        return targetList;
    }

    public TestTargetClass setTargetList(List<TestItem> targetList) {
        this.targetList = targetList;
        return this;
    }

}
