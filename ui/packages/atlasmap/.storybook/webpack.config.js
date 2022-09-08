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
  if (babelConfig) {
    babelConfig.options.sourceType = 'unambiguous'
  }

  config.module.rules.push({
    test: /\.js$/,
    use: ["source-map-loader"],
    enforce: "pre",
    include: [
      path.resolve(__dirname, "..")
    ]
  })

  config.module.rules.push({
    test: /\.js$/,
    use: [
      {
        loader: "babel-loader",
        options: {
          presets: [
            "@babel/preset-env"
          ]
        },
      }
    ],
    include: [ path.resolve(__dirname, '../../../node_modules/ky') ]
  })

  const cssModuleRegex = /\.module\.css$/;
  config.module.rules.forEach((rule, idx) => {
    if (rule.test.test('.css')) {
      if (rule.exclude) {
        if (Array.isArray(rule.exclude)) {
          rule.exclude = [...rule.exclude, cssModuleRegex]
        } else {
          rule.exclude = [rule.exclude, cssModuleRegex]
        }
      } else {
        rule.exclude = cssModuleRegex;
      }
    }
  })

  config.module.rules.push({
    test: /\.module\.css$/,
    sideEffects: true,
    use: [
      "style-loader",
      {
        loader: "css-loader",
        options: {
          importLoaders: 1,
          modules : {
            localIdentName: "[path][name]__[local]--[hash:base64:5]",
          }
        }
      },
      {
        loader: "postcss-loader",
        options: {
          postcssOptions: {
            plugins: [
              [ 'postcss-preset-env', {'stage': 0}],
            ],
          }
        }
      }
    ],
    include: [ path.resolve(__dirname, '../') ]
  })

  return config
}