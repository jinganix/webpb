import baseConfig from "../runtime/ts/eslint.config.js";

export default [
  {
    ignores: ["testdata/ts/errors/**"],
  },
  ...baseConfig,
  {
    files: ["testdata/ts/**/*.ts"],
    rules: {
      "@typescript-eslint/no-explicit-any": "off",
      "@typescript-eslint/no-namespace": "off",
    },
  },
];
