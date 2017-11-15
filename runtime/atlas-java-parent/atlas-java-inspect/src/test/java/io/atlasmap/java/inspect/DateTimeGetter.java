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

public interface DateTimeGetter {
    java.time.Year getYear();
    java.time.Month getMonth();
    java.time.YearMonth getYearMonth();
    java.time.MonthDay getMonthDay();
    java.time.LocalDate getLocalDate();
    java.time.LocalTime getLocalTime();
    java.time.LocalDateTime getLocalDateTime();
    java.time.ZonedDateTime getZonedDateTime();
    java.util.Date getDate();
    java.sql.Date getSqlDate();
}
