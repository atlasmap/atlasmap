import { css } from "@patternfly/react-styles";
import React, {
  FunctionComponent,
  MouseEvent,
  ReactElement,
  useMemo,
} from "react";
import { NodesArc, useToggle } from "../../../UI";
import { IAtlasmapMapping } from "../../../Views";
import { SOURCES_FIELD_ID_PREFIX, TARGETS_FIELD_ID_PREFIX } from "../Columns";

import styles from "./SourceTargetLinks.css";

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
  const sortedMappings = useMemo(
    () => mappings.sort((a) => (a.id === selectedMappingId ? 1 : -1)),
    [mappings, selectedMappingId],
  );

  return (
    <svg
      style={{
        position: "absolute",
        top: 0,
        left: 0,
        width: "100%",
        height: "100%",
        pointerEvents: "none",
      }}
    >
      {sortedMappings.map((m) => (
        <MappingLines
          mapping={m}
          onClick={() => onSelectMapping(m)}
          isSelected={m.id === selectedMappingId}
          key={m.id}
        />
      ))}

      <NodesArc
        start={"dnd-start"}
        end={"dnd-target-field"}
        color={"var(--pf-global--active-color--400)"}
      />
    </svg>
  );
};

interface IMappingLinesProps {
  mapping: IAtlasmapMapping;
  isSelected: boolean;
  onClick: () => void;
}

const MappingLines: FunctionComponent<IMappingLinesProps> = ({
  mapping,
  isSelected,
  onClick,
}) => {
  const {
    state: isHovered,
    toggleOn: toggleHoveredOn,
    toggleOff: toggleHoveredOff,
  } = useToggle(false);

  const handleClick = (event: MouseEvent) => {
    onClick();
    event.stopPropagation();
  };

  const color = isSelected ? "var(--pf-global--active-color--100)" : undefined;
  const hoverColor = !isSelected
    ? "var(--pf-global--active-color--400)"
    : undefined;
  const mappingLines = mapping.sourceFields.reduce<ReactElement[]>(
    (lines, start) => {
      const linesFromSource = mapping.targetFields.map((end) => (
        <NodesArc
          key={`${start.id}${end.id}`}
          start={`${SOURCES_FIELD_ID_PREFIX}${start.id}`}
          end={`${TARGETS_FIELD_ID_PREFIX}${end.id}`}
          color={isHovered ? hoverColor : color}
          onClick={handleClick}
          onMouseEnter={toggleHoveredOn}
          onMouseLeave={toggleHoveredOff}
          className={css(styles.arc)}
        />
      ));
      return [...lines, ...linesFromSource];
    },
    [],
  );
  return <g className={css(styles.group)}>{mappingLines}</g>;
};
