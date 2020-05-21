/* tslint:disable:no-unused-variable */

import { ConfigModel } from '../../src/models/config.model';

describe('ConfigModel', () => {
  test('initialize', () => {
    const cfg = ConfigModel.getConfig();
    expect(cfg.propertyDoc);
    expect(cfg.propertyDoc.getName(true) === 'Properties');
    expect(cfg.constantDoc.getName(true) === 'Constants');
  });
});
