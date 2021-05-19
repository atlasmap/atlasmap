/*
 * Copyright (C) 2017 Oracle
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
package com.sun.xml.xsom;

/**
 * Constants that represent variety of simple types.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke,kawaguchi@sun.com)
 */
public final class XSVariety {
    public static final XSVariety ATOMIC = new XSVariety("atomic");
    public static final XSVariety UNION  = new XSVariety("union");
    public static final XSVariety LIST   = new XSVariety("list");
    
    private XSVariety(String _name) { this.name=_name; }
    private final String name;
    public String toString() { return name; }
}

