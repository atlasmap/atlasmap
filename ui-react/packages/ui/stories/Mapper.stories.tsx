import React from 'react';
import { Mapper } from '@src';

export default {
  title: 'Mapper',
};

const sources = [
  {
    id: 'Foo',
    title: 'Foo',
    fields: [{ id: 'A', element: <p>A some random word</p> }, { id: 'a', element: <p>a some random word</p> }],
  },
  {
    id: 'Foo 1',
    title: 'Foo 1',
    fields: [
      { id: 'B', element: <p>B some random word</p> },
      { id: 'b', element: <p>b some random word</p> },
      {
        id: 'C',
        title: 'C',
        fields: [
          { id: 'C - 1', element: <p>C - 1 some random word</p> },
          {
            id: 'C - 2',
            title: 'C - 2',
            fields: [
              { id: 'C - 2 - a', element: <p>C - 2 - a some random word</p> },
              { id: 'C - 2 - b', element: <p>C - 2 - b some random word</p> },
            ]
          },
          { id: 'C - 3', element: <p>C - 3 some random word</p> },
          ]
      },
    ],
  },
  {
    id: 'Foo 2',
    title: 'Foo 2',
    fields: [{ id: 'c', element: <p>c some random word</p> }],
  },
  {
    id: 'Foo 3',
    title: 'Foo 3',
    fields: [
      { id: 'D', element: <p>D some random word</p> },
      { id: 'd', element: <p>d some random word</p> },
      { id: 'E', element: <p>E some random word</p> },
      { id: 'e', element: <p>e some random word</p> },
    ],
  },
  {
    id: 'Bar',
    title: 'Bar',
    fields: [{ id: 'F', element: <p>F some random word</p> }],
  },
  {
    id: 'Bar 1',
    title: 'Bar 1',
    fields: [
      { id: 'f', element: <p>f some random word</p> },
      { id: 'G', element: <p>G some random word</p> },
      { id: 'g', element: <p>g some random word</p> },
    ],
  },
  {
    id: 'Bar 2',
    title: 'Bar 2',
    fields: [
      { id: 'H', element: <p>H some random word</p> },
      { id: 'h', element: <p>h some random word</p> },
      { id: 'I', element: <p>I some random word</p> },
      { id: 'i', element: <p>i some random word</p> },
    ],
  },
  {
    id: 'Bar 3',
    title: 'Bar 3',
    fields: [{ id: 'J', element: <p>J some random word</p> }, { id: 'j', element: <p>j some random word</p> }],
  },
  {
    id: 'Bar 4',
    title: 'Bar 4',
    fields: [{ id: 'K', element: <p>K some random word</p> }, { id: 'k', element: <p>k some random word</p> }],
  },
  {
    id: 'Bar 5',
    title: 'Bar 5',
    fields: [
      { id: 'L', element: <p>L some random word</p> },
      { id: 'l', element: <p>l some random word</p> },
      { id: 'M', element: <p>M some random word</p> },
      { id: 'm', element: <p>m some random word</p> },
    ],
  },
  {
    id: 'Baz',
    title: 'Baz',
    fields: [
      { id: 'N', element: <p>N some random word</p> },
      { id: 'n', element: <p>n some random word</p> },
      { id: 'O', element: <p>O some random word</p> },
    ],
  },
  {
    id: 'Baz 1',
    title: 'Baz 1',
    fields: [
      { id: 'o', element: <p>o some random word</p> },
      { id: 'P', element: <p>P some random word</p> },
      { id: 'p', element: <p>p some random word</p> },
    ],
  },
  {
    id: 'Baz 2',
    title: 'Baz 2',
    fields: [{ id: 'Q', element: <p>Q some random word</p> }, { id: 'q', element: <p>q some random word</p> }],
  },
  {
    id: 'Baz 3',
    title: 'Baz 3',
    fields: [
      { id: 'R', element: <p>R some random word</p> },
      { id: 'r', element: <p>r some random word</p> },
      { id: 'S', element: <p>S some random word</p> },
    ],
  },
  {
    id: 'Baz 4',
    title: 'Baz 4',
    fields: [
      { id: 's', element: <p>s some random word</p> },
      { id: 'T', element: <p>T some random word</p> },
      { id: 't', element: <p>t some random word</p> },
      { id: 'U', element: <p>U some random word</p> },
    ],
  },
  {
    id: 'Baz 5',
    title: 'Baz 5',
    fields: [
      { id: 'u', element: <p>u some random word</p> },
      { id: 'V', element: <p>V some random word</p> },
      { id: 'v', element: <p>v some random word</p> },
    ],
  },
  {
    id: 'Baz 6',
    title: 'Baz 6',
    fields: [
      { id: 'W', element: <p>W some random word</p> },
      { id: 'w', element: <p>w some random word</p> },
      { id: 'X', element: <p>X some random word</p> },
    ],
  },
  {
    id: 'Baz 7',
    title: 'Baz 7',
    fields: [
      { id: 'x', element: <p>x some random word</p> },
      { id: 'Y', element: <p>Y some random word</p> },
      { id: 'y', element: <p>y some random word</p> },
      { id: 'Z', element: <p>Z some random word</p> },
      { id: 'z', element: <p>z some random word</p> },
    ],
  },
];
const targets = [
  {
    id: 'Qux',
    title: 'Qux',
    fields: [
      { id: '1', element: <p>1 some random word</p> },
      { id: '2', element: <p>2 some random word</p> },
      { id: '3', element: <p>3 some random word</p> },
      { id: '4', element: <p>4 some random word</p> },
      { id: '5', element: <p>5 some random word</p> },
    ],
  },
  {
    id: 'Quux',
    title: 'Quux',
    fields: [
      { id: '5', element: <p>5 some random word</p> },
      { id: '6', element: <p>6 some random word</p> },
      { id: '7', element: <p>7 some random word</p> },
      { id: '8', element: <p>8 some random word</p> },
      { id: '9', element: <p>9 some random word</p> },
      { id: '10', element: <p>10 some random word</p> },
    ],
  },
];
const mappings = [
  {
    id: 'a',
    sourceFields: ['A'],
    targetFields: ['10'],
  },
  {
    id: 'b',
    sourceFields: ['b', 'C - 1', 'C - 2 - a', 'Z'],
    targetFields: ['1'],
  },
  {
    id: 'c',
    sourceFields: ['h'],
    targetFields: ['3', '4', '5'],
  },
  {
    id: 'd',
    sourceFields: ['n', 'H'],
    targetFields: ['7'],
  },
  {
    id: 'e',
    sourceFields: ['F'],
    targetFields: ['7'],
  },
];

export const sample = () => (
  <Mapper sources={sources} targets={targets} mappings={mappings} />
);
