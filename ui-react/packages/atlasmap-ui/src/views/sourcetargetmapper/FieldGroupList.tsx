import { Accordion } from '@patternfly/react-core';
import { css, StyleSheet } from '@patternfly/react-styles';
import React, { FunctionComponent, ReactElement, useRef } from 'react';

const styles = StyleSheet.create({
  content: {
    overflowY: 'auto',
    overflowX: 'hidden',
    fontSize: 'inherit',
    direction: 'rtl',
    marginBottom: '1rem',
    maxHeight: '100%',
    minHeight: '40px',
    flex: '0 1 auto',
  },
  accordion: {
    padding: 0
  }
});

export interface IFieldGroupList {
  children: (props: { ref: HTMLElement | null }) => ReactElement;
}

export const FieldGroupList: FunctionComponent<IFieldGroupList> = ({ children }) => {
  const ref = useRef<HTMLDivElement | null>(null);
  return (
    <div ref={ref} className={css(styles.content)}>
      <Accordion asDefinitionList={false} className={css(styles.accordion)}>
        {children({ ref: ref.current })}
      </Accordion>
    </div>
  );
}
