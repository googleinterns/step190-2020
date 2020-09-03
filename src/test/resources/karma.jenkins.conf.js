module.exports = function ( config ) {
    config.set( {
        basePath         : '../../../',
        frameworks       : ['jasmine', 'jquery-1.8.3'],
        files            : [
            'src/main/webapp/**/*.js',
            'src/main/webapp/**/*.html',
            'src/test/js/**/*.js'
        ],
        exclude          : [],
        preprocessors    : {
            'src/main/webapp/**/*.js' : ['coverage'],
            'src/main/webapp/**/*.js' : ['babel'],
            'src/test/js/**/*.js' : ['babel'],
            'src/main/webapp/**/*.html' : ['html2js'],
            '**/*.hbs': ['handlebars']
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
        html2JsPreprocessor: {
            processPath: function(filePath) {
              // Drop the file extension
              return filePath.replace(/\.html$/, '');
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
        // added `junit`
        reporters        : ['progress', 'junit', 'coverage'],
        port             : 9876,
        colors           : true,
        logLevel         : config.LOG_INFO,
        // don't watch for file change
        autoWatch        : false,
        // only runs on headless browser
        browsers         : [], // ['PhantomJS'],
        // just run one time
        singleRun        : true,
        // remove `karma-chrome-launcher` because we will be running on headless
        // browser on Jenkins
        plugins          : [
            'karma-jasmine',
            'karma-phantomjs-launcher',
            'karma-junit-reporter',
            'karma-coverage',
            'karma-babel-preprocessor',
            'karma-handlebars-preprocessor',
            'karma-html2js-preprocessor',
            'karma-jquery'
        ],
        // changes type to `cobertura`
        coverageReporter : {
            type : 'cobertura',
            dir  : 'target/coverage-reports/'
        },
        // saves report at `target/surefire-reports/TEST-*.xml` because Jenkins
        // looks for this location and file prefix by default.
        junitReporter    : {
            outputFile : 'target/surefire-reports/TEST-karma-results.xml'
        }
    } );
};