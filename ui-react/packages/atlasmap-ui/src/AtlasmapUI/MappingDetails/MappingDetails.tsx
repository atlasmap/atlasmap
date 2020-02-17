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
  Level,
  LevelItem,
} from '@patternfly/react-core';
import { FunctionComponent } from 'react';
import React from 'react';
import { css, StyleSheet } from '@patternfly/react-styles';
import { TrashIcon } from '@patternfly/react-icons';

const styles = StyleSheet.create({
  content: {
    height: '100%',
    overflow: 'auto',
    fontSize: 'small',
  },
  accordion: {
    padding: 0,
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
          <Level>
            <LevelItem>
              <Title headingLevel={TitleLevel.h1} size={BaseSizes['2xl']}>
                Mapping Details
              </Title>
            </LevelItem>
            <LevelItem>
              <Tooltip
                position={'auto'}
                enableFlip={true}
                content={<div>Remove the current mapping</div>}
              >
                <Button variant={'link'} onClick={onDelete}>
                  <TrashIcon />
                </Button>
              </Tooltip>
            </LevelItem>
          </Level>
        </PageSection>
      </StackItem>
      <StackItem isFilled={true} className={css(styles.content)}>
        <Accordion asDefinitionList={false} className={css(styles.accordion)}>
          {children}
        </Accordion>
      </StackItem>
      <StackItem>
        <PageSection>
          <Button onClick={onClose} variant={'primary'}>
            Close
          </Button>{' '}
        </PageSection>
      </StackItem>
    </Stack>
  );
};
