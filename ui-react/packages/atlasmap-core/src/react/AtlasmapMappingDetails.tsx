import { MappingModel } from '../models/mapping.model';
import React, { FunctionComponent } from 'react';
import {
  MappingAction,
  MappingDetails,
  MappingField,
  MappingFields,
} from '@atlasmap/ui';
import { useAtlasmap } from './AtlasmapProvider';
import { DataMapperUtil } from '../common/data-mapper-util';
import { FieldAction } from '../models/field-action.model';
import { ConfigModel } from '../models/config.model';
import { MappedField } from '../models/mapping.model';

export interface IAtlasmapMappingDetailsProps {
  mapping: MappingModel;
  closeDetails: () => void;
  onRemoveMappedField: (remove: () => void) => void;
  onNewTransformation: (newTransformation: () => void) => void;
  onRemoveTransformation: (removeTransformation: () => void) => void;
}

export const handleIndexChange = (
  mapping: MappingModel,
  mField: MappedField,
  event: any
) => {
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
      mField.field!.isSource()
    );
  }
  cfg.mappingService.moveMappedFieldTo(mapping, mField, insertionIndex);
};

export const AtlasmapMappingDetails: FunctionComponent<IAtlasmapMappingDetailsProps> = ({
  mapping,
  closeDetails,
  onRemoveMappedField,
}) => {
  const {
    getMappingActions,
    getMultiplicityActions,
    getMultiplicityActionDelimiters,
    handleActionChange,
    removeMapping,
  } = useAtlasmap();

  const cfg = ConfigModel.getConfig();
  const sources = mapping.getMappedFields(true);
  const showSourcesIndex =
    sources.length > 1 &&
    !mapping.transition.enableExpression &&
    mapping.transition.isManyToOneMode();
  const targets = mapping.getMappedFields(false);
  const showTargetIndex =
    targets.length > 1 &&
    !mapping.transition.enableExpression &&
    mapping.transition.isOneToManyMode();
  const availableActions = getMultiplicityActions(mapping);
  const actionsOptions = availableActions.map(a => ({
    name: DataMapperUtil.toDisplayable(a.name),
    value: a.name,
  }));
  const availableDelimiters = getMultiplicityActionDelimiters();
  const actionDelimiters = availableDelimiters.map(a => ({
    displayName: a.prettyName!,
    delimiterValue: a.actualDelimiter,
  }));
  const multiplicityFieldAction = mapping.transition.transitionFieldAction;
  const multiplicityId = `multiplicity-${multiplicityFieldAction}`;
  const handleRemoveMapping = () => {
    removeMapping(mapping);
    closeDetails();
  };

  return (
    <MappingDetails onDelete={handleRemoveMapping} onClose={closeDetails}>
      <MappingFields title={'Sources'}>
        {multiplicityFieldAction && (
          <MappingAction
            key={multiplicityId}
            associatedFieldActionName={multiplicityFieldAction.name}
            actionsOptions={actionsOptions}
            actionDelimiters={actionDelimiters}
            currentActionDelimiter={
              actionDelimiters[mapping.transition.delimiter].delimiterValue
            }
            isMultiplicityAction={true}
            args={multiplicityFieldAction.definition.arguments
              .map(arg => multiplicityFieldAction.getArgumentValue(arg.name))
              .map(opts => ({
                ...opts,
                label: DataMapperUtil.toDisplayable(opts.name),
                name: opts.name,
                value: opts.value,
              }))}
            onArgValueChange={(value: string, event: any) => {
              multiplicityFieldAction.setArgumentValue(
                event.target.name,
                value
              );
              cfg.mappingService.notifyMappingUpdated();
            }}
            onActionChange={(name: string) =>
              handleActionChange(
                mapping.transition.transitionFieldAction,
                availableActions.find(a => a.name === name)!
              )
            }
            onActionDelimiterChange={(delimiterValue: string) => {
              multiplicityFieldAction.setArgumentValue(
                'delimiter',
                delimiterValue
              );
              cfg.mappingService.notifyMappingUpdated();
            }}
            onRemoveTransformation={() => {
              void 0;
            }}
          />
        )}
        {sources.map(s => {
          const availableActions = getMappingActions(true);
          const actionsOptions = availableActions.map(a => ({
            name: DataMapperUtil.toDisplayable(a.name),
            value: a.name,
          }));
          return (
            <MappingField
              key={s.field!.uuid}
              name={s.field!.name}
              info={s.field!.getFieldLabel(true, true)}
              onDelete={() =>
                onRemoveMappedField(() => mapping.removeMappedField(s))
              }
              onIndexChange={(event: any) =>
                handleIndexChange(mapping, s, event)
              }
              onNewTransformation={() => {
                const action: FieldAction = new FieldAction();
                availableActions[0].populateFieldAction(action);
                s.actions.push(action);
                cfg.mappingService.notifyMappingUpdated();
              }}
              index={mapping.getIndexForMappedField(s)!}
              showIndex={showSourcesIndex}
              canEditIndex={!s.isPadField()}
            >
              {s.actions.map((associatedFieldAction, idx) => (
                <MappingAction
                  key={idx}
                  associatedFieldActionName={associatedFieldAction.name}
                  actionsOptions={actionsOptions}
                  onActionDelimiterChange={() => {
                    void 0;
                  }}
                  actionDelimiters={actionDelimiters}
                  currentActionDelimiter={''}
                  isMultiplicityAction={false}
                  args={associatedFieldAction.definition.arguments
                    .map(arg =>
                      associatedFieldAction.getArgumentValue(arg.name)
                    )
                    .map(opts => ({
                      ...opts,
                      label: DataMapperUtil.toDisplayable(opts.name),
                      name: opts.name,
                    }))}
                  onArgValueChange={(value: string, event: any) => {
                    associatedFieldAction.setArgumentValue(
                      event.target.name,
                      value
                    );
                    cfg.mappingService.notifyMappingUpdated();
                  }}
                  onActionChange={(name: string) =>
                    handleActionChange(
                      s.actions[idx],
                      availableActions.find(a => a.name === name)!
                    )
                  }
                  onRemoveTransformation={() => {
                    const action = s.actions.find(
                      a => a.name === s.actions[idx].name
                    )!;
                    DataMapperUtil.removeItemFromArray(action!, s.actions);
                    cfg.mappingService.notifyMappingUpdated();
                  }}
                />
              ))}
            </MappingField>
          );
        })}
      </MappingFields>

      <MappingFields title={'Targets'}>
        {targets.map(t => {
          const availableActions = getMappingActions(false);
          const actionsOptions = availableActions.map(a => ({
            name: DataMapperUtil.toDisplayable(a.name),
            value: a.name,
          }));
          return (
            <MappingField
              key={t.field!.uuid}
              name={t.field!.name}
              info={t.field!.getFieldLabel(true, true)}
              onDelete={() =>
                onRemoveMappedField(() => mapping.removeMappedField(t))
              }
              onIndexChange={(event: any) =>
                handleIndexChange(mapping, t, event)
              }
              onNewTransformation={() => {
                const action: FieldAction = new FieldAction();
                availableActions[0].populateFieldAction(action);
                t.actions.push(action);
                cfg.mappingService.notifyMappingUpdated();
              }}
              index={mapping.getIndexForMappedField(t)!}
              showIndex={showTargetIndex}
              canEditIndex={!t.isPadField()}
            >
              {t.actions.map((associatedFieldAction, idx) => (
                <MappingAction
                  key={idx}
                  associatedFieldActionName={associatedFieldAction.name}
                  actionsOptions={actionsOptions}
                  onActionDelimiterChange={() => {
                    void 0;
                  }}
                  actionDelimiters={actionDelimiters}
                  currentActionDelimiter={''}
                  isMultiplicityAction={false}
                  args={associatedFieldAction.definition.arguments
                    .map(arg =>
                      associatedFieldAction.getArgumentValue(arg.name)
                    )
                    .map(opts => ({
                      ...opts,
                      label: DataMapperUtil.toDisplayable(opts.name),
                      name: opts.name,
                    }))}
                  onArgValueChange={(value: string, event: any) => {
                    associatedFieldAction.setArgumentValue(
                      event.target.name,
                      value
                    );
                    cfg.mappingService.notifyMappingUpdated();
                  }}
                  onActionChange={(name: string) =>
                    handleActionChange(
                      t.actions[idx],
                      availableActions.find(a => a.name === name)!
                    )
                  }
                  onRemoveTransformation={() => {
                    const action = t.actions.find(
                      a => a.name === t.actions[idx].name
                    )!;
                    DataMapperUtil.removeItemFromArray(action!, t.actions);
                    cfg.mappingService.notifyMappingUpdated();
                  }}
                />
              ))}
            </MappingField>
          );
        })}
      </MappingFields>
    </MappingDetails>
  );
};
