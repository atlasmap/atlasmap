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

export enum ErrorLevel { DEBUG, INFO, WARN, ERROR, VALIDATION_ERROR }

export class ErrorInfo {

    readonly identifier: string;
    readonly message: string;
    readonly level: ErrorLevel;
    readonly error: any;
    private static errorIdentifierCounter = 0;

    constructor(message: string, level: ErrorLevel, error?: any) {
        this.identifier = ErrorInfo.errorIdentifierCounter.toString();
        this.message = message;
        this.level = level;
        this.error = error;
        ErrorInfo.errorIdentifierCounter++;
    }
}
