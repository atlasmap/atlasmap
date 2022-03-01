/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import { languages } from 'monaco-editor';

export const atlasmapLanguageID = 'atlasmapLanguage';
export const atlasmapWordPattern =
  /[a-zA-Z0-9_\-*)|([^`~!@#%^&*()\-=+[{\]}\\|;:'",.<>/?\s]+/g;

/*
  Consider moving this and all Atlasmap language parsing logic to the backend
  and access it through a language server protocol.
  ref: https://github.com/atlasmap/atlasmap/issues/3138
*/
export const atlasmapLanguageConfig = {
  wordPattern: /(-?\d*\.\d\w*)|([^`~!@#%^&*()=+[{\]}\\|;:'",.<>/?\s]+)/g,
  comments: {
    // lineComment: '//',
    blockComment: ['/*', '*/'],
  },
  brackets: [
    ['{', '}'],
    ['[', ']'],
    ['(', ')'],
  ],
  onEnterRules: [
    {
      // e.g. /** | */
      beforeText: /^\s*\/\*\*(?!\/)([^*]|\*(?!\/))*$/,
      afterText: /^\s*\*\/$/,
      action: {
        indentAction: languages?.IndentAction.IndentOutdent,
        appendText: ' * ',
      },
    },
    {
      // e.g. /** ...|
      beforeText: /^\s*\/\*\*(?!\/)([^*]|\*(?!\/))*$/,
      action: {
        indentAction: languages?.IndentAction.None,
        appendText: ' * ',
      },
    },
    {
      // e.g.  * ...|
      beforeText: /^(\t|( ))* \*( ([^*]|\*(?!\/))*)?$/,
      action: {
        indentAction: languages?.IndentAction.None,
        appendText: '* ',
      },
    },
    {
      // e.g.  */|
      beforeText: /^(\t|( ))* \*\/\s*$/,
      action: {
        indentAction: languages?.IndentAction.None,
        removeText: 1,
      },
    },
  ],
  autoClosingPairs: [
    { open: '[', close: ']' },
    { open: '(', close: ')' },
    { open: '"', close: '"', notIn: ['string'] },
    { open: "'", close: "'", notIn: ['string', 'comment'] },
    { open: '`', close: '`', notIn: ['string', 'comment'] },
    { open: '/**', close: ' */', notIn: ['string'] },
  ],
  folding: {
    markers: {
      start: new RegExp('^\\s*//\\s*#?region\\b'),
      end: new RegExp('^\\s*//\\s*#?endregion\\b'),
    },
  },
};

export const atlasmapTokensProvider = {
  defaultToken: '',
  identifier: 'identifier',
  tokenPostfix: '',

  // io.atlasmap.actions
  actions: [
    // CollectionActions
    'copyTo',

    // DateFieldActions
    'addDays',
    'addSeconds',
    'currentDate',
    'currentTime',
    'dayOfMonth',
    'dayOfWeek',
    'dayOfYear',

    // ExpressionFieldAction
    'process',

    // NumberFieldActions
    'absoluteValue',
    'add',
    'average',
    'ceiling',
    'convertMassUnit',
    'convertDistanceUnit',
    'convertAreaUnit',
    'convertVolumeUnit',
    'divide',
    'doMultiply',
    'floor',
    'maximum',
    'minimum',
    'multiply',
    'requiresDoubleResult',
    'round',
    'subtract',
    'warnIgnoringValue',

    // ObjectFieldActions
    'contains',
    'count',
    'equals',
    'isNull',
    'itemAt',
    'length',
    'collectionContains',

    // StringComplexFieldActions
    'append',
    'concatenate',
    'doSubString',
    'endsWith',
    'format',
    'genareteUUID',
    'indexOf',
    'lastIndexOf',
    'padStringLeft',
    'padStringRight',
    'prepend',
    'replaceAll',
    'replaceFirst',
    'split',
    'repeat',
    'startsWith',
    'subString',
    'subStringAfter',
    'subStringBefore',

    // StringSimpleFieldActions
    'capitalize',
    'fileExtension',
    'lowercase',
    'lowercaseChar',
    'normalize',
    'removeFileExtension',
    'separateByDash',
    'separateByUnderscore',
    'trimLeft',
    'trimRight',
    'uppercase',
    'uppercaseChar',
  ],

  keywords: [
    'boolean',
    'BOOLEAN',
    'char',
    'CHAR',
    'double',
    'DOUBLE',
    'false',
    'FALSE',
    'filter',
    'FILTER',
    'float',
    'FLOAT',
    'if',
    'IF',
    'int',
    'INT',
    'isempty',
    'ISEMPTY',
    'lt',
    'LT',
    'null',
    'NULL',
    'select',
    'SELECT',
    'short',
    'SHORT',
    'tolower',
    'TOLOWER',
    'true',
    'TRUE',
  ],

  operators: [
    '=',
    '>',
    '<',
    '!',
    '~',
    '?',
    ':',
    '==',
    '<=',
    '>=',
    '!=',
    '&&',
    '||',
    '+',
    '-',
    '*',
    '/',
    '&',
    '|',
    '^',
    '%',
  ],

  // we include these common regular expressions
  symbols: /[=><!~?:&|+\-*/^%]+/,
  escapes:
    /\\(?:[abfnrtv\\"']|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,
  digits: /\d+(_+\d+)*/,
  octaldigits: /[0-7]+(_+[0-7]+)*/,
  binarydigits: /[0-1]+(_+[0-1]+)*/,
  hexdigits: /[[0-9a-fA-F]+(_+[0-9a-fA-F]+)*/,

  // The main tokenizer AtlasMap tokens provider.
  tokenizer: {
    root: [
      // numbers
      [/(@digits)[eE]([-+]?(@digits))?[fFdD]?/, 'number.float'],
      [/(@digits)\.(@digits)([eE][-+]?(@digits))?[fFdD]?/, 'number.float'],
      [/0[xX](@hexdigits)[Ll]?/, 'number.hex'],
      [/0(@octaldigits)[Ll]?/, 'number.octal'],
      [/0[bB](@binarydigits)[Ll]?/, 'number.binary'],
      [/(@digits)[fFdD]/, 'number.float'],
      [/(@digits)[lL]?/, 'number'],

      // action functions, identifiers and keywords
      [
        /[a-zA-Z]+[a-zA-Z0-9_\-$][\w$]*/,
        {
          cases: {
            '@actions': { token: 'action.$0' },
            '@keywords': { token: 'keyword.$0' },
            '@default': 'identifier',
          },
        },
      ],

      // whitespace
      { include: '@whitespace' },

      // delimiters and operators
      [/[{}()[\]]/, '@brackets'],
      [/[<>](?!@symbols)/, '@brackets'],
      [
        /@symbols/,
        {
          cases: {
            '@operators': 'delimiter',
            '@default': '',
          },
        },
      ],

      // delimiter: after number because of .\d floats
      [/[;,.]/, 'delimiter'],

      // strings
      [/'([^'\\]|\\.)*$/, 'string.invalid'], // non-teminated string
      [/'/, { token: 'string.quote', bracket: '@open', next: '@string' }],
    ],

    whitespace: [
      [/[ \t\r\n]+/, ''],
      [/\/\*/, 'comment', '@comment'],
      // [/\/\/.*$/, 'comment'],
    ],

    comment: [
      [/[^/*]+/, 'comment'],
      // [/\/\*/, 'comment', '@push' ],    // nested comment not allowed :-(
      // [/\/\*/,    'comment.invalid' ],    // this breaks block comments in the shape of /* //*/
      [/\*\//, 'comment', '@pop'],
      [/[/*]/, 'comment'],
    ],

    string: [
      [/[^\\']+/, 'string'],
      [/@escapes/, 'string.escape'],
      [/\\./, 'string.escape.invalid'],
      [/'/, 'string', '@pop'],
    ],
  },
};
