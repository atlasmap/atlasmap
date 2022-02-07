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

import { CommonUtil } from './common-util';
import { Field } from '../models/field.model';

describe('CommonUtil', () => {
  test('removeItemFromArray', () => {
    const items = [{ foo0: 'bar0' }, { foo1: 'bar1' }, { foo2: 'bar2' }];
    const item = items[1];
    const itemsRemoved = [{ foo0: 'bar0' }, { foo2: 'bar2' }];
    const removed = CommonUtil.removeItemFromArray(item, items);
    expect(removed).toBe(true);
    expect(items).toEqual(itemsRemoved);
  });

  test('sanitizeJSON()', () => {
    const buffer = '{}\u007F\uFFFF';
    const sanitized = CommonUtil.sanitizeJSON(buffer);
    expect(sanitized).toBe('"{}\\u007f\\uffff"');
  });

  test('objectize()', () => {
    const objectized = CommonUtil.objectize('{"string": "test"}');
    expect(objectized.string).toBe('test');
    const f = new Field();
    f.name = 'dummy';
    const noop = CommonUtil.objectize(f);
    expect(noop).toBe(f);
  });

  test('urlWithParameters', () => {
    expect(CommonUtil.urlWithParameters('url:', {})).toBe('url:');
    expect(CommonUtil.urlWithParameters('url:', { a: '1' })).toBe('url:?a=1');
    expect(CommonUtil.urlWithParameters('url:?a=1', { b: '2' })).toBe(
      'url:?b=2'
    );
    expect(
      CommonUtil.urlWithParameters('url:?a=1&b=2', { b: '2', c: '3' })
    ).toBe('url:?b=2&c=3');
    expect(CommonUtil.urlWithParameters('url:?a=1&b=2', {})).toBe('url:');
  });
});
