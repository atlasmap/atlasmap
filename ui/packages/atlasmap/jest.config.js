const path = require('path');
module.exports = {
  globals: {
    'ts-jest': {
      allowJs: true,
      isolatedModules: true,
      tsConfig: `tsconfig.test.json`,
    },
  },
  moduleDirectories: ["node_modules", "src"],
  moduleNameMapper: {
    '\\.(css|less)$': path.resolve(__dirname, './test/__mocks__/styleMock.js'),
    '\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$': path.resolve(
      __dirname,
      './test/__mocks__/fileMock.js'
    ),
    'ky': 'ky/umd',
    'monaco-editor': 'monaco-editor/esm/vs/editor/editor.api.js',
    '@atlasmap/core': path.resolve(__dirname, '../atlasmap-core/src')
  },
  preset: 'ts-jest',
  setupFilesAfterEnv: [path.resolve(__dirname, './test/setup.ts')],
  testEnvironment: 'jest-environment-jsdom-sixteen',
  transform: {
    '^.+\\.(js|jsx)$': 'babel-jest',
    '^.+\\.(ts|tsx)$': 'ts-jest',
    '^.+\\.(css)$': 'jest-css-modules-transform'
  },
  transformIgnorePatterns: [
    '(?!atlasmap-core)',
    '/node_modules/(?!ky)',
    '/node_modules/(?!monaco-editor)'
  ],
  verbose: true,
};
