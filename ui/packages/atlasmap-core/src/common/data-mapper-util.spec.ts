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

import { DataMapperUtil } from '../common/data-mapper-util';

describe('DataMapperUtil', () => {
  test('removeItemFromArray', () => {
    const items = [{ foo0: 'bar0' }, { foo1: 'bar1' }, { foo2: 'bar2' }];
    const item = items[1];
    const itemsRemoved = [{ foo0: 'bar0' }, { foo2: 'bar2' }];
    const removed = DataMapperUtil.removeItemFromArray(item, items);
    expect(removed).toBe(true);
    //console.log(items);
    expect(items).toEqual(itemsRemoved);
  });
});
