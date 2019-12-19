import { configure, addDecorator, addParameters } from '@storybook/react';
import { withKnobs } from '@storybook/addon-knobs';
import { DocsPage, DocsContainer } from '@storybook/addon-docs/blocks';

import '@patternfly/react-core/dist/styles/base.css';
import './reset.css';

configure(require.context('../stories/', true, /\.stories\.(mdx|[tj]sx?)$/), module);

addDecorator(withKnobs);

addParameters({
  docs: {
    container: DocsContainer,
    page: DocsPage,
  },
});