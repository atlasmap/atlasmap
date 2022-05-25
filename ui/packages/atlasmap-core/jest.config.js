const path = require('path');
module.exports = {
  globals: {
    'ts-jest': {
      tsConfig: `tsconfig.test.json`,
    },
  },
  moduleNameMapper: {
    '\\.(css|less)$': path.resolve(__dirname, './test/__mocks__/styleMock.js'),
    '\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$': path.resolve(
      __dirname,
      './test/__mocks__/fileMock.js'
    ),
    "ky": "ky/umd",
    "monaco-editor": "monaco-editor/esm/vs/editor/editor.api.js",
    "@src": "<rootDir>/src/index.ts",
    "@src/(.*)": "<rootDir>/src/$1",
    "@test/(.*)": "<rootDir>/test/$1"
  },
  preset: 'ts-jest',
  setupFilesAfterEnv: [path.resolve(__dirname, './test/setup.tsx')],
  testEnvironment: 'jsdom',
  transform: {
    "monaco-editor": "jest-esm-transformer",
    "^.+\\.(ts|tsx)$": "ts-jest"
  },
  transformIgnorePatterns: [
    "/node_modules/(?!(ky|monaco-editor)/)"
  ]
};
