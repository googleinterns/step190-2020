module.exports = function ( config ) {
    config.set( {
        basePath         : '../../../',
        frameworks : [
            'jasmine'
        ],
        files            : [
            'src/main/webapp/**/*.js',
            'src/test/js/**/*.js'
        ],
        exclude          : [],
        preprocessors    : {
            'src/main/webapp/**/*.js' : ['coverage'],
            'src/main/webapp/**/*.js' : ['babel'],
            'src/test/js/**/*.js' : ['babel'],
            '**/*.hbs': 'handlebars'
        },
        babelPreprocessor: {
            options: {
              presets: ['@babel/preset-env'],
              sourceMap: 'inline'
            },
            filename: function (file) {
              return file.originalPath.replace(/\.js$/, '.es5.js');
            },
            sourceFileName: function (file) {
              return file.originalPath;
            }
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
        },
        reporters        : ['progress', 'coverage'],
        port             : 9876,
        colors           : true,
        logLevel         : config.LOG_INFO,
        autoWatch        : true,
        browsers         : ['Chrome', 'PhantomJS'],
        singleRun        : false,
        plugins          : [
            'karma-jasmine',
            'karma-chrome-launcher',
            'karma-phantomjs-launcher',
            'karma-junit-reporter',
            'karma-coverage',
            'karma-babel-preprocessor',
            'karma-handlebars-preprocessor'
        ],
        coverageReporter : {
            type : 'html',
            dir  : 'target/coverage/'
        }
    } );
};