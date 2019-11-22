import { css, StyleSheet } from '@patternfly/react-styles';
import React, { FunctionComponent } from 'react';

const styles = StyleSheet.create({
  content: {
    overflowY: 'auto',
    overflowX: 'hidden',
    flex: '1',
    height: '100%',
  },
});

export const MappingList: FunctionComponent = ({ children }) => (
  <div
    className={css(styles.content)}
  >
    { children }
  </div>
);
