import { useAtlasmap } from "./AtlasmapProvider";
import React, { useCallback } from "react";
import {
  IMappingDetailsViewProps,
  MappingDetailsView,
  IAtlasmapField,
} from "../Views";
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
    flatSources,
    flatTargets,
    constants,
    properties,
    isFieldAddableToSelection,
    addToCurrentMapping,
    notifications,
    markNotificationRead,

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
        if (amField) {
          removeMappedFieldFromCurrentMapping(amField);
        }
      };

      const addableSources = [
        constants?.fields,
        properties?.fields,
        flatSources,
      ]
        .flatMap((fields) => (fields ? (fields as IAtlasmapField[]) : []))
        .filter((f) => isFieldAddableToSelection("source", f));
      const addableTargets = flatTargets.filter((f) =>
        isFieldAddableToSelection("target", f),
      );

      return (
        <MappingDetailsView
          notifications={notifications.filter(
            (n) => n.mappingId === selectedMapping.id && !n.isRead,
          )}
          sources={sources}
          targets={targets}
          addableSources={addableSources}
          addableTargets={addableTargets}
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
          onAddFieldToMapping={(_isSource, f) => addToCurrentMapping(f.amField)}
          onNotificationRead={markNotificationRead}
        />
      );
    }
    return <>TODO: error</>;
  }, [
    selectedMapping,
    fromMappedFieldToIMappingField,
    getMappingActions,
    constants,
    properties,
    flatSources,
    flatTargets,
    notifications,
    deselectMapping,
    onRemoveMapping,
    handleIndexChange,
    handleNewTransformation,
    handleRemoveTransformation,
    handleTransformationChange,
    handleTransformationArgumentChange,
    markNotificationRead,
    getMultiplicityActions,
    getMultiplicityActionDelimiters,
    handleMultiplicityChange,
    handleMultiplicityArgumentChange,
    removeMappedFieldFromCurrentMapping,
    isFieldAddableToSelection,
    addToCurrentMapping,
  ]);
}
