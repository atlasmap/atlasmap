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
import { ErrorInfo, ErrorLevel, ErrorScope, ErrorType } from './models/error.model';

export interface IAtlasmapMappingDetaisProps {
  mapping: MappingModel;
  closeDetails: () => void;
  onRemoveMappedField: (remove: () => void) => void;
  onNewTransformation: (newTransformation: () => void) => void;
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
          const actions = getMappingActions(true);
          if (!actions) {
            cfg.errorService.addError(new ErrorInfo({message:
              'The selected field has no applicable transformation actions.',
              level: ErrorLevel.INFO, scope: ErrorScope.MAPPING, type: ErrorType.USER}));
            return;
          }
          const actionsOptions = actions.map(a => ({
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
                actions[0].populateFieldAction(action);
                s.actions.push(action);
                cfg.mappingService.notifyMappingUpdated();
              }}
              index={mapping.getIndexForMappedField(s)!}
              showIndex={showSourcesIndex}
              canEditIndex={!s.isPadField()}
            >
              {s.actions.map((a, idx) => (
                <MappingAction
                  key={idx}
                  value={a.name}
                  actions={actionsOptions}
                  args={a.definition.arguments
                    .map(arg => a.getArgumentValue(arg.name))
                    .map(opts => ({
                      ...opts,
                      name: DataMapperUtil.toDisplayable(opts.name),
                    }))}
                  onChange={name =>
                    handleActionChange(
                      s.actions[idx],
                      actions.find(a => a.name === name)!
                    )
                  }
                />
              ))}
            </MappingField>
          );
        })}
      </MappingFields>
      <MappingFields title={'Targets'}>
        {targets.map(t => {
          const actions = getMappingActions(false);
          if (!actions) {
            cfg.errorService.addError(new ErrorInfo({message:
              'The selected field has no applicable transformation actions.',
              level: ErrorLevel.INFO, scope: ErrorScope.MAPPING, type: ErrorType.USER}));
            return;
          }
          const actionsOptions = actions.map(a => ({
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
                actions[0].populateFieldAction(action);
                t.actions.push(action);
                cfg.mappingService.notifyMappingUpdated();
              }}
              index={mapping.getIndexForMappedField(t)!}
              showIndex={showTargetIndex}
              canEditIndex={!t.isPadField()}
            >
              {
                t.actions.map((a, idx) => (
                <MappingAction
                  key={idx}
                  value={a.name}
                  actions={actionsOptions}
                  args={a.definition.arguments
                    .map(arg => a.getArgumentValue(arg.name))
                    .map(opts => ({
                      ...opts,
                      name: DataMapperUtil.toDisplayable(opts.name),
                    }))}
                  onChange={name =>
                    handleActionChange(
                      t.actions[idx],
                      actions.find(a => a.name === name)!
                    )
                  }
                />
              ))}
            </MappingField>
          );
        })}
      </MappingFields>
    </MappingDetails>
  );
};
