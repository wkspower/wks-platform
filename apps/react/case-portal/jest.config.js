// Jest harness for the portal. Kept separate from the webpack/babel-loader setup
// (package.json "babel" stays preset-react only, for the production build); jest
// gets its own inline babel transform below so the two never fight.
module.exports = {
  testEnvironment: 'jsdom',
  setupFilesAfterEnv: ['<rootDir>/src/setupTests.js'],
  // Resolve the portal's absolute imports (e.g. `services`, `store`) the same way
  // webpack's resolve.modules does — from src/ as a root.
  moduleDirectories: ['node_modules', 'src'],
  moduleNameMapper: {
    // Stub styles and static assets so importing a component doesn't choke on them.
    '\\.(css|scss|sass|less)$': 'identity-obj-proxy',
    '\\.(png|jpg|jpeg|gif|svg|woff|woff2|ttf|eot)$':
      '<rootDir>/test/__mocks__/fileMock.js',
    // Replace src/consts (top-level await) with a static test config so any module
    // that imports it stays loadable under jest's CommonJS transform.
    '(?:^|/)consts$': '<rootDir>/test/__mocks__/constsMock.js',
  },
  transform: {
    '^.+\\.[jt]sx?$': [
      'babel-jest',
      {
        // Isolate from the project babel config so the webpack build is untouched.
        configFile: false,
        babelrc: false,
        presets: [
          ['@babel/preset-env', { targets: { node: 'current' } }],
          ['@babel/preset-react', { runtime: 'automatic' }],
        ],
      },
    ],
  },
  // Some deps (e.g. keycloak-js) ship ESM-only builds. Jest ignores node_modules
  // for transforms by default, so allow-list the ESM ones through babel.
  transformIgnorePatterns: ['/node_modules/(?!(keycloak-js)/)'],
  testMatch: ['<rootDir>/src/**/*.test.js', '<rootDir>/test/**/*.test.js'],
}
