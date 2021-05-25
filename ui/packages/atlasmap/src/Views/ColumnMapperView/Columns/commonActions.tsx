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
  Button,
  DropdownItem,
  DropdownToggle,
  Split,
  SplitItem,
  Title,
  Tooltip,
} from '@patternfly/react-core';
import {
  ExchangeAltIcon,
  LinkIcon,
  ProjectDiagramIcon,
  UnlinkIcon,
} from '@patternfly/react-icons';

import { AutoDropdown } from '../../../UI';
import { IAtlasmapMapping } from '../../../Views';
import React from 'react';

export interface ICommonActionsProps {
  connectedMappings: IAtlasmapMapping[];
  onShowMappingDetails: (mapping: IAtlasmapMapping) => void;
  canAddFieldToSelectedMapping: boolean;
  onAddToSelectedMapping: () => void;
  canRemoveFromSelectedMapping: boolean;
  onRemoveFromSelectedMapping: () => void;
  canStartMapping: boolean;
  onStartMapping: () => void;
}

export function commonActions({
  connectedMappings,
  onShowMappingDetails,
  canAddFieldToSelectedMapping,
  onAddToSelectedMapping,
  canRemoveFromSelectedMapping,
  onRemoveFromSelectedMapping,
  canStartMapping,
  onStartMapping,
}: ICommonActionsProps) {
  return [
    <Tooltip
      key={'select-mapping'}
      position={'top'}
      enableFlip={true}
      content={<div>Show mapping details</div>}
    >
      {connectedMappings.length > 1 ? (
        <AutoDropdown
          toggle={({ toggleOpen }) => (
            <DropdownToggle
              toggleIndicator={null}
              aria-label="Show mapping details"
              onToggle={toggleOpen}
            >
              <ExchangeAltIcon />
            </DropdownToggle>
          )}
          isPlain={true}
          position={'right'}
          dropdownItems={connectedMappings.map((m) => (
            <DropdownItem key={m.id} onClick={() => onShowMappingDetails(m)}>
              <Title headingLevel="h2" size="lg">
                {m.name}
              </Title>
              <Split hasGutter>
                <SplitItem>
                  <Title headingLevel="h2" size="md">
                    Sources
                  </Title>
                  {m.sourceFields.map((s) => (
                    <div key={s.id}>{s.name}</div>
                  ))}
                </SplitItem>
                <SplitItem>
                  <Title headingLevel="h2" size="md">
                    Targets
                  </Title>
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
          data-testid={'show-mapping-details-button'}
        >
          <ExchangeAltIcon />
        </Button>
      )}
    </Tooltip>,
    canRemoveFromSelectedMapping ? (
      <Tooltip
        key={'quick-remove'}
        position={'top'}
        enableFlip={true}
        content={<div>Disconnect from the selected mapping</div>}
      >
        <Button
          variant="plain"
          onClick={onRemoveFromSelectedMapping}
          aria-label={'Disconnect from the selected mapping'}
          data-testid={'disconnect-from-the-selected-mapping-button'}
          tabIndex={0}
        >
          <UnlinkIcon />
        </Button>
      </Tooltip>
    ) : (
      <Tooltip
        key={'quick-add'}
        position={'top'}
        enableFlip={true}
        content={<div>Connect to the selected mapping</div>}
      >
        <Button
          variant="plain"
          onClick={onAddToSelectedMapping}
          aria-label={'Connect to the selected mapping'}
          tabIndex={0}
          isDisabled={!canAddFieldToSelectedMapping}
          data-testid={'connect-to-the-selected-mapping-button'}
        >
          <LinkIcon />
        </Button>
      </Tooltip>
    ),
    <Tooltip
      key={'add'}
      position={'top'}
      enableFlip={true}
      content={<div>Create new mapping</div>}
    >
      <Button
        variant="plain"
        onClick={onStartMapping}
        aria-label={'Create new mapping'}
        tabIndex={0}
        isDisabled={!canStartMapping}
        data-testid={'create-new-mapping-button'}
      >
        <ProjectDiagramIcon />
      </Button>
    </Tooltip>,
  ];
}
