import React, { FunctionComponent } from "react";

import { MappingDetailsSidebar } from "../Layout";
import {
  MappingField,
  MappingFields,
  MappingTransformation,
  ITransformationArgument,
} from "../UI";
import { IAtlasmapMappedField, INotification } from "./models";
import { Alert } from "@patternfly/react-core";

export interface IMappingDetailsViewProps {
  notifications: INotification[];
  sources: Array<IAtlasmapMappedField | null>;
  targets: Array<IAtlasmapMappedField | null>;
  showSourcesIndex: boolean;
  showTargetsIndex: boolean;
  multiplicity?: {
    name: string;
    transformationsOptions: { name: string; value: string }[];
    transformationsArguments?: ITransformationArgument[];
    onChange: (newName: string) => void;
    onArgumentChange: (argumentName: string, argumentValue: string) => void;
  };
  sourceTransformationsOptions: { name: string; value: string }[];
  targetTransformationsOptions: { name: string; value: string }[];
  onClose: () => void;
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
}

export const MappingDetailsView: FunctionComponent<IMappingDetailsViewProps> = ({
  notifications,
  sources,
  targets,
  showSourcesIndex,
  showTargetsIndex,
  sourceTransformationsOptions,
  onClose,
  onRemoveMapping,
  onRemoveMappedField,
  onIndexChange,
  onNewTransformation,
  onTransformationChange,
  onTransformationArgumentChange,
  onRemoveTransformation,
  multiplicity,
}) => {
  const mappingAction = multiplicity && (
    <MappingTransformation
      name={multiplicity.name}
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
            transformationsOptions={sourceTransformationsOptions}
            transformationsArguments={t.arguments}
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
          title={n.message}
          isInline={true}
        />
      ))}
      {mappingAction}
      <MappingFields title={"Sources"}>
        {sources.map(renderSourceMappingField)}
      </MappingFields>
      <MappingFields title={"Targets"}>
        {targets.map(renderTargetMappingField)}
      </MappingFields>
    </MappingDetailsSidebar>
  );
};
