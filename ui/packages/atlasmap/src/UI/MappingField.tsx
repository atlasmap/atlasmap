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
import { BoltIcon, InfoAltIcon, TrashIcon } from '@patternfly/react-icons';
import {
  Button,
  InputGroup,
  InputGroupText,
  Split,
  SplitItem,
  TextInput,
  Title,
  Tooltip,
} from '@patternfly/react-core';
import { DraggableField, FieldDropTarget } from './dnd';
import React, { Children, FunctionComponent } from 'react';

import { IAtlasmapField } from '../../src/Views/models';
import { NodeRef } from './Canvas/NodeRef';
import { css } from '@patternfly/react-styles';
import styles from './MappingField.module.css';

export interface IMappingFieldProps {
  field: IAtlasmapField;
  name: string;
  info: string;
  index: number;
  canShowIndex: boolean;
  mappingExpressionEnabled: boolean;
  hasTransformations: boolean;
  onDelete: () => void;
  onIndexChange?: (value: string | IAtlasmapField) => void;
  onNewTransformation?: () => void;
}

const formTransInputGroup = {
  '--pf-c-form-control--FontSize': 'small',
} as React.CSSProperties;

export const MappingField: FunctionComponent<IMappingFieldProps> = ({
  field,
  name,
  info,
  index,
  canShowIndex,
  mappingExpressionEnabled,
  hasTransformations,
  onDelete,
  onIndexChange,
  onNewTransformation,
  children,
}) => {
  const id = `mapping-field-${name}`;
  return (
    <FieldDropTarget
      key={field ? field.id : name}
      target={{
        id: field.id,
        name: field.name,
        type: 'mapping',
        payload: field,
      }}
      canDrop={() => {
        return true;
      }}
      accept={['mapping']}
    >
      {({ isTarget }) => (
        <DraggableField
          field={{
            type: 'mapping',
            id: field.id,
            name: field.name,
            payload: field,
          }}
          onDrop={(_, target) => {
            if (!onIndexChange) {
              return;
            }
            onIndexChange(target!.payload as IAtlasmapField);
          }}
        >
          {({ isDragging }) => (
            <NodeRef
              id={[
                field.id,
                isDragging ? 'dnd-start' : undefined,
                isTarget ? 'dnd-target-field' : undefined,
              ]}
            >
              <div
                className={styles.field}
                aria-labelledby={id}
                data-testid={id}
              >
                <Split>
                  <SplitItem isFilled>
                    <Title
                      headingLevel="h6"
                      size="md"
                      id={id}
                      className={styles.title}
                    >
                      {canShowIndex && (
                        <Tooltip
                          position={'auto'}
                          enableFlip={true}
                          entryDelay={750}
                          exitDelay={100}
                          content={
                            <div>
                              Edit the index for this element by selecting the
                              arrows or by dragging and dropping the element to
                              the desired position. Placeholders may be
                              automatically inserted to account for any gaps in
                              the indexing.
                            </div>
                          }
                        >
                          <InputGroup className={styles.fieldIndex}>
                            <InputGroupText>#</InputGroupText>
                            <TextInput
                              type={'number'}
                              value={index}
                              id={'index'}
                              onChange={onIndexChange}
                              data-testid={`change-${name}-input-index`}
                              isDisabled={!onIndexChange}
                              style={formTransInputGroup}
                            />
                          </InputGroup>
                        </Tooltip>
                      )}
                      <Tooltip
                        position={'auto'}
                        enableFlip={true}
                        entryDelay={750}
                        exitDelay={100}
                        content={<div>{info}</div>}
                      >
                        <div className={styles.fieldName}>
                          {name} <InfoAltIcon />
                        </div>
                      </Tooltip>
                    </Title>
                  </SplitItem>
                  {!mappingExpressionEnabled && onNewTransformation && (
                    <SplitItem>
                      <Tooltip
                        position={'auto'}
                        enableFlip={true}
                        entryDelay={750}
                        exitDelay={100}
                        content={'Add a new transformation.'}
                      >
                        <Button
                          variant={'plain'}
                          onClick={onNewTransformation}
                          className={styles.link}
                          data-testid={`add-transformation-to-${name}-field-button`}
                        >
                          <BoltIcon />
                        </Button>
                      </Tooltip>
                    </SplitItem>
                  )}
                  <SplitItem>
                    <Tooltip
                      position={'auto'}
                      enableFlip={true}
                      entryDelay={750}
                      exitDelay={100}
                      content={'Delete this field from the mapping.'}
                    >
                      <Button
                        variant={'plain'}
                        onClick={onDelete}
                        className={styles.link}
                        data-testid={`remove-${name}-from-mapping-button`}
                      >
                        <TrashIcon />
                      </Button>
                    </Tooltip>
                  </SplitItem>
                </Split>
                {/*
                  Show established field action transformations associated with this
                  field.
                  */}
                {!mappingExpressionEnabled &&
                  hasTransformations &&
                  children &&
                  Children.count(children) > 0 && (
                    <div className={css(styles.transformationsWrapper)}>
                      <Title headingLevel="h6" size="md">
                        Transformations
                      </Title>
                      <div className={styles.transformations}>{children}</div>
                    </div>
                  )}
              </div>
            </NodeRef>
          )}
        </DraggableField>
      )}
    </FieldDropTarget>
  );
};
