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

    private boolean targetBoolean;
    private int targetInteger;
    private int targetInteger2;
    private String targetFirstName;
    private String targetLastName;
    private String targetName;
    private String targetFullName;
    private Integer targetStreetNumber;
    private String targetStreetName1;
    private String targetStreetName2;
    private Date targetDate;
    private List<Item> targetList;

    private String targetString;
    private List<String> targetStringList;

    private List<Integer> targetIntegerList;
    private Double targetWeightDouble;
    private String targetWeightUnit;

    public int getTargetInteger() {
        return targetInteger;
    }

    public TargetClass setTargetInteger(int targetInteger) {
        this.targetInteger = targetInteger;
        return this;
    }

    public int getTargetInteger2() {
        return targetInteger2;
    }

    public TargetClass setTargetInteger2(int targetInteger2) {
        this.targetInteger2 = targetInteger2;
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

    public String getTargetFullName() {
        return targetFullName;
    }

    public void setTargetFullName(String targetFullName) {
        this.targetFullName = targetFullName;
    }

    public Integer getTargetStreetNumber() {
        return targetStreetNumber;
    }

    public void setTargetStreetNumber(Integer targetStreetNumber) {
        this.targetStreetNumber = targetStreetNumber;
    }

    public String getTargetStreetName1() {
        return targetStreetName1;
    }

    public void setTargetStreetName1(String targetStreetName1) {
        this.targetStreetName1 = targetStreetName1;
    }

    public String getTargetStreetName2() {
        return targetStreetName2;
    }

    public void setTargetStreetName2(String targetStreetName2) {
        this.targetStreetName2 = targetStreetName2;
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

    public boolean isTargetBoolean() {
        return targetBoolean;
    }

    public void setTargetBoolean(boolean targetBoolean) {
        this.targetBoolean = targetBoolean;
    }

    public Double getTargetWeightDouble() {
        return targetWeightDouble;
    }

    public void setTargetWeightDouble(Double targetWeightDouble) {
        this.targetWeightDouble = targetWeightDouble;
    }

    public String getTargetWeightUnit() {
        return targetWeightUnit;
    }

    public void setTargetWeightUnit(String targetWeightUnit) {
        this.targetWeightUnit = targetWeightUnit;
    }

}
