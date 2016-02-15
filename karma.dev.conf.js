var path = require('path');

module.exports = function(config) {
  var root = 'target';

  config.set({
    logLevel: config.LOG_DEBUG,
    frameworks: ['cljs-test'],

    files: [
      // Provide a stub `window.renderContext`.
      'test-js/test-render-context.js',

      // shim the gravie member environment (bad!)
      'test-js/test-gravie-env.js',

      // Add Google Closure.
      path.join(root, 'goog/base.js'),

      path.join(root, 'cljs_deps.js'),

      // Give Closure the dependency info it needs.
      path.join(root, 'gravie-member-client-debug.js'),

      // Require the actual tests.
      'test-js/require-karma.js',

      // Serve the app. Use `included: false` so that Karma itself doesn't load
      // these files in the browser; we leave that to `goog.require()` and
      // namespace dependency resolution.
      { pattern: path.join(root, '**/*.js'), included: false},
      { pattern: path.join(root, '**/*.js.map'), included: false},
      { pattern: path.join(root, '**/*.cljs'), included: false}
    ],

    client: {
      // main function
      args: ['gravie_member_client.test_runner.run_tests_for_karma']
    },

    browsers: ['Chrome'],

    reporters: ['progress']
  });
};
