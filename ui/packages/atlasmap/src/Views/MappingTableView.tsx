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
import { DocumentFieldPreview } from "../UI";

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
  const renderPreview = (
    mapping: IAtlasmapMapping,
    mappedField: IAtlasmapField,
  ) =>
    shouldShowMappingPreview(mapping) && (
      <DocumentFieldPreview
        id={mappedField.id}
        value={mappedField.previewValue}
        onChange={(value) => onFieldPreviewChange(mappedField, value)}
      />
    );

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
                {renderPreview(mapping, field)}
              </div>
            );
          });
          const targets = mapping.targetFields.map((target, index) => {
            const field: IAtlasmapField = target;
            const { name } = field;
            return (
              <div key={index}>
                {name}
                {renderPreview(mapping, field)}
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
