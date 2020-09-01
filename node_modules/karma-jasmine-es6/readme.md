# karma-jasmine-es6

The system here is a *hack* while waiting for karma to deliver a fix. A PR is currently under work here: [karma#2834](https://github.com/karma-runner/karma/pull/2834).

## Installation

This library has no runtime depandancies. But if you need to test web components, you probably will need the polyfills.

Install it

```lang=bash
npm install -D karma-jasmine-es6
```

## Usage in Karma

Add it as a "framework" in karma.conf.js:

```lang=javascript
module.exports = function(config) {
    config.set({
        frameworks : [
            'jasmine-es6',
            'jasmine'
        ],
    });
}
```

!! jasmine-es6 must come before 'jasmine', since jasmine-es6 will hook into window.__karma__.start and delay it the necessary time.

## Limits

This does not cover the case where the module is not loaded correctly. No message is logged by the plugin in that case, look at the console.

## Links with karma

I love the karma project, and that is why I did develop this module. It took me 30 minutes to make it, tests included, which is not a lot of time.

Could I fix the "es6 modules" PR myself? I tried, checkout the karma code, and had a look into it, but couldn't manage to fix the code after hours. So I let it go to people that
have great knowledge, and I deliver what I need: a fix today.

I say to the karma team: please fix karma to load es6 modules... That would be great ! And this module will then be not necessary anymore, and that would be a success for me.

## To test

To test this framework:

```lang=javascript
npm run link
npm run test
```
