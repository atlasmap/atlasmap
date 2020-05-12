const path = require('path')
const TsconfigPathsPlugin = require('tsconfig-paths-webpack-plugin')
const excludePaths = [/node_modules/, /dist/]

module.exports = ({ config }) => {
  // HACK: ensure typescript paths are resolved by webpack too
  // https://github.com/storybookjs/storybook/issues/3291#issuecomment-500472928
  config.resolve.plugins = config.resolve.plugins || []
  config.resolve.plugins.push(
    new TsconfigPathsPlugin({
      configFile: path.resolve(__dirname, "../tsconfig.json")
    })
  )

  // Use real file paths for symlinked dependencies do avoid including them multiple times
  config.resolve.symlinks = true

  // HACK: extend existing JS rule to ensure all dependencies are correctly ignored
  // https://github.com/storybooks/storybook/issues/3346#issuecomment-459439438
  const jsRule = config.module.rules.find((rule) => rule.test.test('.jsx'))
  jsRule.exclude = excludePaths

  // HACK: Instruct Babel to check module type before injecting Core JS polyfills
  // https://github.com/i-like-robots/broken-webpack-bundle-test-case
  const babelConfig = jsRule.use.find(({ loader }) => loader === 'babel-loader')
  babelConfig.options.sourceType = 'unambiguous'

  // HACK: Ensure we only bundle one instance of React
  config.resolve.alias.react = require.resolve('react')

  config.module.rules.push({
    test: /\.js$/,
    use: ["source-map-loader"],
    enforce: "pre",
    include: [
      path.resolve(__dirname, "..")
    ]
  })
  

  return config
}