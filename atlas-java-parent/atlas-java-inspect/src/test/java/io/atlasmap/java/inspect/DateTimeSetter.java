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
package io.atlasmap.java.inspect;

interface DateTimeSetter {
    void setYear(java.time.Year year);
    void setMonth(java.time.Month month);
    void setYearMonth(java.time.YearMonth yearMonth);
    void setMonthDay(java.time.MonthDay monthDay);
    void setLocalDate(java.time.LocalDate localDate);
    void setLocalTime(java.time.LocalTime localTime);
    void setLocalDateTime(java.time.LocalDateTime localDateTime);
    void setZonedDateTime(java.time.ZonedDateTime zonedDateTime);
    void setDate(java.util.Date date);
    void setSqlDate(java.sql.Date sqlDate);
}
