const path = require('path');

module.exports = {
  globals: {
    'ts-jest': {
      isolatedModules: true,
      tsConfig: `tsconfig.test.json`,
    },
  },
  moduleNameMapper: {
    '\\.(css|less)$': path.resolve(__dirname, './src/__mocks__/styleMock.js'),
    '\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$': path.resolve(
      __dirname,
      './src/__mocks__/fileMock.js'
    ),
    'ky': 'ky/umd',
    'monaco-editor': 'monaco-editor/esm/vs/editor/editor.api.d.ts',
    '@atlasmap/core': path.resolve(__dirname, '../atlasmap-core/src')
  },
  preset: 'ts-jest',
  setupFilesAfterEnv: [path.resolve(__dirname, './src/setupTests.js')],
  transform: {
    '^.+\.(js|ts|tsx)$': 'ts-jest',
    '^.+\\.(css)$': 'jest-css-modules-transform'
  },
  transformIgnorePatterns: [
    '<rootDir>/node_modules/(?!(ky|@patternfly/react-styles|monaco-editor|d3-scale)/)'
  ]
};
