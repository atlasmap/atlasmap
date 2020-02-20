module.exports = {
  stories: [
    '../doc/*.stories.mdx',
    '../src/**/*.stories.*'
  ],
  addons: [
    {
      name: "@storybook/preset-typescript",
      options: {
        tsLoaderOptions: {
          transpileOnly: true
        },
        forkTsCheckerWebpackPluginOptions: {
          ignoreDiagnostics: [2344, 2430] // HACK: remove when better typings will come out
        },
        transpileManager: true
      }
    },
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
  ]
}