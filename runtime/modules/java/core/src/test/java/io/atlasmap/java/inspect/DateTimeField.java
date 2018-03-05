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

class DateTimeField {
    java.time.Year year;
    java.time.Month month;
    java.time.YearMonth yearMonth;
    java.time.MonthDay monthDay;
    java.time.LocalDate localDate;
    java.time.LocalTime localTime;
    java.time.LocalDateTime localDateTime;
    java.time.ZonedDateTime zonedDateTime;
    java.util.Calendar calendar;
    java.util.Date date;
    java.util.GregorianCalendar gregorianCalendar;
    java.sql.Date sqlDate;
    java.sql.Time sqlTime;
    java.sql.Timestamp sqlTimestamp;
}
