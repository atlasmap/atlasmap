package io.atlasmap.core.v3;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class TestSourceClass {

    private int sourceInteger;
    private double sourceDouble;
    private String sourceFirstName;
    private String sourceLastName;
    private String sourceName;
    private Date sourceDate;
    private List<TestItem> sourceList = new LinkedList<>();

    public int getSourceInteger() {
        return sourceInteger;
    }

    public TestSourceClass setSourceInteger(int sourceInteger) {
        this.sourceInteger = sourceInteger;
        return this;
    }

    public double getSourceDouble() {
        return sourceDouble;
    }

    public TestSourceClass setSourceDouble(double sourceDouble) {
        this.sourceDouble = sourceDouble;
        return this;
    }

    public String getSourceFirstName() {
        return sourceFirstName;
    }

    public TestSourceClass setSourceFirstName(String sourceFirstName) {
        this.sourceFirstName = sourceFirstName;
        return this;
    }

    public String getSourceLastName() {
        return sourceLastName;
    }

    public TestSourceClass setSourceLastName(String sourceLastName) {
        this.sourceLastName = sourceLastName;
        return this;
    }

    public String getSourceName() {
        return sourceName;
    }

    public TestSourceClass setSourceName(String sourceName) {
        this.sourceName = sourceName;
        return this;
    }

    public Date getSourceDate() {
        return sourceDate;
    }

    public TestSourceClass setSourceDate(Date sourceDate) {
        this.sourceDate = sourceDate;
        return this;
    }

    public List<TestItem> getSourceList() {
        return sourceList;
    }
}
