import React, { ReactNode } from 'react';
import { FunctionComponent } from 'react';
import { TableIcon, SearchIcon } from '@patternfly/react-icons';
import { css, StyleSheet } from '@patternfly/react-styles';
import {
  Stack,
  StackItem,
  Split,
  SplitItem,
  Grid,
  GridItem,
} from '@patternfly/react-core';
import { useAtlasmapUI } from './AtlasmapUIProvider';
import { IAtlasmapField } from './models';

const styles = StyleSheet.create({
  table: {
    border: '1px solid #ccc',
    margin: '10px',
  },
  titleBar: {
    borderBottom: '1px solid #ccc',
    padding: '10px',
  },
  title: {
    paddingLeft: '10px',
    fontWeight: 'bold',
    fontSize: 'larger',
  },
  item: {
    borderBottom: '1px solid #ccc',
    padding: '10px',
  },
  header: {
    borderBottom: '1px solid #ccc',
    fontWeight: 'bold',
    padding: '10px',
  },
});

export interface IMappingTableProps {
  contextToolbar: ReactNode;
  viewToolbar: ReactNode;
}

export const MappingTable: FunctionComponent<IMappingTableProps> = ({
  contextToolbar,
  viewToolbar,
}) => {
  const { mappings } = useAtlasmapUI();

  const mappingRows: Array<ReactNode> = [];
  mappings.map((mapping, index) => {
    const sources = mapping.sourceFields.map(source => {
      const { name } = source as IAtlasmapField;
      return <div key={name}>{name}</div>;
    });
    const targets = mapping.targetFields.map(target => {
      const { name } = target as IAtlasmapField;
      return <div key={name}>{name}</div>;
    });
    mappingRows.push(
      <GridItem
        key={'mapping' + index + 'Sources'}
        className={css(styles.item)}
      >
        <div>{sources}</div>
      </GridItem>
    );
    mappingRows.push(
      <GridItem
        key={'mapping' + index + 'Targets'}
        className={css(styles.item)}
      >
        <div>{targets}</div>
      </GridItem>
    );
    mappingRows.push(
      <GridItem key={'mapping' + index + 'Type'} className={css(styles.item)}>
        {mapping.name}
      </GridItem>
    );
    return mappingRows;
  });

  return (
    <Stack>
      <StackItem>{contextToolbar}</StackItem>
      <StackItem>{viewToolbar}</StackItem>
      <StackItem className={css(styles.titleBar)}>
        <Split>
          <SplitItem>
            <TableIcon />
          </SplitItem>
          <SplitItem isFilled className={css(styles.title)}>
            Mappings
          </SplitItem>
          <SplitItem>
            <SearchIcon />
          </SplitItem>
        </Split>
      </StackItem>
      <StackItem>
        <Grid span={4} className={css(styles.table)}>
          <GridItem className={css(styles.header)}>Sources</GridItem>
          <GridItem className={css(styles.header)}>Targets</GridItem>
          <GridItem className={css(styles.header)}>Type</GridItem>
          {mappingRows}
        </Grid>
      </StackItem>
    </Stack>
  );
};
