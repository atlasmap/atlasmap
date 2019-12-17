import {
  Accordion,
  BaseSizes,
  Button,
  PageSection,
  Stack,
  StackItem,
  Title,
  TitleLevel,
  Tooltip,
} from '@patternfly/react-core';
import { FunctionComponent } from 'react';
import React from 'react';
import { css, StyleSheet } from '@patternfly/react-styles';

const styles = StyleSheet.create({
  content: {
    height: '100%',
    overflow: 'auto'
  },
  accordion: {
    padding: 0
  },
});
export interface IMappingDetailsProps {
  onDelete: () => void;
  onClose: () => void;
}

export const MappingDetails: FunctionComponent<IMappingDetailsProps> = ({
  onDelete,
  onClose,
  children,
}) => {
  return (
    <Stack>
      <StackItem>
        <PageSection>
          <Title headingLevel={TitleLevel.h1} size={BaseSizes['2xl']}>
            Mapping details
          </Title>
        </PageSection>
      </StackItem>
      <StackItem isFilled={true} className={css(styles.content)}>
        <Accordion
          asDefinitionList={false}
          noBoxShadow={true}
          className={css(styles.accordion)}
        >
          {children}
        </Accordion>
      </StackItem>
      <StackItem>
        <PageSection>
          <Button onClick={onClose} variant={'secondary'}>
            Close
          </Button>{' '}
          <Tooltip
            position={'auto'}
            enableFlip={true}
            content={<div>Remove the current mapping</div>}
          >
            <Button variant={'danger'} onClick={onDelete}>
              Remove
            </Button>
          </Tooltip>
        </PageSection>
      </StackItem>
    </Stack>
  );
};
