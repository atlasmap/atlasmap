/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import {
  Bullseye,
  Button,
  EmptyState,
  EmptyStateIcon,
  EmptyStateVariant,
  Level,
  LevelItem,
  Title,
  Tooltip,
} from '@patternfly/react-core';
import {
  ICell,
  IRowData,
  Table,
  TableBody,
  TableHeader,
  textCenter,
} from '@patternfly/react-table';
import { PlusIcon, TableIcon } from '@patternfly/react-icons';
import React, { FunctionComponent, ReactElement } from 'react';

import { Actions } from '../UI';
import { IAtlasmapDocument } from '.';
import { MainContent } from '../Layout';
import styles from './NamespaceTableView.module.css';

export interface INamespaceTableProps {
  sources: IAtlasmapDocument[];
  onCreateNamespace: (docName: string) => void;
  onEditNamespace: (
    docName: string,
    alias: string,
    uri: string,
    locationUri: string,
    isTarget: boolean,
  ) => void;
  onDeleteNamespace: (docName: string, alias: string) => void;
}

export const NamespaceTableView: FunctionComponent<INamespaceTableProps> = ({
  sources,
  onCreateNamespace,
  onEditNamespace,
  onDeleteNamespace,
}) => {
  const columns = [
    'Alias',
    'URI',
    'Location URI',
    {
      title: 'Target Namespace',
      transforms: [textCenter],
      cellTransforms: [textCenter],
    },
  ];

  const namespaceTables: ReactElement[] = sources.reduce(
    (xmlSources: ReactElement[], source: IAtlasmapDocument) => {
      if (source.type !== 'XML') {
        return xmlSources;
      }

      const rows =
        !source.namespaces || source.namespaces.length === 0
          ? [
              {
                cells: ['<None>'],
              },
            ]
          : source.namespaces.map((namespace) => {
              return {
                cells: [
                  { title: namespace.alias },
                  { title: namespace.uri },
                  { title: namespace.locationUri },
                  {
                    title: namespace.isTarget ? '\u2713' : '',
                  },
                ],
              };
            });

      const actions = [
        {
          title: 'Edit',
          onClick: (_event: any, _rowId: any, row: IRowData, _extra: any) => {
            onEditNamespace(
              source.name,
              (row.cells?.[0] as ICell).title as string,
              (row.cells?.[1] as ICell).title as string,
              (row.cells?.[2] as ICell).title as string,
              ((row.cells?.[3] as ICell).title as string).length > 0
                ? true
                : false,
            );
          },
        },
        {
          title: 'Remove',
          onClick: (_event: any, _rowId: any, row: any, _extra: any) =>
            onDeleteNamespace(source.name, row.alias.title),
        },
      ];

      return [
        ...xmlSources,
        <Level key="level">
          <LevelItem key="title">
            <Title size="lg" headingLevel={'h1'} className={styles.title}>
              Namespaces for {source.name}
            </Title>
          </LevelItem>
          <LevelItem key="actions">
            <Actions>
              <Tooltip
                position={'top'}
                enableFlip={true}
                content={<div>Create a namespace</div>}
                key={'create-namespace'}
              >
                <Button
                  onClick={() => onCreateNamespace(source.name)}
                  variant={'plain'}
                  aria-label="Create a namespace"
                  data-testid="create-namespace-button"
                >
                  <PlusIcon />
                </Button>
              </Tooltip>
            </Actions>
          </LevelItem>
        </Level>,
        <Table
          key="table"
          aria-label="Namespaces"
          cells={columns}
          rows={rows}
          actions={
            source.namespaces && source.namespaces.length > 0
              ? actions
              : undefined
          }
        >
          <TableHeader />
          <TableBody />
        </Table>,
      ];
    },
    [],
  );

  return namespaceTables.length === 0 ? (
    <Bullseye>
      <EmptyState variant={EmptyStateVariant.small}>
        <EmptyStateIcon icon={TableIcon} />
        <Title headingLevel="h2" size="lg">
          No XML sources found
        </Title>
      </EmptyState>
    </Bullseye>
  ) : (
    <MainContent>{namespaceTables}</MainContent>
  );
};
