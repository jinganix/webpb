module.exports = {
  bail: true,
  collectCoverage: true,
  collectCoverageFrom: ["src/**/*.ts"],
  coverageThreshold: {
    global: {
      branches: 100,
      functions: 100,
      lines: 100,
      statements: 100,
    },
  },
  errorOnDeprecated: true,
  preset: "ts-jest",
  rootDir: "./",
  testEnvironment: "node",
  testMatch: ["<rootDir>/test/**/*(*.)@(test).ts"],
};
