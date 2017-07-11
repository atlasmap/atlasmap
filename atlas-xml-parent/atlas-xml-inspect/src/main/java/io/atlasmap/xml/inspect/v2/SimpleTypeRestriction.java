package io.atlasmap.xml.inspect.v2;

import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSRestrictionSimpleType;

import java.util.Vector;

/**
 */
public class SimpleTypeRestriction {

    String[] enumeration = null;
    String maxValue = null;
    String minValue = null;
    String length = null;
    String maxLength = null;
    String minLength = null;
    String pattern = null;
    String totalDigits = null;
    String maxExclusive = null;
    String minExclusive = null;


    public void initRestrictions(XSRestrictionSimpleType restriction) {
        if (restriction != null) {
            Vector<String> enumeration = new Vector<>();
            for (XSFacet facet : restriction.getDeclaredFacets()) {
                if (facet.getName().equals(XSFacet.FACET_ENUMERATION)) {
                    enumeration.add(facet.getValue().value);
                }
                if (facet.getName().equals(XSFacet.FACET_MAXINCLUSIVE)) {
                    this.maxValue = facet.getValue().value;
                }
                if (facet.getName().equals(XSFacet.FACET_MININCLUSIVE)) {
                    this.minValue = facet.getValue().value;
                }
                if (facet.getName().equals(XSFacet.FACET_MAXEXCLUSIVE)) {
                    this.maxExclusive = String.valueOf(Integer.parseInt(facet.getValue().value) - 1);
                }
                if (facet.getName().equals(XSFacet.FACET_MINEXCLUSIVE)) {
                    this.minExclusive = String.valueOf(Integer.parseInt(facet.getValue().value) + 1);
                }
                if (facet.getName().equals(XSFacet.FACET_LENGTH)) {
                    this.length = facet.getValue().value;
                }
                if (facet.getName().equals(XSFacet.FACET_MAXLENGTH)) {
                    this.maxLength = facet.getValue().value;
                }
                if (facet.getName().equals(XSFacet.FACET_MINLENGTH)) {
                    this.minLength = facet.getValue().value;
                }
                if (facet.getName().equals(XSFacet.FACET_PATTERN)) {
                    this.pattern = facet.getValue().value;
                }
                if (facet.getName().equals(XSFacet.FACET_TOTALDIGITS)) {
                    this.totalDigits = facet.getValue().value;
                }
            }
            if (enumeration.size() > 0) {
                this.enumeration = enumeration.toArray(new String[]{});
            }
        }
    }
}
