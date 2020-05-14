/* tslint:disable:no-unused-variable */

import { DataMapperUtil } from '../../src/common/data-mapper-util';

describe('DataMapperUtil', () => {

  test('removeItemFromArray', () => {
    const items = [
      { "foo0": "bar0" },
      { "foo1": "bar1" },
      { "foo2": "bar2" }
    ];
    const item = items[1];
    const itemsRemoved = [
      { "foo0": "bar0" },
      { "foo2": "bar2" }
    ];
    const removed = DataMapperUtil.removeItemFromArray(item, items);
    expect(removed).toBe(true);
    //console.log(items);
    expect(items).toEqual(itemsRemoved);
  });

});
