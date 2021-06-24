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

import { FieldType } from '../contracts';

export class LookupTableEntry {
  sourceValue: string;
  sourceType = FieldType.STRING;
  targetValue: string;
  targetType = FieldType.STRING;
}

export class LookupTable {
  name: string;
  lookupEntry: LookupTableEntry[] = [];
  sourceIdentifier: string;
  targetIdentifier: string;

  constructor() {
    this.name =
      new Date().getTime() +
      '-' +
      Math.floor(Math.random() * 1000000).toString();
  }

  getInputOutputKey(): string {
    return this.sourceIdentifier + ':' + this.targetIdentifier;
  }

  getEntryForSource(
    sourceValue: string,
    autocreate: boolean
  ): LookupTableEntry | null {
    for (const entry of this.lookupEntry) {
      if (entry.sourceValue === sourceValue) {
        return entry;
      }
    }
    if (autocreate) {
      const entry: LookupTableEntry = new LookupTableEntry();
      entry.sourceValue = sourceValue;
      this.lookupEntry.push(entry);
      return entry;
    }
    return null;
  }

  toString() {
    let result: string =
      'Lookup Table, name: ' +
      this.name +
      ', entries: ' +
      this.lookupEntry.length;
    result += '\nsourceIdentifier: ' + this.sourceIdentifier;
    result += '\n\targetIdentifier: ' + this.targetIdentifier;
    let counter = 0;
    for (const entry of this.lookupEntry) {
      result +=
        '\n\tEntry #' +
        counter +
        ': ' +
        entry.sourceValue +
        ' => ' +
        entry.targetValue;
      counter += 1;
    }
    return result;
  }
}
