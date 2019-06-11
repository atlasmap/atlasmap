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
import java.util.List;

public class TargetClass {

    private int targetInteger;
    private String targetFirstName;
    private String targetLastName;
    private String targetName;
    private Date targetDate;
    private List<Item> targetList;

    private String targetString;
    private List<String> targetStringList;

    private List<Integer> targetIntegerList;

    public int getTargetInteger() {
        return targetInteger;
    }

    public TargetClass setTargetInteger(int targetInteger) {
        this.targetInteger = targetInteger;
        return this;
    }

    public String getTargetFirstName() {
        return targetFirstName;
    }

    public TargetClass setTargetFirstName(String targetFirstName) {
        this.targetFirstName = targetFirstName;
        return this;
    }

    public String getTargetLastName() {
        return targetLastName;
    }

    public TargetClass setTargetLastName(String targetLastName) {
        this.targetLastName = targetLastName;
        return this;
    }

    public String getTargetName() {
        return targetName;
    }

    public TargetClass setTargetName(String targetName) {
        this.targetName = targetName;
        return this;
    }

    public Date getTargetDate() {
        return targetDate;
    }

    public TargetClass setTargetDate(Date targetDate) {
        this.targetDate = targetDate;
        return this;
    }

    public List<Item> getTargetList() {
        return targetList;
    }

    public TargetClass setTargetList(List<Item> targetList) {
        this.targetList = targetList;
        return this;
    }

    public String getTargetString() {
        return targetString;
    }

    public TargetClass setTargetString(String targetString) {
        this.targetString = targetString;
        return this;
    }

    public List<String> getTargetStringList() {
        return targetStringList;
    }

    public void setTargetStringList(List<String> targetStringList) {
        this.targetStringList = targetStringList;
    }

    public List<Integer> getTargetIntegerList() {
        return targetIntegerList;
    }

    public void setTargetIntegerList(List<Integer> targetIntegerList) {
        this.targetIntegerList = targetIntegerList;
    }

}
