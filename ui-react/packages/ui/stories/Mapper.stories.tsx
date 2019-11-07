import React from 'react';
import { Mapper } from '@src';

export default {
  title: 'Mapper',
}

const sources = [
  'A',
  'a',
  'B',
  'b',
  'C',
  'c',
  'D',
  'd',
  'E',
  'e',
  'F',
  'f',
  'G',
  'g',
  'H',
  'h',
  'I',
  'i',
  'J',
  'j',
  'K',
  'k',
  'L',
  'l',
  'M',
  'm',
  'N',
  'n',
  'O',
  'o',
  'P',
  'p',
  'Q',
  'q',
  'R',
  'r',
  'S',
  's',
  'T',
  't',
  'U',
  'u',
  'V',
  'v',
  'W',
  'w',
  'X',
  'x',
  'Y',
  'y',
  'Z',
  'z',
];
const targets = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10'];
const mappings = [
  {
    id: 'a',
    sourceFields: ['A'],
    targetFields: ['10']
  }, {
    id: 'b',
    sourceFields: ['b', 'C', 'Z'],
    targetFields: ['1']
  }, {
    id: 'c',
    sourceFields: ['h'],
    targetFields: ['3', '4', '5']
  }, {
    id: 'd',
    sourceFields: ['n'],
    targetFields: ['7']
  }, {
    id: 'e',
    sourceFields: ['F'],
    targetFields: ['2']
  }
];

export const sample = () => (
  <Mapper
    sources={[{
      title: 'Foo',
      fields: sources.map(s => ({
        id: s,
        element: <p>{s}</p>
      }))
    }]}
    targets={[{
      title: 'Bar',
      fields: targets.map(s => ({
        id: s,
        element: <p>{s}</p>
      }))
    }]}
    mappings={mappings}
  />
);
