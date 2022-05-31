const path = require('path');

module.exports = {
  // globals: {
  //   'ts-jest': {
  //     tsConfig: `tsconfig.test.json`,
  //   },
  // },
  moduleNameMapper: {
    'ky': 'ky/umd',
    'monaco-editor': 'monaco-editor/esm/vs/editor/editor.api.js'
  },
  preset: 'ts-jest',
  setupFilesAfterEnv: [path.resolve(__dirname, './src/setupTests.js')],
  // testEnvironment: 'jsdom',
  transform: {
    // '^.+\.(js|jsx)$': 'babel-jest',
    'monaco-editor': 'jest-esm-transformer',
    // '^.+\.(ts|tsx)$': 'test-jest',
    '^.+\.(js|ts|tsx)$': 'ts-jest'
  },
  transformIgnorePatterns: [
    '<rootDir>/node_modules/(?!(ky|@patternfly/react-styles|monaco-editor|d3-scale)/)'
  ]
};
