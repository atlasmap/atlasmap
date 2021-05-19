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

