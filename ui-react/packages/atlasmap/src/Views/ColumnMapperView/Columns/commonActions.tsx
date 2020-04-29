import React from "react";

import {
  Button,
  DropdownItem,
  DropdownToggle,
  Split,
  SplitItem,
  Title,
  Tooltip,
} from "@patternfly/react-core";
import {
  ExchangeAltIcon,
  LinkIcon,
  ProjectDiagramIcon,
  UnlinkIcon,
} from "@patternfly/react-icons";

import { AutoDropdown } from "../../../UI";
import { IAtlasmapMapping } from "../../../Views";

export interface ICommonActionsProps {
  connectedMappings: IAtlasmapMapping[];
  onShowMappingDetails: (mapping: IAtlasmapMapping) => void;
  canAddToSelectedMapping: boolean;
  onAddToSelectedMapping: () => void;
  canRemoveFromSelectedMapping: boolean;
  onRemoveFromSelectedMapping: () => void;
  canStartMapping: boolean;
  onStartMapping: () => void;
}

export function commonActions({
  connectedMappings,
  onShowMappingDetails,
  canAddToSelectedMapping,
  onAddToSelectedMapping,
  canRemoveFromSelectedMapping,
  onRemoveFromSelectedMapping,
  canStartMapping,
  onStartMapping,
}: ICommonActionsProps) {
  return [
    <Tooltip
      key={"select-mapping"}
      position={"top"}
      enableFlip={true}
      content={<div>Show mapping details</div>}
    >
      {connectedMappings.length > 1 ? (
        <AutoDropdown
          toggle={({ toggleOpen }) => (
            <DropdownToggle
              iconComponent={null}
              aria-label="Show mapping details"
              onToggle={toggleOpen}
            >
              <ExchangeAltIcon />
            </DropdownToggle>
          )}
          isPlain={true}
          position={"right"}
          dropdownItems={connectedMappings.map((m) => (
            <DropdownItem key={m.id} onClick={() => onShowMappingDetails(m)}>
              <Title size={"lg"}>{m.name}</Title>
              <Split gutter="sm">
                <SplitItem>
                  <Title size={"md"}>Sources</Title>
                  {m.sourceFields.map((s) => (
                    <div key={s.id}>{s.name}</div>
                  ))}
                </SplitItem>
                <SplitItem>
                  <Title size={"md"}>Targets</Title>
                  {m.targetFields.map((t) => (
                    <div key={t.id}>{t.name}</div>
                  ))}
                </SplitItem>
              </Split>
            </DropdownItem>
          ))}
          disabled={connectedMappings.length === 0}
        />
      ) : (
        <Button
          variant="plain"
          onClick={() => onShowMappingDetails(connectedMappings[0])}
          aria-label="Show mapping details"
          tabIndex={0}
          isDisabled={connectedMappings.length === 0}
        >
          <ExchangeAltIcon />
        </Button>
      )}
    </Tooltip>,
    canRemoveFromSelectedMapping ? (
      <Tooltip
        key={"quick-remove"}
        position={"top"}
        enableFlip={true}
        content={<div>Disconnect from the selected mapping</div>}
      >
        <Button
          variant="plain"
          onClick={onRemoveFromSelectedMapping}
          aria-label={"Disconnect from the selected mapping"}
          tabIndex={0}
        >
          <UnlinkIcon />
        </Button>
      </Tooltip>
    ) : (
      <Tooltip
        key={"quick-add"}
        position={"top"}
        enableFlip={true}
        content={<div>Connect to the selected mapping</div>}
      >
        <Button
          variant="plain"
          onClick={onAddToSelectedMapping}
          aria-label={"Connect to the selected mapping"}
          tabIndex={0}
          isDisabled={!canAddToSelectedMapping}
        >
          <LinkIcon />
        </Button>
      </Tooltip>
    ),
    <Tooltip
      key={"add"}
      position={"top"}
      enableFlip={true}
      content={<div>Create new mapping</div>}
    >
      <Button
        variant="plain"
        onClick={onStartMapping}
        aria-label={"Create new mapping"}
        tabIndex={0}
        isDisabled={!canStartMapping}
      >
        <ProjectDiagramIcon />
      </Button>
    </Tooltip>,
  ];
}
