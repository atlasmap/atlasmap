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
package com.sun.xml.xsom.impl.parser.state;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public interface NGCCEventSource {
    /**
     * Replaces an old handler with a new handler, and returns
     * ID of the EventReceiver thread.
     */
    int replace( NGCCEventReceiver _old, NGCCEventReceiver _new );
    
    /** Sends an enter element event to the specified EventReceiver thread. */
    void sendEnterElement( int receiverThreadId, String uri, String local, String qname, Attributes atts ) throws SAXException;

    void sendLeaveElement( int receiverThreadId, String uri, String local, String qname ) throws SAXException;
    void sendEnterAttribute( int receiverThreadId, String uri, String local, String qname ) throws SAXException;
    void sendLeaveAttribute( int receiverThreadId, String uri, String local, String qname ) throws SAXException;
    void sendText( int receiverThreadId, String value ) throws SAXException;
}
