import { Accordion } from '@patternfly/react-core';
import { css, StyleSheet } from '@patternfly/react-styles';
import React, { FunctionComponent } from 'react';

const styles = StyleSheet.create({
  content: {
    overflowY: 'auto',
    overflowX: 'hidden',
    flex: '1',
    height: '100%',
    fontSize: 'inherit',
    direction: 'rtl'
  },
});

export const FieldGroupList: FunctionComponent = ({ children }) => (
  <Accordion asDefinitionList={false} className={css(styles.content)}>
    {children}
  </Accordion>
);
