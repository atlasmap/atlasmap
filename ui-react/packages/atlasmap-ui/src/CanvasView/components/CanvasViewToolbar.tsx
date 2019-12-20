import React, { FunctionComponent } from 'react';
import {
  Button,
  TextInput,
  Toolbar,
  ToolbarGroup,
  ToolbarItem,
} from '@patternfly/react-core';
import { css, StyleSheet } from '@patternfly/react-styles';

const styles = StyleSheet.create({
  toolbar: { borderBottom: '1px solid #ccc' },
  toolbarItem: { flex: 1}
});

export const CanvasViewToolbar: FunctionComponent = () => {
  return (
    <Toolbar
      className={css('view-toolbar pf-u-px-md pf-u-py-md', styles.toolbar)}
    >
      <ToolbarGroup className={css(styles.toolbarItem)}>
        <ToolbarItem>
          <Button
            variant={'plain'}
            aria-label="Enable/ Disable conditional mapping expression"
            disabled={true}
          >
            <i>
              f<sub>(x)</sub>
            </i>
          </Button>
        </ToolbarItem>

        <ToolbarItem className={css(styles.toolbarItem)}>
          <TextInput aria-label={'Conditional mapping expression'} />
        </ToolbarItem>
      </ToolbarGroup>
    </Toolbar>
  );
};
