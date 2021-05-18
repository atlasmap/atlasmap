const path = require('path');

module.exports = {
  stories: [
    '../doc/*.stories.mdx',
    '../src/**/*.stories.*'
  ],
  addons: [
    {
      name: "@storybook/addon-docs",
      options: {
        configureJSX: true,
        sourceLoaderOptions: null
      }
    },
    '@storybook/addon-knobs',
    '@storybook/addon-viewport',
    '@storybook/addon-actions',
  ],
  typescript: {
    check: false,
    checkOptions: {},
    reactDocgen: 'react-docgen-typescript',
    reactDocgenTypescriptOptions: {
      shouldExtractLiteralValuesFromEnum: true,
      propFilter: (prop) => (prop.parent ? !/node_modules/.test(prop.parent.fileName) : true),
    },
  }
}