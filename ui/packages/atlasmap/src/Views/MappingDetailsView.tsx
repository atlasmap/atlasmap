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
  AddFieldTypeahead,
  Document,
  ITransformationArgument,
  MappingField,
  MappingFields,
  MappingTransformation,
} from '../UI';
import { Alert, AlertActionCloseButton, Badge } from '@patternfly/react-core';
import { IAtlasmapField, IAtlasmapMappedField, INotification } from './models';
import React, { FunctionComponent } from 'react';

import { Field } from '@atlasmap/core/dist/models/field.model';
import { MappingDetailsSidebar } from '../Layout';

export interface IMappingDetailsViewProps {
  notifications: INotification[];
  sources: Array<IAtlasmapMappedField | null>;
  targets: Array<IAtlasmapMappedField | null>;
  addableSources: IAtlasmapField[];
  addableTargets: IAtlasmapField[];
  showSourcesIndex: boolean;
  showTargetsIndex: boolean;
  multiplicity?: {
    name: string;
    transformationsOptions: { name: string; value: string }[];
    transformationsArguments?: ITransformationArgument[];
    onChange: (newName: string) => void;
    onArgumentChange: (argumentName: string, argumentValue: string) => void;
  };
  onClose: () => void;
  mappingExpressionEnabled: boolean;
  onRemoveMapping: () => void;
  onRemoveMappedField: (isSource: boolean, index: number) => void;
  onIndexChange: (
    isSource: boolean,
    currentIndex: number,
    newIndex: number | Field,
  ) => void;
  onNewTransformation: (isSource: boolean, index: number) => void;
  onTransformationChange: (
    isSource: boolean,
    index: number,
    currentTransformationIndex: number,
    newTransformationName: string,
  ) => void;
  onTransformationArgumentChange: (
    isSource: boolean,
    index: number,
    transformationIndex: number,
    argumentName: string,
    argumentValue: string,
  ) => void;
  onRemoveTransformation: (
    isSource: boolean,
    index: number,
    currentTransformationIndex: number,
  ) => void;
  onAddFieldToMapping: (isSource: boolean, field: IAtlasmapField) => void;
  onNotificationRead: (id: string) => void;
  onEditEnum: (cb: any) => void;
  isEnumMapping: () => boolean;
  onCreateConstant: (
    constants: any | null,
    addToActiveMapping?: boolean,
  ) => void;
  onCreateProperty: (
    isSource: boolean,
    props: any | null,
    addToActiveMapping?: boolean,
  ) => void;
  canAddToSelectedMapping: (isSource: boolean) => boolean;
}

export const MappingDetailsView: FunctionComponent<
  IMappingDetailsViewProps
> = ({
  notifications,
  sources,
  targets,
  addableSources,
  addableTargets,
  showSourcesIndex,
  showTargetsIndex,
  onClose,
  mappingExpressionEnabled,
  onRemoveMapping,
  onRemoveMappedField,
  onIndexChange,
  onNewTransformation,
  onTransformationChange,
  onTransformationArgumentChange,
  onRemoveTransformation,
  onAddFieldToMapping,
  onNotificationRead,
  onEditEnum,
  isEnumMapping,
  onCreateConstant,
  onCreateProperty,
  canAddToSelectedMapping,
  multiplicity,
}) => {
  const formInputGroupStyle = {
    '--pf-c-form--GridGap': '0',
    marginTop: '1.0rem',
  } as React.CSSProperties;

  const mappingAction = multiplicity && (
    <MappingTransformation
      name={multiplicity.name}
      disableTransformation={mappingExpressionEnabled}
      transformationsOptions={multiplicity.transformationsOptions}
      transformationsArguments={multiplicity.transformationsArguments}
      onTransformationChange={multiplicity.onChange}
      onTransformationArgumentChange={multiplicity.onArgumentChange}
    />
  );

  const genericPaddingField: IAtlasmapField = {
    id: '',
    name: 'Padding field',
    value: '',
    type: '',
    scope: 'current',
    path: '',
    mappings: [],
    hasTransformations: false,
    isAttribute: false,
    isCollection: false,
    isConnected: false,
    isInCollection: false,
    isDisabled: false,
    amField: {} as IAtlasmapField['amField'],
    enumeration: false,
  };

  const renderMappingField = (
    isSource: boolean,
    canShowIndex: boolean,
    f: IAtlasmapMappedField | null,
    index: number,
  ) => {
    return f ? (
      <MappingField
        key={f.id}
        field={f}
        name={f.name}
        info={
          f.scope
            ? `${f.path} <${f.scope}> (${f.type})`
            : `${f.path} (${f.type})`
        }
        mappingExpressionEnabled={mappingExpressionEnabled}
        hasTransformations={f.transformations.length > 0}
        onDelete={() => onRemoveMappedField(isSource, index)}
        onIndexChange={(value: string | IAtlasmapField) =>
          typeof value === 'string'
            ? onIndexChange(isSource, index, parseInt(value, 10))
            : onIndexChange(isSource, index, value.amField)
        }
        onNewTransformation={() => onNewTransformation(isSource, index)}
        index={index + 1}
        canShowIndex={canShowIndex}
      >
        <div className={'pf-c-form'} style={formInputGroupStyle}>
          {f.transformations.map((t, transformationIndex) => (
            <MappingTransformation
              key={transformationIndex}
              name={t.name}
              transformationsOptions={t.options}
              transformationsArguments={t.arguments}
              disableTransformation={mappingExpressionEnabled}
              onTransformationChange={(value) =>
                onTransformationChange(
                  isSource,
                  index,
                  transformationIndex,
                  value,
                )
              }
              onTransformationArgumentChange={(name, value) =>
                onTransformationArgumentChange(
                  isSource,
                  index,
                  transformationIndex,
                  name,
                  value,
                )
              }
              onRemoveTransformation={() => {
                onRemoveTransformation(isSource, index, transformationIndex);
              }}
            />
          ))}
        </div>
      </MappingField>
    ) : (
      <MappingField
        field={genericPaddingField}
        key={index}
        name={'Padding field'}
        info={'This padding field has been automatically added'}
        mappingExpressionEnabled={mappingExpressionEnabled}
        hasTransformations={false}
        onDelete={() => onRemoveMappedField(isSource, index)}
        index={index + 1}
        canShowIndex={showSourcesIndex}
      />
    );
  };

  const renderSourceMappingField = (
    f: IAtlasmapMappedField | null,
    index: number,
  ) => renderMappingField(true, showSourcesIndex, f, index);
  const renderTargetMappingField = (
    f: IAtlasmapMappedField | null,
    index: number,
  ) => renderMappingField(false, showTargetsIndex, f, index);

  const errors = notifications.filter((n) => n.variant === 'danger');
  const warnings = notifications.filter((n) => n.variant === 'warning');
  const messages = notifications.filter(
    (n) => n.variant !== 'warning' && n.variant !== 'danger',
  );

  return (
    <MappingDetailsSidebar
      onClose={onClose}
      onDelete={onRemoveMapping}
      onEditEnum={onEditEnum}
      isEnumMapping={isEnumMapping}
    >
      {errors.length > 0 && (
        <NotificationsGroup
          notifications={errors}
          title={'Errors'}
          onNotificationRead={onNotificationRead}
        />
      )}
      {warnings.length > 0 && (
        <NotificationsGroup
          notifications={warnings}
          title={'Warnings'}
          onNotificationRead={onNotificationRead}
        />
      )}
      {messages.length > 0 && (
        <NotificationsGroup
          notifications={messages}
          title={'Messages'}
          onNotificationRead={onNotificationRead}
        />
      )}
      <div className={'pf-c-form'} style={formInputGroupStyle}>
        {mappingAction}
        <MappingFields
          isSource={true}
          title={'Sources'}
          onCreateConstant={onCreateConstant}
          onCreateProperty={onCreateProperty}
          canAddToSelectedMapping={canAddToSelectedMapping}
        >
          {sources.map(renderSourceMappingField)}
          {targets.length <= 1 && (
            <AddFieldTypeahead
              ariaLabelTypeAhead={'Select source to add to the mapping'}
              placeholderText={'Select source to add to the mapping'}
              isSource={true}
              fields={addableSources.map((s) => ({
                label: s.amField.isPropertyOrConstant() ? s.name : s.path,
                group: s.amField!.docDef.name,
                onAdd: () => onAddFieldToMapping(true, s),
              }))}
              data-testid={'add-source-to-mapping'}
            />
          )}
        </MappingFields>
        <MappingFields
          isSource={false}
          title={'Targets'}
          onCreateConstant={onCreateConstant}
          onCreateProperty={onCreateProperty}
          canAddToSelectedMapping={canAddToSelectedMapping}
        >
          {targets.map(renderTargetMappingField)}
          {sources.length <= 1 && (
            <AddFieldTypeahead
              ariaLabelTypeAhead={'Select target to add to the mapping'}
              placeholderText={'Select target to add to the mapping'}
              isSource={false}
              fields={addableTargets.map((t) => ({
                label: t.amField.isProperty() ? t.name : t.path,
                group: t.amField!.docDef.name,
                onAdd: () => onAddFieldToMapping(false, t),
              }))}
              data-testid={'add-target-to-mapping'}
            />
          )}
        </MappingFields>
      </div>
    </MappingDetailsSidebar>
  );
};

interface INotificationsGroupProps {
  title: string;
  notifications: INotification[];
  onNotificationRead: (id: string) => void;
}

const NotificationsGroup: FunctionComponent<INotificationsGroupProps> = ({
  title,
  notifications,
  onNotificationRead,
}) => {
  return (
    <Document
      title={title}
      noShadows={true}
      noPadding={true}
      actions={[<Badge key={1}>{notifications.length}</Badge>]}
    >
      <div style={{ maxHeight: 200, overflow: 'auto' }}>
        {notifications.map((n, idx) => (
          <Alert
            key={idx}
            variant={n.variant}
            title={n.description}
            isInline={true}
            actionClose={
              <AlertActionCloseButton
                title={n.title}
                variantLabel={`${n.variant} alert`}
                onClose={() => onNotificationRead(n.id)}
                data-testid={`dismiss-mapping-notification-${n.id}`}
              />
            }
          />
        ))}
      </div>
    </Document>
  );
};
