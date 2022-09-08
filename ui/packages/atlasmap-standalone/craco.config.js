module.exports = {
  babel: {
    presets: [
      "@babel/preset-env",
      "@babel/preset-react"
    ],
    plugins: [
      "@babel/plugin-proposal-nullish-coalescing-operator",
      ["@babel/plugin-proposal-private-methods", { "loose": true }],
      ["@babel/plugin-proposal-class-properties", { "loose": true }]
    ]
  }
};
