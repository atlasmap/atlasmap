import React from "react";
import { addDecorator, addParameters } from '@storybook/react';
import { withKnobs } from '@storybook/addon-knobs';
import { DocsPage, DocsContainer } from '@storybook/addon-docs/blocks';

import '@patternfly/react-core/dist/styles/base.css';
import './reset.css';

addDecorator(withKnobs);
addDecorator(storyFn => <div className="pf-m-redhat-font story-wrapper">{storyFn()}</div>)

addParameters({
  docs: {
    container: DocsContainer,
    page: DocsPage,
  },
});