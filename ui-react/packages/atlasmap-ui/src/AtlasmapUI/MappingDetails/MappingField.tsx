import {
  DataListContent,
  DataListItem,
  Label,
  Title,
  Tooltip,
  Button,
  Split,
  SplitItem,
  Stack,
  StackItem,
} from '@patternfly/react-core';
import React, { FunctionComponent, Children } from 'react';
import { BoltIcon, InfoAltIcon } from '@patternfly/react-icons';
import { css, StyleSheet } from '@patternfly/react-styles';

const styles = StyleSheet.create({
  dataListContent: { boxShadow: 'none' },
  indexInput: {
    background: 'transparent',
    color: 'inherit',
    border: '0 none',
    width: 40,
  },
  remove: {
    padding: 0,
  },
  bolt: {
    padding: 5,
  },
});

export interface IMappingFieldProps {
  name: string;
  info: string;
  index: number;
  showIndex: boolean;
  canEditIndex: boolean;
  onDelete: () => void;
  onIndexChange: (event: any) => void;
  onNewTransformation: () => void;
}

export const MappingField: FunctionComponent<IMappingFieldProps> = ({
  name,
  info,
  index,
  showIndex,
  canEditIndex,
  onDelete,
  onIndexChange,
  onNewTransformation,
  children,
}) => {
  const id = `mapping-field-${name}`;
  return (
    <DataListItem aria-labelledby={id}>
      <Split>
        <SplitItem isFilled>
          <Stack>
            <StackItem isFilled />
            <StackItem>
              <Title size={'md'} headingLevel={'h3'} id={id}>
                <Tooltip
                  position={'auto'}
                  enableFlip={true}
                  content={<div>{info}</div>}
                >
                  <span>
                    <InfoAltIcon /> {name}
                  </span>
                </Tooltip>
              </Title>
            </StackItem>
            <StackItem isFilled />
          </Stack>
        </SplitItem>
        <SplitItem>
          {showIndex && (
            <Tooltip
              position={'auto'}
              enableFlip={true}
              content={<div>Edit the index for this element by selecting the arrows. 
                Placeholders may be automatically inserted to account for any gaps in the indexing</div>}
            >
              <Label>
                #{' '}
                <input
                  type={'number'}
                  value={index}
                  id={'index'}
                  disabled={!canEditIndex}
                  className={css(styles.indexInput)}
                  onChange={onIndexChange}
                />
              </Label>
            </Tooltip>
          )}
        </SplitItem>
        <SplitItem>
          <Tooltip
            position={'auto'}
            enableFlip={true}
            content={<div>Add a new field transformation</div>}
          >
            <Button
              isDisabled={!canEditIndex}
              variant={'link'}
              onClick={onNewTransformation}
              className={css(styles.bolt)}
            >
              <BoltIcon />
            </Button>
          </Tooltip>
        </SplitItem>
        <SplitItem>
          <Stack>
            <StackItem isFilled />
            <StackItem>
              <Button
                variant={'link'}
                onClick={onDelete}
                className={css(styles.remove)}
              >
                Remove
              </Button>
            </StackItem>
            <StackItem isFilled />
        </Stack>
        </SplitItem>
      </Split>

      // Show established field action transformations associated with this field.
      {Children.count(children) > 0 && (
        <DataListContent
          aria-label={'Field transformations'}
          className={css(styles.dataListContent)}
        >
          <Title size={'xs'} headingLevel={'h4'}>Transformations</Title>
          {children}
        </DataListContent>
      )}
    </DataListItem>
  );
};