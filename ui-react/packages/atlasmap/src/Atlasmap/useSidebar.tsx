import { useAtlasmap } from "./AtlasmapProvider";
import React, { useCallback } from "react";
import { IMappingDetailsViewProps, MappingDetailsView } from "../Views";
import { DataMapperUtil } from "@atlasmap/core";

export interface IUseSidebarProps {
  onRemoveMapping: () => void;
}

export function useSidebar({ onRemoveMapping }: IUseSidebarProps) {
  const {
    selectedMapping,
    deselectMapping,
    removeMappedFieldFromCurrentMapping,
    fromMappedFieldToIMappingField,

    //mapping details
    getMappingActions,
    getMultiplicityActions,
    getMultiplicityActionDelimiters,
    handleIndexChange,
    handleNewTransformation,
    handleRemoveTransformation,
    handleTransformationChange,
    handleTransformationArgumentChange,
    handleMultiplicityChange,
    handleMultiplicityArgumentChange,
  } = useAtlasmap();

  return useCallback(() => {
    if (selectedMapping) {
      const m = selectedMapping.mapping;
      const sources = m
        .getMappedFields(true)
        .map(fromMappedFieldToIMappingField);
      const targets = m
        .getMappedFields(false)
        .map(fromMappedFieldToIMappingField);
      const showSourcesIndex =
        sources.length > 1 &&
        m.transition.isManyToOneMode() &&
        !m.transition.enableExpression;
      const showTargetsIndex =
        targets.length > 1 &&
        m.transition.isOneToManyMode() &&
        !m.transition.enableExpression;

      const multiplicityFieldAction = m.transition.transitionFieldAction;

      let multiplicity: IMappingDetailsViewProps["multiplicity"] = undefined;
      if (multiplicityFieldAction) {
        const transformations = getMultiplicityActions(m);
        const transformationsOptions = transformations.map((a) => ({
          label: DataMapperUtil.toDisplayable(a.name),
          name: a.name,
          value: a.name,
        }));
        const delimiters = getMultiplicityActionDelimiters();
        const delimitersOptions = delimiters.map((a) => ({
          name: a.prettyName!,
          value: a.actualDelimiter,
        }));

        multiplicity = {
          name: multiplicityFieldAction.name,
          transformationsOptions,
          transformationsArguments: multiplicityFieldAction.argumentValues.map(
            (a) => ({
              label: DataMapperUtil.toDisplayable(a.name),
              name: a.name,
              value: a.value,
              options: a.name === "delimiter" ? delimitersOptions : undefined,
            }),
          ),
          onChange: (name) =>
            handleMultiplicityChange(multiplicityFieldAction, name),
          onArgumentChange: (argumentName, arguemntValue) =>
            handleMultiplicityArgumentChange(
              multiplicityFieldAction,
              argumentName,
              arguemntValue,
            ),
        };
      }
      const sourceTransformations = getMappingActions(true);
      const sourceTransformationsOptions = sourceTransformations.map((a) => ({
        name: DataMapperUtil.toDisplayable(a.name),
        value: a.name,
      }));
      const targetTransformations = getMappingActions(false);
      const targetTransformationsOptions = targetTransformations.map((a) => ({
        name: DataMapperUtil.toDisplayable(a.name),
        value: a.name,
      }));

      const handleRemoveMappedField = (isSource: boolean, index: number) => {
        const amField = selectedMapping.mapping.getMappedFieldForIndex(
          "" + (index + 1),
          isSource,
        );
        console.log(amField);
        if (amField) {
          removeMappedFieldFromCurrentMapping(amField);
        }
      };

      return (
        <MappingDetailsView
          sources={sources}
          targets={targets}
          onClose={deselectMapping}
          onRemoveMapping={onRemoveMapping}
          onRemoveMappedField={handleRemoveMappedField}
          showSourcesIndex={showSourcesIndex}
          showTargetsIndex={showTargetsIndex}
          multiplicity={multiplicity}
          sourceTransformationsOptions={sourceTransformationsOptions}
          targetTransformationsOptions={targetTransformationsOptions}
          onIndexChange={handleIndexChange}
          onNewTransformation={handleNewTransformation}
          onRemoveTransformation={handleRemoveTransformation}
          onTransformationChange={handleTransformationChange}
          onTransformationArgumentChange={handleTransformationArgumentChange}
        />
      );
    }
    return <>TODO: error</>;
  }, [
    selectedMapping,
    fromMappedFieldToIMappingField,
    getMappingActions,
    deselectMapping,
    onRemoveMapping,
    handleIndexChange,
    handleNewTransformation,
    handleRemoveTransformation,
    handleTransformationChange,
    handleTransformationArgumentChange,
    getMultiplicityActions,
    getMultiplicityActionDelimiters,
    handleMultiplicityChange,
    handleMultiplicityArgumentChange,
    removeMappedFieldFromCurrentMapping,
  ]);
}
