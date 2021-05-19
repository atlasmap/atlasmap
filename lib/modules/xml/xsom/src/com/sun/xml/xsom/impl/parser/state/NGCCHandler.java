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
 * @version $Id: NGCCHandler.java,v 1.9 2002/09/29 02:55:48 okajima Exp $
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public abstract class NGCCHandler implements NGCCEventReceiver {
    protected NGCCHandler( NGCCEventSource source, NGCCHandler parent, int parentCookie ) {
            
        _parent = parent;
        _source = source;
        _cookie = parentCookie;
    }
    
    /**
     * Parent NGCCHandler, if any.
     * If this is the root handler, this field will be null.
     */
    protected final NGCCHandler _parent;
    
    /**
     * Event source.
     */
    protected final NGCCEventSource _source;
    
    /**
     * This method will be implemented by the generated code
     * and returns a reference to the current runtime.
     */
    protected abstract NGCCRuntime getRuntime();
    
    /**
     * Cookie assigned by the parent.
     * 
     * This value will be passed to the onChildCompleted handler
     * of the parent.
     */
    protected final int _cookie;
    
    // used to copy parameters to (enter|leave)(Element|Attribute) events.
    //protected String localName,uri,qname;
    
    
    /**
     * Notifies the completion of a child object.
     * 
     * @param result
     *      The parsing result of the child state.
     * @param cookie
     *      The cookie value passed to the child object
     *      when it is created.
     * @param needAttCheck
     *      This flag is true when the callee needs to call the
     *      processAttribute method to check attribute transitions.
     *      This flag is set to false when this method is triggered by
     *      attribute transition.
     */
    protected abstract void onChildCompleted( Object result, int cookie, boolean needAttCheck ) throws SAXException;
    
//
//
// spawns a new child object from event handlers.
//
//
    public void spawnChildFromEnterElement( NGCCEventReceiver child,
        String uri, String localname, String qname, Attributes atts) throws SAXException {
        
        int id = _source.replace(this,child);
        _source.sendEnterElement(id,uri,localname,qname,atts);
    }
    public void spawnChildFromEnterAttribute( NGCCEventReceiver child,
        String uri, String localname, String qname) throws SAXException {
            
        int id = _source.replace(this,child);
        _source.sendEnterAttribute(id,uri,localname,qname);
    }
    public void spawnChildFromLeaveElement( NGCCEventReceiver child,
        String uri, String localname, String qname) throws SAXException {
            
        int id = _source.replace(this,child);
        _source.sendLeaveElement(id,uri,localname,qname);
    }
    public void spawnChildFromLeaveAttribute( NGCCEventReceiver child,
        String uri, String localname, String qname) throws SAXException {
            
        int id = _source.replace(this,child);
        _source.sendLeaveAttribute(id,uri,localname,qname);
    }
    public void spawnChildFromText( NGCCEventReceiver child,
        String value) throws SAXException {
            
        int id = _source.replace(this,child);
        _source.sendText(id,value);
    }
    
//
//
// reverts to the parent object from the child handler
//
//
    public void revertToParentFromEnterElement( Object result, int cookie,
        String uri,String local,String qname, Attributes atts ) throws SAXException {
            
        int id = _source.replace(this,_parent);
        _parent.onChildCompleted(result,cookie,true);
        _source.sendEnterElement(id,uri,local,qname,atts);
    }
    public void revertToParentFromLeaveElement( Object result, int cookie,
        String uri,String local,String qname ) throws SAXException {
        
        if(uri==NGCCRuntime.IMPOSSIBLE && uri==local && uri==qname && _parent==null )
            // all the handlers are properly finalized.
            // quit now, because we don't have any more NGCCHandler.
            // see the endDocument handler for detail
            return;
        
        int id = _source.replace(this,_parent);
        _parent.onChildCompleted(result,cookie,true);
        _source.sendLeaveElement(id,uri,local,qname);
    }
    public void revertToParentFromEnterAttribute( Object result, int cookie,
        String uri,String local,String qname ) throws SAXException {
            
        int id = _source.replace(this,_parent);
        _parent.onChildCompleted(result,cookie,true);
        _source.sendEnterAttribute(id,uri,local,qname);
    }
    public void revertToParentFromLeaveAttribute( Object result, int cookie,
        String uri,String local,String qname ) throws SAXException {
            
        int id = _source.replace(this,_parent);
        _parent.onChildCompleted(result,cookie,true);
        _source.sendLeaveAttribute(id,uri,local,qname);
    }
    public void revertToParentFromText( Object result, int cookie,
        String text ) throws SAXException {
            
        int id = _source.replace(this,_parent);
        _parent.onChildCompleted(result,cookie,true);
        _source.sendText(id,text);
    }


//
//
// error handler
//
//
    public void unexpectedEnterElement(String qname) throws SAXException {
        getRuntime().unexpectedX('<'+qname+'>');
    }
    public void unexpectedLeaveElement(String qname) throws SAXException {
        getRuntime().unexpectedX("</"+qname+'>');
    }
    public void unexpectedEnterAttribute(String qname) throws SAXException {
        getRuntime().unexpectedX('@'+qname);
    }
    public void unexpectedLeaveAttribute(String qname) throws SAXException {
        getRuntime().unexpectedX("/@"+qname);
    }
}
