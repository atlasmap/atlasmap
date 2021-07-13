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
  AddCircleOIcon,
  CaretDownIcon,
  CaretRightIcon,
  PlusIcon,
} from '@patternfly/react-icons';
import { Button, Tooltip } from '@patternfly/react-core';
import React, { FunctionComponent } from 'react';

import styles from './MappingFields.module.css';
import { useToggle } from '../impl/utils';

export interface IMappingFieldsProps {
  isSource: boolean;
  title: string;
  canAddToSelectedMapping: (isSource: boolean) => boolean;
  onCreateConstant: (constants: any | null) => void;
  onCreateProperty: (isSource: boolean, props: any | null) => void;
}

export interface IMappedFieldsProps {}

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
    <div className={styles.wrapper}>
      <Button
        key={'expandable'}
        variant={'plain'}
        aria-label="Ok"
        onClick={toggleExpanded}
        data-testid={`mapping-fields-detail-${title}-toggle`}
        style={{ display: 'inline', marginLeft: 'auto' }}
      >
        {expanded ? <CaretDownIcon /> : <CaretRightIcon />}
      </Button>
      {title}
      <Tooltip
        position={'top'}
        enableFlip={true}
        content={<div>Create and map a property.</div>}
        key={'create-property'}
        entryDelay={750}
        exitDelay={100}
      >
        <Button
          key={'create-prop'}
          variant={'plain'}
          aria-label="Add Property"
          isDisabled={!canAddToSelectedMapping(isSource)}
          onClick={() => onCreateProperty(isSource, null)}
          data-testid={'mapping-details-add-property-button-test'}
          style={{ display: 'inline', marginLeft: 'auto', float: 'right' }}
        >
          <AddCircleOIcon />
        </Button>
      </Tooltip>
      {isSource && (
        <Tooltip
          position={'top'}
          enableFlip={true}
          content={<div>Create and map a source constant.</div>}
          key={'create-constant'}
          entryDelay={750}
          exitDelay={100}
        >
          <Button
            key={'create-const'}
            variant={'plain'}
            aria-label="Add Constant"
            isDisabled={!canAddToSelectedMapping(isSource)}
            onClick={onCreateConstant}
            data-testid={'mapping-details-add-constant-button-test'}
            style={{ display: 'inline', marginLeft: 'auto', float: 'right' }}
          >
            <PlusIcon />
          </Button>
        </Tooltip>
      )}
      {expanded && children}
    </div>
  );
};
