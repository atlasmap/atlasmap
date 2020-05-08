import React, {
  FunctionComponent,
  MouseEvent,
  ReactElement,
  useMemo,
} from "react";

import { css, StyleSheet } from "@patternfly/react-styles";

import { NodesArc } from "../../../UI";
import { IAtlasmapMapping } from "../../../Views";
import { SOURCES_FIELD_ID_PREFIX, TARGETS_FIELD_ID_PREFIX } from "../Columns";

const styles = StyleSheet.create({
  arc: {
    cursor: "pointer",
  },
});

export interface ISourceTargetLinksProps {
  mappings: IAtlasmapMapping[];
  selectedMappingId?: string;
  onSelectMapping: (mapping: IAtlasmapMapping) => void;
}

export const SourceTargetLinks: FunctionComponent<ISourceTargetLinksProps> = ({
  mappings,
  selectedMappingId,
  onSelectMapping,
}) => {
  const links = useMemo(() => {
    return mappings.reduce<ReactElement[]>((lines, m) => {
      const handleClick = (event: MouseEvent) => {
        onSelectMapping(m);
        event.stopPropagation();
      };

      const isSelected = m.id === selectedMappingId;
      const color = isSelected
        ? "var(--pf-global--active-color--100)"
        : undefined;
      const hoverColor = !isSelected
        ? "var(--pf-global--active-color--400)"
        : undefined;
      const mappingLines = m.sourceFields.reduce<ReactElement[]>(
        (lines, start) => {
          const linesFromSource = m.targetFields.map((end) => (
            <NodesArc
              key={`${start.id}${end.id}`}
              start={`${SOURCES_FIELD_ID_PREFIX}${start.id}`}
              end={`${TARGETS_FIELD_ID_PREFIX}${end.id}`}
              color={color}
              hoveredColor={hoverColor}
              onClick={handleClick}
              className={css(styles.arc)}
            />
          ));
          return [...lines, ...linesFromSource];
        },
        [],
      );
      return isSelected
        ? [...lines, ...mappingLines]
        : [...mappingLines, ...lines];
    }, []);
  }, [mappings, onSelectMapping, selectedMappingId]);

  return (
    <>
      {links}

      <NodesArc
        start={"dnd-start"}
        end={"dnd-target-field"}
        color={"var(--pf-global--active-color--400)"}
      />
    </>
  );
};
