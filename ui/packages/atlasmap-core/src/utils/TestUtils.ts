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
/*
    Deep diff of JSON objects.  Return true if they match, false otherwise.
*/
export class TestUtils {
  static isEqualJSON = function (eObj: any, gObj: any): boolean {
    const expected = Object.keys(eObj);
    const generated = Object.keys(gObj);

    if (expected.length !== generated.length) {
      console.error(
        'JSON object key length error.  Expected: ' +
          expected.length +
          ', Generated: ' +
          generated.length
      );
      return false;
    }

    for (let objKey of generated) {
      if (eObj[objKey] !== gObj[objKey]) {
        if (
          typeof eObj[objKey] === 'object' &&
          typeof gObj[objKey] === 'object'
        ) {
          if (!TestUtils.isEqualJSON(eObj[objKey], gObj[objKey])) {
            console.info('Expected: ', eObj[objKey]);
            console.error('Generated: ', gObj[objKey]);
            return false;
          }
        }
      }
    }
    return true;
  };
}
