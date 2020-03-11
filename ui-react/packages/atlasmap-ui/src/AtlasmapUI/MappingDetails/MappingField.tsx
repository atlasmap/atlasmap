import {
  DataList,
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
  bolt: {
    padding: 5,
    fontSize: 'small',
  },
  dataListContent: {
    boxShadow: 'none',
    fontSize: 'small',
    fontWeight: 'bold',
  },
  indexInput: {
    background: 'transparent',
    color: 'inherit',
    border: '0 none',
    width: 30,
    fontSize: 'small',
  },
  icon: {
    color: 'blue',
    fontSize: 'small',
    fontWeight: 'bold',
    marginLeft: 5,
  },
  link: {
    padding: 0,
    fontSize: 'small',
    marginRight: 5,
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
    <DataList aria-label="dataList">
      <DataListItem
        className={css(styles.dataListContent)}
        aria-labelledby={id}
      >
        <Split>
          <SplitItem isFilled>
            <Stack>
              <StackItem isFilled />
              <StackItem>
                <Title
                  className={css(styles.dataListContent)}
                  size={'sm'}
                  headingLevel={'h3'}
                  id={id}
                >
                  <Tooltip
                    position={'auto'}
                    enableFlip={true}
                    content={<div>{info}</div>}
                  >
                    <span>
                      <InfoAltIcon className={css(styles.icon)} /> {name}
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
                content={
                  <div>
                    Edit the index for this element by selecting the arrows.
                    Placeholders may be automatically inserted to account for
                    any gaps in the indexing
                  </div>
                }
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
            <Stack>
              <StackItem isFilled />
              <StackItem>
                <Button
                  variant={'link'}
                  onClick={onDelete}
                  className={css(styles.link)}
                >
                  Remove
                </Button>
              </StackItem>
              <StackItem isFilled />
            </Stack>
          </SplitItem>
        </Split>
        {/*
        Show established field action transformations associated with this
        field.
        */}
        {Children.count(children) > 0 && (
          <DataListContent
            aria-label={'Field transformations'}
            className={css(styles.dataListContent)}
          >
            <Title
              className={css(styles.dataListContent)}
              size={'sm'}
              headingLevel={'h4'}
            >
              Transformations
            </Title>
            {children}
          </DataListContent>
        )}
      </DataListItem>
      <DataListItem
        className={css(styles.dataListContent)}
        aria-labelledby={id}
      >
        <Split>
          <StackItem>
            <Button
              isDisabled={!canEditIndex}
              variant={'link'}
              onClick={onNewTransformation}
              className={css(styles.link)}
            >
              <BoltIcon />
              Add Transformation
            </Button>
          </StackItem>
        </Split>
      </DataListItem>
    </DataList>
  );
};
