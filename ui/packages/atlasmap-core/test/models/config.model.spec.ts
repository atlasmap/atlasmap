/* tslint:disable:no-unused-variable */

import { ConfigModel } from '../../src/models/config.model';

describe('ConfigModel', () => {
  test('initialize', () => {
    const cfg = ConfigModel.getConfig();
    expect(cfg.sourcePropertyDoc);
    expect(cfg.sourcePropertyDoc.getName(true) === 'Properties');
    expect(cfg.targetPropertyDoc);
    expect(cfg.targetPropertyDoc.getName(true) === 'Properties');
    expect(cfg.constantDoc.getName(true) === 'Constants');
  });
});
