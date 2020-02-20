import React, { FunctionComponent, MouseEvent, ReactElement } from "react";

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
  highlightedMappingId?: string;
  onMouseOver: (mapping: IAtlasmapMapping) => void;
  onMouseOut: () => void;
  onSelectMapping: (mapping: IAtlasmapMapping) => void;
}

export const SourceTargetLinks: FunctionComponent<ISourceTargetLinksProps> = ({
  mappings,
  selectedMappingId,
  highlightedMappingId,
  onSelectMapping,
  onMouseOver,
  onMouseOut,
}) => {
  const links = mappings.reduce<ReactElement[]>((lines, m) => {
    const handleMouseOver = () => onMouseOver(m);
    const handleMouseOut = () => onMouseOut();
    const handleClick = (event: MouseEvent) => {
      onSelectMapping(m);
      event.stopPropagation();
    };

    const isHighlighted = m.id === highlightedMappingId;
    const isSelected = m.id === selectedMappingId;
    const color = isSelected
      ? "var(--pf-global--active-color--100)"
      : isHighlighted
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
            onClick={handleClick}
            onMouseOver={handleMouseOver}
            onMouseOut={handleMouseOut}
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
