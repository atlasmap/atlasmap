import React, { FunctionComponent } from "react";

import { Alert, AlertActionCloseButton } from "@patternfly/react-core";

import { MappingDetailsSidebar } from "../Layout";
import {
  AddFieldTypeahead,
  ITransformationArgument,
  MappingField,
  MappingFields,
  MappingTransformation,
} from "../UI";
import { IAtlasmapField, IAtlasmapMappedField, INotification } from "./models";

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
    newIndex: number,
  ) => void;
  onNewTransformation: (isSource: boolean, index: number) => void;
  onTransformationChange: (
    isSource: boolean,
    index: number,
    currentTransformationName: string,
    newTransformationName: string,
  ) => void;
  onTransformationArgumentChange: (
    isSource: boolean,
    index: number,
    transformationName: string,
    argumentName: string,
    argumentValue: string,
  ) => void;
  onRemoveTransformation: (
    isSource: boolean,
    index: number,
    currentTransformationName: string,
  ) => void;
  onAddFieldToMapping: (isSource: boolean, field: IAtlasmapField) => void;
  onNotificationRead: (id: string) => void;
}

export const MappingDetailsView: FunctionComponent<IMappingDetailsViewProps> = ({
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
  multiplicity,
}) => {
  const mappingAction = multiplicity && (
    <MappingTransformation
      name={multiplicity.name}
      disableTransformation={mappingExpressionEnabled}
      transformationsOptions={multiplicity.transformationsOptions}
      transformationsArguments={multiplicity.transformationsArguments}
      onTransformationChange={multiplicity.onChange}
      onTransformationArgumentChange={multiplicity.onArgumentChange}
      noPaddings={true}
    />
  );

  const renderMappingField = (
    isSource: boolean,
    canShowIndex: boolean,
    f: IAtlasmapMappedField | null,
    index: number,
  ) => {
    return f ? (
      <MappingField
        key={f.id}
        name={f.name}
        info={`${f.path} (${f.type})`}
        onDelete={() => onRemoveMappedField(isSource, index)}
        onIndexChange={(value: string) =>
          onIndexChange(isSource, index, parseInt(value, 10))
        }
        onNewTransformation={() => onNewTransformation(isSource, index)}
        index={index + 1}
        canShowIndex={canShowIndex}
      >
        {f.transformations.map((t, transformationIndex) => (
          <MappingTransformation
            key={transformationIndex}
            name={t.name}
            transformationsOptions={t.options}
            transformationsArguments={t.arguments}
            disableTransformation={mappingExpressionEnabled}
            onTransformationChange={(value) =>
              onTransformationChange(isSource, index, t.name, value)
            }
            onTransformationArgumentChange={(name, value) =>
              onTransformationArgumentChange(
                isSource,
                index,
                t.name,
                name,
                value,
              )
            }
            onRemoveTransformation={() =>
              onRemoveTransformation(isSource, index, t.name)
            }
          />
        ))}
      </MappingField>
    ) : (
      <MappingField
        key={index}
        name={"Padding field"}
        info={"This padding field has been automatically added"}
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

  return (
    <MappingDetailsSidebar onDelete={onRemoveMapping} onClose={onClose}>
      {notifications.map((n, idx) => (
        <Alert
          key={idx}
          variant={n.variant}
          title={n.description}
          isInline={true}
          action={
            <AlertActionCloseButton
              title={n.title}
              variantLabel={`${n.variant} alert`}
              onClose={() => onNotificationRead(n.id)}
              data-testid={`dismiss-mapping-notification-${n.id}`}
            />
          }
        />
      ))}
      {mappingAction}
      <MappingFields title={"Sources"}>
        {sources.map(renderSourceMappingField)}
        {addableSources.length > 0 && (
          <AddFieldTypeahead
            ariaLabelTypeAhead={"Select source to add to the mapping"}
            placeholderText={"Select source to add to the mapping"}
            fields={addableSources.map((s) => ({
              label: s.path,
              group: s.amField.docDef.name,
              onAdd: () => onAddFieldToMapping(true, s),
            }))}
            data-testid={"add-source-to-mapping"}
          />
        )}
      </MappingFields>
      <MappingFields title={"Targets"}>
        {targets.map(renderTargetMappingField)}
        {addableTargets.length > 0 && (
          <AddFieldTypeahead
            ariaLabelTypeAhead={"Select target to add to the mapping"}
            placeholderText={"Select target to add to the mapping"}
            fields={addableTargets.map((s) => ({
              label: s.path,
              group: s.amField.docDef.name,
              onAdd: () => onAddFieldToMapping(false, s),
            }))}
            data-testid={"add-target-to-mapping"}
          />
        )}
      </MappingFields>
    </MappingDetailsSidebar>
  );
};
