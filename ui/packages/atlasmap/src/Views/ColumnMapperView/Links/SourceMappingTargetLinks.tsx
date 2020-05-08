import React, { FunctionComponent, MouseEvent } from "react";

import { css, StyleSheet } from "@patternfly/react-styles";

import { NodesArc } from "../../../UI";
import { IAtlasmapMapping } from "../../../Views";
import {
  MAPPINGS_DOCUMENT_ID_PREFIX,
  MAPPINGS_FIELD_ID_PREFIX,
  SOURCES_FIELD_ID_PREFIX,
  TARGETS_FIELD_ID_PREFIX,
} from "../Columns";

const styles = StyleSheet.create({
  arc: {
    cursor: "pointer",
  },
});

export interface ISourceMappingTargetLinksEvents {
  onMouseOver: (mapping: IAtlasmapMapping) => void;
  onMouseOut: () => void;
  onSelectMapping: (mapping: IAtlasmapMapping) => void;
}

export interface ISourceMappingTargetLinksData {
  mappings: IAtlasmapMapping[];
  selectedMappingId?: string;
  highlightedMappingId?: string;
}

export const SourceMappingTargetLinks: FunctionComponent<
  ISourceMappingTargetLinksData & ISourceMappingTargetLinksEvents
> = ({
  mappings,
  selectedMappingId,
  highlightedMappingId,
  onSelectMapping,
  onMouseOver,
  onMouseOut,
}) => {
  const links = mappings.map((m) => {
    const handleClick = (event: MouseEvent) => {
      onSelectMapping(m);
      event.stopPropagation();
    };
    const handleMouseOver = () => onMouseOver(m);
    const handleMouseOut = () => onMouseOut();
    const isHighlighted = m.id === highlightedMappingId;
    const isSelected = m.id === selectedMappingId;
    const color = isSelected
      ? "var(--pf-global--active-color--100)"
      : isHighlighted
      ? "var(--pf-global--active-color--400)"
      : undefined;
    return [
      ...m.sourceFields.map((s) => (
        <NodesArc
          key={`mapping-arc-from-${s.id}${m.id}`}
          start={`${SOURCES_FIELD_ID_PREFIX}${s.id}`}
          end={`${MAPPINGS_DOCUMENT_ID_PREFIX}${m.id}-${MAPPINGS_FIELD_ID_PREFIX}${s.id}`}
          color={color}
          onClick={handleClick}
          onMouseOver={handleMouseOver}
          onMouseOut={handleMouseOut}
          className={css(styles.arc)}
        />
      )),
      ...m.targetFields.map((s) => (
        <NodesArc
          key={`mapping-arc-to-${m.id}${s.id}`}
          start={`${MAPPINGS_DOCUMENT_ID_PREFIX}${m.id}-${MAPPINGS_FIELD_ID_PREFIX}${s.id}`}
          end={`${TARGETS_FIELD_ID_PREFIX}${s.id}`}
          color={color}
          onClick={handleClick}
          onMouseOver={handleMouseOver}
          onMouseOut={handleMouseOut}
          className={css(styles.arc)}
        />
      )),
    ];
  });
  return (
    <>
      {links}
      <NodesArc
        start={"dnd-start"}
        end={"dnd-target-mapping"}
        color={"var(--pf-global--active-color--400)"}
      />

      <NodesArc
        start={"dnd-start"}
        end={"dnd-new-mapping"}
        color={"var(--pf-global--active-color--400)"}
      />
      <NodesArc
        start={"dnd-new-mapping"}
        end={"dnd-target-field"}
        color={"var(--pf-global--active-color--400)"}
      />
    </>
  );
};
