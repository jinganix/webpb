module.exports = {
  env: {
    es6: true,
    jest: true,
    node: true,
  },
  extends: [
    "plugin:@typescript-eslint/recommended",
    "plugin:prettier/recommended",
  ],
  parser: "@typescript-eslint/parser",
  plugins: ["import", "sort-keys-fix"],
  root: true,
  rules: {
    "import/newline-after-import": "error",
    "import/order": [
      "error",
      {
        groups: [
          ["builtin", "external"],
          "internal",
          ["index", "parent", "sibling"],
        ],
      },
    ],
    "max-len": [
      "error",
      {
        code: 80,
        ignoreStrings: true,
        ignoreUrls: true,
      },
    ],
    "require-jsdoc": [
      "error",
      {
        require: {
          ArrowFunctionExpression: false,
          ClassDeclaration: false,
          FunctionDeclaration: false,
          FunctionExpression: false,
          MethodDefinition: false,
        },
      },
    ],
    semi: "off",
    "@typescript-eslint/no-var-requires": "off",
    "sort-keys": ["error"],
    "sort-keys-fix/sort-keys-fix": "warn",
  },
};
