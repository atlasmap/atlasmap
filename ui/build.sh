#!/bin/bash

yarn install
yarn lint
yarn build:lib
yarn inspect
yarn test
