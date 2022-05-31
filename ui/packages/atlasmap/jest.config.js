const path = require('path');
module.exports = {
  globals: {
    'ts-jest': {
      allowJs: true,
      isolatedModules: true,
      tsConfig: `tsconfig.test.json`,
    },
  },
  moduleNameMapper: {
    // '\\.(css|less)$': path.resolve(__dirname, './test/__mocks__/styleMock.js'),
    '\\.(css|less|sass|scss)$': 'identity-obj-proxy',
    '\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$': path.resolve(
      __dirname,
      './test/__mocks__/fileMock.js'
    ),
    'ky': 'ky/umd',
    'monaco-editor': 'monaco-editor/esm/vs/editor/editor.api.d.ts',
    '@atlasmap/core': path.resolve(__dirname, '../atlasmap-core/src')
  },
  preset: 'ts-jest',
  setupFilesAfterEnv: [path.resolve(__dirname, './test/setup.ts')],
  testEnvironment: 'jest-environment-jsdom-sixteen',
  transform: {
    '^.+\\.(ts|tsx)$': 'ts-jest',
    '^.+\\.(css)$': 'jest-css-modules-transform'
  },
  transformIgnorePatterns: [
    '/node_modules/(?!(ky|monaco-editor)/)'
  ],
  verbose: true,
};
