module.exports = {
  extends: [
    "react-app",
    "prettier/@typescript-eslint",
    "plugin:prettier/recommended",
    "plugin:mdx/recommended",
  ],
  settings: {
    react: {
      version: "detect",
    },
  },
  plugins: ["react-hooks"],
  rules: {
    "react-hooks/rules-of-hooks": "error",
    "react-hooks/exhaustive-deps": "error",
  },
};
