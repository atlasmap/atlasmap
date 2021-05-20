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

import { ErrorInfo, ErrorLevel } from '../models/error.model';

describe('ErrorModel', () => {
  it('initialize', () => {
    const ei: ErrorInfo = new ErrorInfo({
      message: 'test error message',
      level: ErrorLevel.ERROR,
      object: null,
    });
    expect(ei.object).toBeNull();
    expect(+ei.identifier).toBeGreaterThanOrEqual(0);
    expect(ei.level).toBe(ErrorLevel.ERROR);
    expect(ei.message).toBe('test error message');
  });
});
