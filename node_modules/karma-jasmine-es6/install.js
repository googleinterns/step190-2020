/* eslint-env node */
// https://github.com/jprichardson/node-fs-extra/blob/HEAD/docs/ensureSymlink-sync.md

const fs = require('fs-extra');

const srcpath = '../';
const dstpath = 'node_modules/karma-jasmine-es6';
fs.ensureSymlinkSync(srcpath, dstpath);
