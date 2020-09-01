# karma-handlebars-preprocessor

[![Build Status](https://travis-ci.org/petrbela/karma-handlebars-preprocessor.svg)](https://travis-ci.org/petrbela/karma-handlebars-preprocessor)
[![npm version](https://badge.fury.io/js/karma-handlebars-preprocessor.svg)](http://badge.fury.io/js/karma-handlebars-preprocessor)

> Preprocessor to compile Handlebars on the fly.

Forked from [hanachin's code](https://github.com/hanachin/karma-handlebars-preprocessor) (kudos!)

Works with **Karma 0.9 or later**.

For more information on Karma see the [homepage].


## Installation

1. Install karma-handlebars-preprocessor plugin.

  ```sh
  $ npm install karma-handlebars-preprocessor --save-dev
  ```

2. Define it as a preprocessor in the config file

  ```js
    preprocessors: {
      '**/*.hbs': 'handlebars'
    }
  ```

  or pass through the command line

  ```sh
    $ karma start --preprocessors handlebars
  ```


## Configuration

You can configure default behaviour in the `handlebarsPreprocessor` section of the config file. The following shows the default implementation:

```js
// karma.conf.js
module.exports = function(config) {
  config.set({
    preprocessors: {
      '**/*.hbs': ['handlebars']
    },

    handlebarsPreprocessor: {
      // name of the variable to store the templates hash
      templates: "Handlebars.templates",

      // translates original file path to template name
      templateName: function(filepath) {
        return filepath.replace(/^.*\/([^\/]+)\.hbs$/, '$1');
      },

      // transforms original file path to path of the processed file
      transformPath: function(path) {
        return path.replace(/\.hbs$/, '.js');
      }
    }
  });
};
```

### AMD Based Template Configuration

If you want to export your compiled templates as anonymous AMD modules,
use the `amd` option in the config as shown below:

```js
    handlebarsPreprocessor: {
      amd: true
    }
```

## License

MIT License


[homepage]: http://karma-runner.github.io
