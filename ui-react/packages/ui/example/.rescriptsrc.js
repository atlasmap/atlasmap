const path = require('path');
const resolveFrom = require('resolve-from');

module.exports = [
  config => {
    config.resolve = {
      ...config.resolve,
      alias: {
        ...config.resolve.alias,
        'atlasmap-ui': path.resolve(__dirname, '../dist'),
        react$: resolveFrom(path.resolve('node_modules'), 'react'),
        'react-dom$': resolveFrom(path.resolve('node_modules'), 'react-dom'),
      },
      plugins: config.resolve.plugins.filter(plugin => plugin.toString().includes('ModuleScopePlugin'))

    };
    return config;
  }
];
