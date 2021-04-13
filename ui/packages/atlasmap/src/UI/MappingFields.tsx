import React, { FunctionComponent } from "react";

import { Button, Tooltip } from "@patternfly/react-core";
import {
  AddCircleOIcon,
  CaretDownIcon,
  CaretRightIcon,
  PlusIcon,
} from "@patternfly/react-icons";
import { css, StyleSheet } from "@patternfly/react-styles";

import { useToggle } from "../Atlasmap/utils";

export interface IMappingFieldsProps {
  isSource: boolean;
  title: string;
  canAddToSelectedMapping: (isSource: boolean) => boolean;
  onCreateConstant: (constants: any | null) => void;
  onCreateProperty: (isSource: boolean, props: any | null) => void;
}

export interface IMappedFieldsProps {}

const styles = StyleSheet.create({
  wrapper: {
    background: "var(--pf-global--BackgroundColor--light-300)",
    padding: "1rem",
    margin: "1rem 0",
    "& .pf-c-expandable__content": {
      margin: "0 !important",
    },
  },
});

export const MappingFields: FunctionComponent<IMappingFieldsProps> = ({
  title,
  isSource,
  onCreateConstant,
  onCreateProperty,
  canAddToSelectedMapping,
  children,
}) => {
  const { state: expanded, toggle: toggleExpanded } = useToggle(true);

  return (
    <div className={css(styles.wrapper)}>
      <Button
        key={"expandable"}
        variant={"plain"}
        aria-label="Ok"
        onClick={toggleExpanded}
        data-testid={`mapping-fields-detail-${title}-toggle`}
        style={{ display: "inline", marginLeft: "auto" }}
      >
        {expanded ? <CaretDownIcon /> : <CaretRightIcon />}
      </Button>
      {title}
      <Tooltip
        position={"top"}
        enableFlip={true}
        content={<div>Create and map a property.</div>}
        key={"create-property"}
        entryDelay={750}
        exitDelay={100}
      >
        <Button
          key={"create-prop"}
          variant={"plain"}
          aria-label="Add Property"
          isDisabled={!canAddToSelectedMapping(isSource)}
          onClick={() => onCreateProperty(isSource, null)}
          data-testid={"mapping-details-add-property-button-test"}
          style={{ display: "inline", marginLeft: "auto", float: "right" }}
        >
          <AddCircleOIcon />
        </Button>
      </Tooltip>
      {isSource && (
        <Tooltip
          position={"top"}
          enableFlip={true}
          content={<div>Create and map a source constant.</div>}
          key={"create-constant"}
          entryDelay={750}
          exitDelay={100}
        >
          <Button
            key={"create-const"}
            variant={"plain"}
            aria-label="Add Constant"
            isDisabled={!canAddToSelectedMapping(isSource)}
            onClick={onCreateConstant}
            data-testid={"mapping-details-add-constant-button-test"}
            style={{ display: "inline", marginLeft: "auto", float: "right" }}
          >
            <PlusIcon />
          </Button>
        </Tooltip>
      )}
      {expanded && children}
    </div>
  );
};
