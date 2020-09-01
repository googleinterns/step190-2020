var handlebars = require('handlebars');

/**
 *  config options:
 *   - transformPath - function that transforms original file path to path of the processed file
 *   - templateName - function that translates original file path to template name
 *   - templates - name of the variable to store the templates hash
 */
var createHandlebarsPreprocessor = function(args, config, logger) {
  config = config || {};

  var log = logger.create('preprocessor.handlebars');

  var transformPath = args.transformPath || config.transformPath || function(filepath) {
    return filepath.replace(/\.hbs$/, '.js');
  };

  var templateName = args.templateName || config.templateName || function(filepath) {
    return filepath.replace(/^.*\/([^\/]+)\.hbs$/, '$1');
  };

  var templates = args.templates || config.templates || "Handlebars.templates";

  return function(content, file, done) {
    var processed = null;

    log.debug('Processing "%s".', file.originalPath);
    file.path = transformPath(file.originalPath);

    var template = templateName(file.originalPath);

    if(config.amd) {
      processed = "define(['handlebars'], function(Handlebars) {\n" +
                  "    return Handlebars.template(" + handlebars.precompile(content)  + ");\n" +
                  "});";

    } else {
      try {
        processed = "(function() {" + templates + " = " + templates + " || {};" +
            templates + "['" + template + "'] = Handlebars.template(" +
            handlebars.precompile(content) + ");})();";
      } catch (e) {
        log.error('%s\n  at %s', e.message, file.originalPath);
      }
    }

    done(processed);
  };
};

createHandlebarsPreprocessor.$inject = ['args', 'config.handlebarsPreprocessor', 'logger', 'config.basePath'];

// PUBLISH DI MODULE
module.exports = {
  'preprocessor:handlebars': ['factory', createHandlebarsPreprocessor]
};
