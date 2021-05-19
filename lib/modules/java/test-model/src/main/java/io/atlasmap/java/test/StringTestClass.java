/*
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
package io.atlasmap.java.test;

import java.nio.CharBuffer;

public class StringTestClass {

    private CharBuffer testCharBuffer;
    private CharSequence testCharSequence;
    private String testString;
    private StringBuffer testStringBuffer;
    private StringBuilder testStringBuilder;

    public CharBuffer getTestCharBuffer() {
        return testCharBuffer;
    }
    public void setTestCharBuffer(CharBuffer testCharBuffer) {
        this.testCharBuffer = testCharBuffer;
    }
    public CharSequence getTestCharSequence() {
        return testCharSequence;
    }
    public void setTestCharSequence(CharSequence testCharSequence) {
        this.testCharSequence = testCharSequence;
    }
    public String getTestString() {
        return testString;
    }
    public void setTestString(String testString) {
        this.testString = testString;
    }
    public StringBuffer getTestStringBuffer() {
        return testStringBuffer;
    }
    public void setTestStringBuffer(StringBuffer testStringBuffer) {
        this.testStringBuffer = testStringBuffer;
    }
    public StringBuilder getTestStringBuilder() {
        return testStringBuilder;
    }
    public void setTestStringBuilder(StringBuilder testStringBuilder) {
        this.testStringBuilder = testStringBuilder;
    }

}
