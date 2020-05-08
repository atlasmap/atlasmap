import { relative } from 'path';
import { Bundler } from 'scss-bundle';
import { writeFile } from 'fs-extra';

const sourceCssPath = './src/app/lib/atlasmap-data-mapper/components/app/data-mapper-app.component.css';
const targetCssPath = './src/app/lib/atlasmap-data-mapper/components/app/_data-mapper-app.component.css';

/** Inline CSS @import to go around https://github.com/atlasmap/atlasmap/issues/1655 */
async function inlineCss() {
  const bundleResult = await new Bundler().bundle(sourceCssPath);

  if (bundleResult.imports) {
    const cwd = process.cwd();

    const filesNotFound = bundleResult.imports
      .filter(x => !x.found)
      .map(x => relative(cwd, x.filePath));

    if (filesNotFound.length) {
      console.error(`CSS imports failed \n\n${filesNotFound.join('\n - ')}\n`);
      throw new Error('One or more CSS imports failed');
    }
  }

  if (bundleResult.found) {
    await writeFile(targetCssPath, bundleResult.bundledContent);
  }
}

inlineCss();
