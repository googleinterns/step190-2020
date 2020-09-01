expect = require('chai').expect

index = require('../index.js')

logger = {create: -> {debug: ->}}

describe 'preprocessor:handlebars', ->

  factory = index['preprocessor:handlebars'][1]

  describe 'factory', ->
    it 'should be a function', ->
      expect(factory).to.be.a('function')

    it 'should return a function', ->
      expect(factory({}, {}, logger, {})).to.be.a('function')


  describe 'preprocessor', ->
    it 'should use defaults', ->
      preprocessor = factory({}, {}, logger, {})

      done = (result) ->
        expect(result).to.include('Handlebars.templates')
        expect(result).to.include("Handlebars.templates['file']")
      file = {originalPath: 'folder/file.hbs'}

      preprocessor('', file, done)
      expect(file.path).to.eq('folder/file.js')

    it 'should have configurable templates', ->
      preprocessor = factory({}, {templates: 'HandlebarsTemplates'}, logger, {})

      done = (result) ->
        expect(result).to.include('HandlebarsTemplates')
      file = {originalPath: ''}

      preprocessor('', file, done)

    it 'should have configurable templateName', ->
      preprocessor = factory({}, {templateName: (filepath) -> filepath.replace(/^.*((\/[^\/]+){3})\.hbs$/, '$1').substr(1)}, logger, {})

      done = (result) ->
        expect(result).to.include("Handlebars.templates['albums/templates/albumtile']")
      file = {originalPath: 'app/assets/javascripts/modules/albums/templates/albumtile.hbs'}

      preprocessor('', file, done)

    it 'should have configurable transformPath', ->
      preprocessor = factory({}, {transformPath: (filepath) -> filepath.replace(/\.hbs$/, '.jsx')}, logger, {})

      done = (result) ->
      file = {originalPath: 'filename.hbs'}

      preprocessor('', file, done)
      expect(file.path).to.eq('filename.jsx')

    it 'should generate an anonymous amd module', ->
      preprocessor = factory({}, {amd: true}, logger, {})

      done = (result) ->
        expect(result).to.include("define(['handlebars'], function(Handlebars) {")
        expect(result).to.include("});")
      file = {originalPath: 'folder/file.hbs'}

      preprocessor('', file, done)
      expect(file.path).to.eq('folder/file.js')
