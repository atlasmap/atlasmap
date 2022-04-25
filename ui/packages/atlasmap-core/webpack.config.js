const path = require('path');

module.exports = () => {
  return {
    entry: './src/index',
    devServer: {
      port: 8091,
      stats: 'errors-only',
    },
    module: {
      rules: [
        { test: /\.css$/, use: 'css-loader' },
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
          test: /\.ttf$/,
          use: ['file-loader']
        }
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
