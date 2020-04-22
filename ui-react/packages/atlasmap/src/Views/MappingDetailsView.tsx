import React, { FunctionComponent } from "react";

import {
  ConfigModel,
  DataMapperUtil,
  FieldAction,
  FieldActionDefinition,
  MappedField,
  MappingModel,
} from "@atlasmap/core";

import { MappingDetailsSidebar } from "../Layout";
import { MappingField, MappingFields, MappingTransformation } from "../UI";
import { IAtlasmapField, IAtlasmapMapping } from "./models";

export function handleIndexChange(
  mapping: MappingModel,
  mField: MappedField,
  event: any,
) {
  const cfg = ConfigModel.getConfig();
  const insertionIndex = Number(event.target.value) || 0;
  if (insertionIndex === 0) {
    return;
  }
  const mappedFields = mapping.getMappedFields(mField.isSource());
  const targetIndex = mappedFields.length;
  if (insertionIndex > targetIndex) {
    // Add place-holders for each index value between the previous max index
    // and the insertion index.
    cfg.mappingService.addPlaceholders(
      insertionIndex - mappedFields.length,
      mapping,
      targetIndex,
      mField.field!.isSource(),
    );
  }
  cfg.mappingService.moveMappedFieldTo(mapping, mField, insertionIndex);
}

export interface IMappingDetailsViewProps {
  mapping: IAtlasmapMapping;
  sources: MappedField[];
  showSourcesIndex: boolean;
  targets: MappedField[];
  showTargetsIndex: boolean;
  availableActions: FieldActionDefinition[];
  actionsOptions: { name: string; value: string }[];
  actionDelimiters: { displayName: string; delimiterValue: string }[];
  multiplicityFieldAction: FieldAction | undefined;
  getMappingActions: (isSource: boolean) => FieldActionDefinition[];
  onClose: () => void;
  onRemoveMapping: () => void;
  onRemoveMappedField: (field: IAtlasmapField) => void;
  onActionChange: (
    action: FieldAction,
    definition: FieldActionDefinition,
  ) => void;
  // onNewTransformation: (newTransformation: () => void) => void;
  // onRemoveTransformation: (removeTransformation: () => void) => void;
}

export const MappingDetailsView: FunctionComponent<IMappingDetailsViewProps> = ({
  mapping,
  sources,
  showSourcesIndex,
  targets,
  showTargetsIndex,
  availableActions,
  actionsOptions,
  actionDelimiters,
  multiplicityFieldAction,
  getMappingActions,
  onClose,
  onRemoveMapping,
  // onRemoveMappedField,
  onActionChange,
  // onNewTransformation,
  // onRemoveTransformation,
}) => {
  const mappingAction = multiplicityFieldAction && (
    <MappingTransformation
      associatedFieldActionName={multiplicityFieldAction.name}
      actionsOptions={actionsOptions}
      actionDelimiters={actionDelimiters}
      currentActionDelimiter={
        actionDelimiters[mapping.mapping.transition.delimiter].delimiterValue
      }
      isMultiplicityAction={true}
      args={multiplicityFieldAction.definition.arguments
        .map((arg) => multiplicityFieldAction.getArgumentValue(arg.name))
        .map((opts) => ({
          ...opts,
          label: DataMapperUtil.toDisplayable(opts.name),
          name: opts.name,
          value: opts.value,
        }))}
      onArgValueChange={(value: string, event: any) => {
        multiplicityFieldAction.setArgumentValue(event.target.name, value);
        // notifyMappingUpdated();
      }}
      onActionChange={(name: string) =>
        onActionChange(
          mapping.mapping.transition.transitionFieldAction,
          availableActions.find((a) => a.name === name)!,
        )
      }
      onActionDelimiterChange={(delimiterValue: string) => {
        multiplicityFieldAction.setArgumentValue("delimiter", delimiterValue);
        // notifyMappingUpdated();
      }}
      onRemoveTransformation={() => {
        void 0;
      }}
      noPaddings={true}
    />
  );

  let nextEmptyKey = 0;

  return (
    <MappingDetailsSidebar onDelete={onRemoveMapping} onClose={onClose}>
      <MappingFields title={"Sources"}>
        {mappingAction}
        {sources.map((s) => {
          const availableActions = getMappingActions(true);
          const actionsOptions = availableActions.map((a) => ({
            name: DataMapperUtil.toDisplayable(a.name),
            value: a.name,
          }));
          return (
            <MappingField
              key={s.field?.uuid ?? ++nextEmptyKey}
              name={s.field?.name ?? ""}
              info={s.field?.getFieldLabel(true, true) ?? ""}
              onDelete={() =>
                // onRemoveMappedField(() => mapping.removeMappedField(s))
                void 0
              }
              onIndexChange={(event: any) =>
                handleIndexChange(mapping.mapping, s, event)
              }
              onNewTransformation={() => {
                const action: FieldAction = new FieldAction();
                availableActions[0].populateFieldAction(action);
                s.actions.push(action);
                // notifyMappingUpdated();
              }}
              index={mapping.mapping.getIndexForMappedField(s)!}
              canShowIndex={showSourcesIndex}
              canEditIndex={!s.isPadField()}
            >
              {s.actions.map((associatedFieldAction, idx) => (
                <MappingTransformation
                  key={idx}
                  associatedFieldActionName={associatedFieldAction.name}
                  actionsOptions={actionsOptions}
                  onActionDelimiterChange={() => {
                    void 0;
                  }}
                  actionDelimiters={actionDelimiters}
                  currentActionDelimiter={""}
                  isMultiplicityAction={false}
                  args={associatedFieldAction.definition.arguments
                    .map((arg) =>
                      associatedFieldAction.getArgumentValue(arg.name),
                    )
                    .map((opts) => ({
                      ...opts,
                      label: DataMapperUtil.toDisplayable(opts.name),
                      name: opts.name,
                    }))}
                  onArgValueChange={(value: string, event: any) => {
                    associatedFieldAction.setArgumentValue(
                      event.target.name,
                      value,
                    );
                    // notifyMappingUpdated();
                  }}
                  onActionChange={(name: string) =>
                    onActionChange(
                      s.actions[idx],
                      availableActions.find((a) => a.name === name)!,
                    )
                  }
                  onRemoveTransformation={() => {
                    const action = s.actions.find(
                      (a) => a.name === s.actions[idx].name,
                    )!;
                    DataMapperUtil.removeItemFromArray(action!, s.actions);
                    // notifyMappingUpdated();
                  }}
                />
              ))}
            </MappingField>
          );
        })}
      </MappingFields>

      <MappingFields title={"Targets"}>
        {targets.map((t) => {
          const availableActions = getMappingActions(false);
          const actionsOptions = availableActions.map((a) => ({
            name: DataMapperUtil.toDisplayable(a.name),
            value: a.name,
          }));
          return (
            <MappingField
              key={t.field?.uuid ?? ++nextEmptyKey}
              name={t.field?.name ?? ""}
              info={t.field?.getFieldLabel(true, true) ?? ""}
              onDelete={() =>
                // onRemoveMappedField(() => mapping.removeMappedField(t))
                void 0
              }
              onIndexChange={(event: any) =>
                handleIndexChange(mapping.mapping, t, event)
              }
              onNewTransformation={() => {
                const action: FieldAction = new FieldAction();
                availableActions[0].populateFieldAction(action);
                t.actions.push(action);
                // notifyMappingUpdated();
              }}
              index={mapping.mapping.getIndexForMappedField(t)!}
              canShowIndex={showTargetsIndex}
              canEditIndex={!t.isPadField()}
            >
              {t.actions.map((associatedFieldAction, idx) => (
                <MappingTransformation
                  key={idx}
                  associatedFieldActionName={associatedFieldAction.name}
                  actionsOptions={actionsOptions}
                  onActionDelimiterChange={() => {
                    void 0;
                  }}
                  actionDelimiters={actionDelimiters}
                  currentActionDelimiter={""}
                  isMultiplicityAction={false}
                  args={associatedFieldAction.definition.arguments
                    .map((arg) =>
                      associatedFieldAction.getArgumentValue(arg.name),
                    )
                    .map((opts) => ({
                      ...opts,
                      label: DataMapperUtil.toDisplayable(opts.name),
                      name: opts.name,
                    }))}
                  onArgValueChange={(value: string, event: any) => {
                    associatedFieldAction.setArgumentValue(
                      event.target.name,
                      value,
                    );
                    // notifyMappingUpdated();
                  }}
                  onActionChange={(name: string) =>
                    onActionChange(
                      t.actions[idx],
                      availableActions.find((a) => a.name === name)!,
                    )
                  }
                  onRemoveTransformation={() => {
                    const action = t.actions.find(
                      (a) => a.name === t.actions[idx].name,
                    )!;
                    DataMapperUtil.removeItemFromArray(action!, t.actions);
                    // notifyMappingUpdated();
                  }}
                />
              ))}
            </MappingField>
          );
        })}
      </MappingFields>
    </MappingDetailsSidebar>
  );
};
