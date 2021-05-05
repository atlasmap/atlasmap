const postcss = require("rollup-plugin-postcss");
const autoprefixer = require("autoprefixer");
const cssnano = require("cssnano");
const postcssurl = require("postcss-url");

module.exports = {
  rollup(config, options) {
    config.plugins.push(
      postcss({
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
      }),
    );
    return config;
  },
};
