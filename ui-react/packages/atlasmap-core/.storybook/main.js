const path = require("path");

module.exports = {
  stories: ['../**/*.stories.*'],
  addons: [
    {
      name: "@storybook/preset-typescript",
      options: {
        tsLoaderOptions: {
          configFile: path.resolve(__dirname, 'tsconfig.json'),
        },
        tsDocgenLoaderOptions: {
          tsconfigPath: path.resolve(__dirname, "tsconfig.json")
        },
        include: [
          path.resolve(__dirname, "../src"),
          path.resolve(__dirname, "../stories"),
        ],
        transpileManager: true
      }
    },
    {
      name: "@storybook/addon-docs/preset",
      options: {
        configureJSX: true,
        sourceLoaderOptions: null
      }
    },
    '@storybook/addon-knobs',
    '@storybook/addon-viewport',
    '@storybook/addon-actions',
  ]
}