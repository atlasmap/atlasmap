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
package io.atlasmap.java.test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTimeClass {
    // first representatives
    private Date dateField;
    private LocalDate localDateField;
    private LocalTime localTimeField;
    private ZonedDateTime zonedDateTimeField;
    // others
    private Year yearField;
    private YearMonth yearMonthField;
    private MonthDay monthDayField;
    private LocalDateTime localDateTimeField;
    private Calendar calendarField;
    private GregorianCalendar gregorianCalendarField;
    private java.sql.Date sqlDateField;
    private java.sql.Time sqlTimeField;
    private java.sql.Timestamp sqlTimestampField;

    public Date getDateField() {
        return dateField;
    }

    public void setDateField(Date dateField) {
        this.dateField = dateField;
    }

    public LocalDate getLocalDateField() {
        return localDateField;
    }

    public void setLocalDateField(LocalDate localDateField) {
        this.localDateField = localDateField;
    }

    public LocalTime getLocalTimeField() {
        return localTimeField;
    }

    public void setLocalTimeField(LocalTime localTimeField) {
        this.localTimeField = localTimeField;
    }

    public ZonedDateTime getZonedDateTimeField() {
        return zonedDateTimeField;
    }

    public void setZonedDateTimeField(ZonedDateTime zonedDateTimeField) {
        this.zonedDateTimeField = zonedDateTimeField;
    }

    public LocalDateTime getLocalDateTimeField() {
        return localDateTimeField;
    }

    public void setLocalDateTimeField(LocalDateTime localDateTimeField) {
        this.localDateTimeField = localDateTimeField;
    }

    public Calendar getCalendarField() {
        return calendarField;
    }

    public void setCalendarField(Calendar calendarField) {
        this.calendarField = calendarField;
    }

    public GregorianCalendar getGregorianCalendarField() {
        return gregorianCalendarField;
    }

    public void setGregorianCalendarField(GregorianCalendar gregorianCalendarField) {
        this.gregorianCalendarField = gregorianCalendarField;
    }

    public java.sql.Date getSqlDateField() {
        return sqlDateField;
    }

    public void setSqlDateField(java.sql.Date sqlDateField) {
        this.sqlDateField = sqlDateField;
    }

    public java.sql.Time getSqlTimeField() {
        return sqlTimeField;
    }

    public void setSqlTimeField(java.sql.Time sqlTimeField) {
        this.sqlTimeField = sqlTimeField;
    }

    public java.sql.Timestamp getSqlTimestampField() {
        return sqlTimestampField;
    }

    public void setSqlTimestampField(java.sql.Timestamp sqlTimestampField) {
        this.sqlTimestampField = sqlTimestampField;
    }

    public Year getYearField() {
        return yearField;
    }

    public void setYearField(Year yearField) {
        this.yearField = yearField;
    }

    public YearMonth getYearMonthField() {
        return yearMonthField;
    }

    public void setYearMonthField(YearMonth yearMonthField) {
        this.yearMonthField = yearMonthField;
    }

    public MonthDay getMonthDayField() {
        return monthDayField;
    }

    public void setMonthDayField(MonthDay monthDayField) {
        this.monthDayField = monthDayField;
    }

}
