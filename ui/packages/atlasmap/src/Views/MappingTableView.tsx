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
  EmptyState,
  EmptyStateIcon,
  EmptyStateVariant,
  Title,
} from '@patternfly/react-core';
import { DocumentFieldPreview, DocumentFieldPreviewResults } from '../UI';
import { IAtlasmapField, IAtlasmapMapping } from '../Views';
import {
  ICell,
  IRow,
  Table,
  TableBody,
  TableHeader,
} from '@patternfly/react-table';
import React, { FunctionComponent, KeyboardEvent, MouseEvent } from 'react';

import { MainContent } from '../Layout';
import { TableIcon } from '@patternfly/react-icons';
import styles from './MappingTableView.module.css';

const emptyContent = [
  {
    heightAuto: true,
    cells: [
      {
        props: { colSpan: 8 },
        title: (
          <Bullseye>
            <EmptyState variant={EmptyStateVariant.small}>
              <EmptyStateIcon icon={TableIcon} />
              <Title headingLevel="h2" size="lg">
                No mappings found
              </Title>
            </EmptyState>
          </Bullseye>
        ),
      },
    ],
  },
];

export interface IMappingTableProps {
  mappings: Array<IAtlasmapMapping>;
  onSelectMapping: (mapping: IAtlasmapMapping) => void;
  shouldShowMappingPreview: (field: IAtlasmapMapping) => boolean;
  onFieldPreviewChange: (field: IAtlasmapField, value: string) => void;
}

export const MappingTableView: FunctionComponent<IMappingTableProps> = ({
  mappings,
  onSelectMapping,
  shouldShowMappingPreview,
  onFieldPreviewChange,
}) => {
  const rows =
    mappings.length === 0
      ? emptyContent
      : mappings.map((mapping) => {
          const sources = mapping.sourceFields.map((source, index) => {
            const field: IAtlasmapField = source;
            const { name } = field;
            return (
              <div key={index}>
                {name}
                {shouldShowMappingPreview(mapping) && (
                  <DocumentFieldPreview
                    id={field.id}
                    value={field.value}
                    onChange={(value) => onFieldPreviewChange(field, value)}
                  />
                )}
              </div>
            );
          });
          const targets = mapping.targetFields.map((target, index) => {
            const field: IAtlasmapField = target;
            const { name } = field;
            return (
              <div key={index}>
                {name}
                {shouldShowMappingPreview(mapping) && (
                  <DocumentFieldPreviewResults
                    id={field.id}
                    value={field.value}
                  />
                )}
              </div>
            );
          });
          return {
            cells: [
              {
                title: sources,
                data: mapping.id,
              },
              {
                title: targets,
              },
              {
                title: mapping.name,
              },
            ],
          };
        });

  const columns = ['Sources', 'Targets', 'Types'];

  const handleSelectMapping = (
    _event: MouseEvent | KeyboardEvent<Element>,
    row: IRow,
  ) => {
    const mapping: IAtlasmapMapping | undefined = mappings.find(
      (mapping) => (row.cells?.[0] as ICell).data === mapping.id,
    );
    if (mapping) {
      onSelectMapping(mapping);
    }
  };

  return (
    <MainContent>
      <Title size="lg" headingLevel={'h1'} className={styles.title}>
        Mappings
      </Title>
      <Table aria-label="Mappings" cells={columns} rows={rows}>
        <TableHeader />
        <TableBody onRowClick={handleSelectMapping} />
      </Table>
    </MainContent>
  );
};
