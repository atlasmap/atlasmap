package io.atlasmap.mock.v2;

import io.atlasmap.v2.Action;

public class DayOfWeekString extends Action {

    private static final long serialVersionUID = 6401903284974777325L;

    private String stringValue;

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }
    
    @Override
    public String getDisplayName() {
        return "DayOfWeek";
    }
   
}
