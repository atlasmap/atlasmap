package io.atlasmap.core.v3;

import java.sql.Time;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class TestClass {

    Boolean nullBooleanWrapper;
    Byte nullByteWrapper;
    Short nullShortWrapper;
    Integer nullIntegerWrapper;
    Long nullLongWrapper;
    Float nullFloatWrapper;
    Double nullDoubleWrapper;
    Character nullCharacterWrapper;
    Boolean booleanWrapper = Boolean.TRUE;
    Byte byteWrapper = 1;
    Short shortWrapper = 2;
    Integer integerWrapper = 3;
    Long longWrapper = 4L;
    Float floatWrapper = 5.5f;
    Double doubleWrapper = 6.9;
    Character characterWrapper = 'a';
    Time nullTime;
    LocalDate nullLocalDate;
    ZonedDateTime nullZonedDateTime;
    TestItem[] nullArray;
    Collection<TestItem> nullSet;
    Map<String, TestItem> nullMap;
    Time time = Time.valueOf("12:00:00");
    LocalDate localDate = LocalDate.now();
    ZonedDateTime zonedDateTime = ZonedDateTime.now();
    TestItem[] array = new TestItem[3];
    Collection<TestItem> set = new HashSet<>();
    Map<String, TestItem> map = new HashMap<>();
    Collection<Collection<TestItem>> collectionOfCollections = new ArrayList<>();
    Map<String, Map<String, TestItem>> mapOfmaps = new HashMap<>();
    String numberString = "16MHz";
    double integerDouble = 7.0;
    private boolean booleanPrimitive = Boolean.TRUE;
    private byte bytePrimitive = 1;
    private short shortPrimitive = Short.MAX_VALUE;
    private int integerPrimitive = Integer.MAX_VALUE;
    private long longPrimitive = Long.MAX_VALUE;
    private float floatPrimitive = Float.MAX_VALUE;
    private double doublePrimitive = Double.MAX_VALUE;
    private char characterPrimitive = 0xFFFF;
    private String string = "string";
    private Date date = new Date();
    private Collection<TestItem> list = new ArrayList<>();

    public boolean hasBooleanPrimitive() {
        return booleanPrimitive;
    }

    public TestClass setBooleanPrimitive(boolean booleanPrimitive) {
        this.booleanPrimitive = booleanPrimitive;
        return this;
    }

    public byte getBytePrimitive() {
        return bytePrimitive;
    }

    public TestClass setBytePrimitive(byte bytePrimitive) {
        this.bytePrimitive = bytePrimitive;
        return this;
    }

    public short getShortPrimitive() {
        return shortPrimitive;
    }

    public TestClass setShortPrimitive(short shortPrimitive) {
        this.shortPrimitive = shortPrimitive;
        return this;
    }

    public int integerPrimitive() {
        return integerPrimitive;
    }

    public TestClass setIntegerPrimitive(int intPrimitive) {
        this.integerPrimitive = intPrimitive;
        return this;
    }

    public long getLongPrimitive() {
        return longPrimitive;
    }

    public TestClass setLongPrimitive(long longPrimitive) {
        this.longPrimitive = longPrimitive;
        return this;
    }

    public float getFloatPrimitive() {
        return floatPrimitive;
    }

    public TestClass setFloatPrimitive(float floatPrimitive) {
        this.floatPrimitive = floatPrimitive;
        return this;
    }

    public double getDoublePrimitive() {
        return doublePrimitive;
    }

    public TestClass setDoublePrimitive(double doublePrimitive) {
        this.doublePrimitive = doublePrimitive;
        return this;
    }

    public char getCharacter() {
        return characterPrimitive;
    }

    public TestClass setCharacter(Character characterPrimitive) {
        this.characterPrimitive = characterPrimitive;
        return this;
    }

    public String getString() {
        return string;
    }

    public TestClass setString(String string) {
        this.string = string;
        return this;
    }

    public Date getDate() {
        return date;
    }

    public TestClass setDate(Date date) {
        this.date = date;
        return this;
    }

    public Collection<TestItem> getList() {
        return list;
    }
}
