import { MappingModel } from './models/mapping.model';
import React, { FunctionComponent } from 'react';
import {
  MappingAction,
  MappingDetails,
  MappingField,
  MappingFields,
} from '@atlasmap/ui';
import { useAtlasmap } from './AtlasmapProvider';
import { DataMapperUtil } from './common/data-mapper-util';
import { FieldAction } from './models/field-action.model';
import { ConfigModel } from './models/config.model';

export interface IAtlasmapMappingDetaisProps {
  mapping: MappingModel;
  closeDetails: () => void;
  onRemoveMappedField: (remove: () => void) => void;
  onNewTransformation: (newTransformation: () => void) => void;
  onRemoveTransformation: (removeTransformation: () => void) => void;
}

export const AtlasmapMappingDetails: FunctionComponent<
  IAtlasmapMappingDetaisProps
> = ({ mapping, closeDetails, onRemoveMappedField }) => {
  const { getMappingActions, handleActionChange } = useAtlasmap();

  const cfg = ConfigModel.getConfig();
  const sources = mapping.getMappedFields(true);
  const showSourcesIndex =
    sources.length > 1 && !mapping.transition.enableExpression;
  const targets = mapping.getMappedFields(false);
  const showTargetIndex =
    sources.length > 1 && !mapping.transition.enableExpression;
  return (
    <MappingDetails onDelete={() => void 0} onClose={closeDetails}>
      <MappingFields title={'Sources'}>
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
                  args={associatedFieldAction.definition.arguments
                    .map(arg => associatedFieldAction.getArgumentValue(arg.name))
                      .map(opts => ({
                        ...opts,
                        label: DataMapperUtil.toDisplayable(opts.name),
                        name: opts.name,
                      })
                    )
                  }
                  onArgValueChange={(value: string, event: any) => {
                    associatedFieldAction.setArgumentValue(event.target.name, value);
                    cfg.mappingService.notifyMappingUpdated();
                  }}
                  onActionChange={(name:string) =>
                    handleActionChange(
                      s.actions[idx],
                      availableActions.find(a => a.name === name)!
                    )
                  }
                  onRemoveTransformation={() => {
                    const action = s.actions.find(a => a.name === s.actions[idx].name)!;
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
              {
                t.actions.map((associatedFieldAction, idx) => (
                <MappingAction
                  key={idx}
                  associatedFieldActionName={associatedFieldAction.name}
                  actionsOptions={actionsOptions}
                  args={associatedFieldAction.definition.arguments
                    .map(arg => associatedFieldAction.getArgumentValue(arg.name))
                      .map(opts => ({
                        ...opts,
                        label: DataMapperUtil.toDisplayable(opts.name),
                        name: opts.name,
                  }))}
                  onArgValueChange={(value: string, event: any) => {
                    associatedFieldAction.setArgumentValue(event.target.name, value);
                    cfg.mappingService.notifyMappingUpdated();
                  }}
                  onActionChange={(name:string) =>
                    handleActionChange(
                      t.actions[idx],
                      availableActions.find(a => a.name === name)!
                    )
                  }
                  onRemoveTransformation={() => {
                    const action = t.actions.find(a => a.name === t.actions[idx].name)!;
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
