import { css, StyleSheet } from '@patternfly/react-styles';
import React, { FunctionComponent, ReactElement, useRef } from 'react';

const styles = StyleSheet.create({
  content: {
    overflowY: 'auto',
    overflowX: 'visible',
    flex: '1',
    height: '100%',
  },
});


export interface IMappingList {
  children: (props: { ref: HTMLElement | null }) => ReactElement;
}

export const MappingList: FunctionComponent<IMappingList> = ({ children }) => {
  const ref = useRef<HTMLDivElement | null>(null);
  return (
    <div
      className={css(styles.content)}
      ref={ref}
    >
      {children({ ref: ref.current })}
    </div>
  );
}
