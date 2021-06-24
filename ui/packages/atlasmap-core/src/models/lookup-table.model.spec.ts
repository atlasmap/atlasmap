/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

import { LookupTable, LookupTableEntry } from '../models/lookup-table.model';
import { FieldType } from '../contracts/common';

describe('LookupTable', () => {
  test('initialize', () => {
    const entry = new LookupTableEntry();
    entry.sourceType = FieldType.STRING;
    entry.sourceValue = 'sValue';
    entry.targetType = FieldType.STRING;
    entry.targetValue = 'tValue';
    const table = new LookupTable();
    expect(table.name);
    table.lookupEntry.push(entry);
    expect(table.toString()).toContain('Entry #0: sValue => tValue');
  });
});
