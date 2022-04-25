/* eslint-disable */
const path = require('path');
const autoprefixer = require("autoprefixer");
const cssnano = require("cssnano");
const postcssurl = require("postcss-url");

module.exports = () => {
  return {
    entry: {
      app: path.resolve(__dirname, 'src', 'index.ts'),
    },
    devServer: {
      port: 8092,
      stats: 'errors-only',
    },
    module: {
      rules: [
        {
          test: /\.(tsx|ts|jsx)?$/,
          use: [
            {
              loader: 'ts-loader',
              options: {
                transpileOnly: true,
                experimentalWatchApi: true,
              },
            },
          ],
        },
        {
          test: /\.css$/i,
          use: [
            "style-loader",
            {
              loader: "css-loader",
              options: {
                importLoaders: 1,
                modules: true,
              },
            },
            {
              loader: "postcss-loader",
              options: {
                postcssOptions: {
                  plugins: [
                    autoprefixer(),
                    cssnano({
                      preset: "default",
                    }),
                    postcssurl({
                      url: "inline",
                    }),
                  ],
                  modules: true,
                  inject: false,
                  extract: 'styles.css'
                },

              }
            },
          ],
          include: /\.module\.css$/,
        },
        {
          test: /\.css$/,
          use: ["style-loader", "css-loader"],
          exclude: /\.module\.css$/,
        },
        {
          test: /\.(ttf|eot|woff|woff2)$/,
          use: [
            {
              loader: 'file-loader',
              options: {
                name: '[name].[ext]',
                outputPath: 'fonts/'
              }
            }
          ]
        },
        {
          test: /\.(svg|jpg|jpeg|png|gif)$/i,
          use: ['file-loader']

        },
      ],
    },
    output: {
      path: path.resolve(__dirname, 'dist'),
    },
    resolve: {
      extensions: ['.js', '.jsx', '.ts', '.tsx']
    },
  };
};
