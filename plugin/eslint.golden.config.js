import baseConfig from "../runtime/ts/eslint.config.js";

export default [
  ...baseConfig,
  {
    files: ["build/golden-format/ts/**/*.ts"],
    rules: {
      "@typescript-eslint/no-explicit-any": "off",
      "@typescript-eslint/no-namespace": "off",
    },
  },
];
