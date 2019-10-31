import React, { FunctionComponent } from 'react';
import { css, StyleSheet } from '@patternfly/react-styles';

const BoxStyles = StyleSheet.create({
  outer: {
    border: '1px solid black',
    width: 200,
    overflow: 'hidden',
    display: 'inline-block',
    background: 'white'
  },
  inner: {
  }
});

export interface IBoxProps {
  foo?: string;
}

/**
 * `Box` sample doc
 */
export const Box: FunctionComponent<IBoxProps> = ({ children }) => {
  return (
    <div className={css(BoxStyles.outer)}>
      <div className={css(BoxStyles.inner)}>
        {children}
      </div>
    </div>
  );
}
