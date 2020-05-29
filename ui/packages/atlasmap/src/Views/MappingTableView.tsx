import React, { FunctionComponent, MouseEvent } from "react";

import {
  Bullseye,
  EmptyState,
  EmptyStateIcon,
  EmptyStateVariant,
  Title,
} from "@patternfly/react-core";
import { TableIcon } from "@patternfly/react-icons";
import { css, StyleSheet } from "@patternfly/react-styles";
import {
  Table,
  TableBody,
  TableHeader,
  IRow,
  ICell,
} from "@patternfly/react-table";

import { MainContent } from "../Layout";
import { IAtlasmapField, IAtlasmapMapping } from "../Views";

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

const styles = StyleSheet.create({
  title: { marginBottom: "1rem" },
});

export interface IMappingTableProps {
  mappings: IAtlasmapMapping[];
  onSelectMapping: (mapping: IAtlasmapMapping) => void;
}

export const MappingTableView: FunctionComponent<IMappingTableProps> = ({
  mappings,
  onSelectMapping,
}) => {
  const rows =
    mappings.length === 0
      ? emptyContent
      : mappings.map((mapping) => {
          const sources = mapping.sourceFields.map((source) => {
            const { name } = source as IAtlasmapField;
            return <div key={name}>{name}</div>;
          });
          const targets = mapping.targetFields.map((target) => {
            const { name } = target as IAtlasmapField;
            return <div key={name}>{name}</div>;
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

  const columns = ["Sources", "Targets", "Types"];

  const handleSelectMapping = (_event: MouseEvent, row: IRow) => {
    const mapping: IAtlasmapMapping | undefined = mappings.find(
      (mapping) => (row.cells?.[0] as ICell).data === mapping.id,
    );
    if (mapping) {
      onSelectMapping(mapping);
    }
  };

  return (
    <MainContent>
      <Title size={"lg"} headingLevel={"h1"} className={css(styles.title)}>
        Mappings
      </Title>
      <Table aria-label="Mappings" cells={columns} rows={rows}>
        <TableHeader />
        <TableBody onRowClick={handleSelectMapping} />
      </Table>
    </MainContent>
  );
};
