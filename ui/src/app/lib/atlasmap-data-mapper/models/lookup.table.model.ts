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

export class LookupTableEntry {
    sourceValue: string;
    sourceType: string = "STRING";
    targetValue: string;
    targetType: string = "STRING";

    public toJSON(): any {
        return {
            "sourceValue": this.sourceValue,
            "sourceType": this.sourceType,
            "targetValue": this.targetValue,
            "targetType": this.targetType,
        };
    }

    public fromJSON(json: any): void {
        this.sourceValue = json.sourceValue;
        this.sourceType = json.sourceType;
        this.targetValue = json.targetValue;
        this.targetType = json.targetType;
    }
}

export class LookupTable {
    name: string;
    entries: LookupTableEntry[] = [];
    sourceIdentifier: string;
    targetIdentifier: string;

    constructor() {
        this.name = (new Date().getTime() + "-" + Math.floor(Math.random()*1000000).toString());
    }

    public getInputOutputKey(): string {
        return this.sourceIdentifier + ":" + this.targetIdentifier;
    }

    public getEntryForSource(sourceValue: string, autocreate: boolean): LookupTableEntry {
        for (let entry of this.entries) {
            if (entry.sourceValue == sourceValue) {
                return entry;
            }
        }
        if (autocreate) {
            var entry: LookupTableEntry = new LookupTableEntry();
            entry.sourceValue = sourceValue;
            this.entries.push(entry);
            return entry;
        }
        return null;
    }

    public toString() {
        var result: string = "Lookup Table, name: " + this.name + ", entries: " + this.entries.length;
        result += "\n\sourceIdentifier: " + this.sourceIdentifier;
        result += "\n\targetIdentifier: " + this.targetIdentifier;
        var counter: number = 0;
        for (let entry of this.entries) {
            result += "\n\tEntry #" + counter + ": " + entry.sourceValue + " => " + entry.targetValue;
            counter += 1;
        }
        return result;
    }
}
