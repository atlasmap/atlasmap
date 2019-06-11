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
package io.atlasmap.itests.core.issue;

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

    private String sourceString;
    private List<String> sourceStringList = new LinkedList<>();

    private String sourceHiphenatedInteger;

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

    public String getSourceString() {
        return sourceString;
    }

    public SourceClass setSourceString(String sourceString) {
        this.sourceString = sourceString;
        return this;
    }

    public List<String> getSourceStringList() {
        return sourceStringList;
    }

    public SourceClass setSourceStringList(List<String> sourceStringList) {
        this.sourceStringList = sourceStringList;
        return this;
    }

    public String getSourceHiphenatedInteger() {
        return sourceHiphenatedInteger;
    }

    public SourceClass setSourceHiphenatedInteger(String sourceHiphenatedInteger) {
        this.sourceHiphenatedInteger = sourceHiphenatedInteger;
        return this;
    }

}
