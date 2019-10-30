import * as React from 'react';
import { css, StyleSheet } from '@patternfly/react-styles';

const BoxStyles = StyleSheet.create({
  outer: {
    border: '1px solid black',
    width: 200,
    overflow: 'hidden',
    display: 'inline-block',
    background: 'white',
    position: 'absolute'
  },
  inner: {
  }
});

export const Box: React.FunctionComponent = ({ children }) => {
  return (
    <div className={css(BoxStyles.outer)}>
      <div className={css(BoxStyles.inner)}>
        {children}
      </div>
    </div>
  );
}
