# User Guide

The recommended workflow is to run TSDX in one terminal:

```
yarn start
```

This builds to `/dist` and runs the project in watch mode so any edits you save inside `src` causes a rebuild to `/dist`.

Then run the example inside another:

```
cd example
yarn # to install dependencies
yarn start
```

To do a one-off build, use `yarn build`.

To run tests, use `yarn test`.