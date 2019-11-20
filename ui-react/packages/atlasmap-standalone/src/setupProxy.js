const proxy = require('http-proxy-middleware');
module.exports = function(app) {
  app.use(
    '/v2',
    proxy({
      target: 'http://localhost:8585',
      changeOrigin: true,
    })
  );
};