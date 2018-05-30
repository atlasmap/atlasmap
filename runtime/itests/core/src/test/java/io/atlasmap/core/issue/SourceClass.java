package io.atlasmap.core.issue;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class SourceClass {

    private int sourceInteger;
    private String sourceFirstName;
    private String sourceLastName;
    private String sourceName;
    private Date sourceDate;
    private List<Item> sourceList = new LinkedList<>();

    public int getSourceInteger() {
        return sourceInteger;
    }

    public SourceClass setSourceInteger(int sourceInteger) {
        this.sourceInteger = sourceInteger;
        return this;
    }

    public String getSourceFirstName() {
        return sourceFirstName;
    }

    public SourceClass setSourceFirstName(String sourceFirstName) {
        this.sourceFirstName = sourceFirstName;
        return this;
    }

    public String getSourceLastName() {
        return sourceLastName;
    }

    public SourceClass setSourceLastName(String sourceLastName) {
        this.sourceLastName = sourceLastName;
        return this;
    }

    public String getSourceName() {
        return sourceName;
    }

    public SourceClass setSourceName(String sourceName) {
        this.sourceName = sourceName;
        return this;
    }

    public Date getSourceDate() {
        return sourceDate;
    }

    public SourceClass setSourceDate(Date sourceDate) {
        this.sourceDate = sourceDate;
        return this;
    }

    public List<Item> getSourceList() {
        return sourceList;
    }

    public SourceClass setSourceList(List<Item> sourceList) {
        this.sourceList = sourceList;
        return this;
    }

}
