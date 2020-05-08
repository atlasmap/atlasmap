import { AppPage } from './app.po';

describe('atlasmap-data-mapper App', () => {
  let page: AppPage;

  beforeEach(() => {
    page = new AppPage();
  });

  it('should display app name', () => {
    page.navigateTo();
    expect(page.getBrandText()).toEqual('AtlasMap Data Mapper UI');
  });
});
