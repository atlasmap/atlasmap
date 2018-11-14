#!/bin/bash

yarn install
yarn lint
yarn build:lib
yarn build:app
yarn inspect
yarn test
