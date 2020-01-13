#!/bin/bash

yarn install
yarn lint
yarn prebuild
yarn prebuild:css
yarn build:lib
yarn build:app
yarn inspect
yarn test
