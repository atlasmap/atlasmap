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
import { Column, ColumnMapper, Columns, NodeRefProvider } from '../UI';
import {
  ISourceColumnCallbacks,
  ISourceTargetLinksProps,
  ISourcesColumnData,
  ITargetsColumnCallbacks,
  ITargetsColumnData,
  SourceTargetLinks,
  SourcesColumn,
  TargetsColumn,
} from './ColumnMapperView';
import React, { FunctionComponent } from 'react';

export interface ISourceTargetViewProps
  extends ISourcesColumnData,
    ITargetsColumnData,
    ISourceTargetLinksProps {
  onSelectMapping: ISourceTargetLinksProps['onSelectMapping'];
  sourceEvents: ISourceColumnCallbacks;
  targetEvents: ITargetsColumnCallbacks;
}

export const SourceTargetView: FunctionComponent<ISourceTargetViewProps> = ({
  sourceProperties,
  targetProperties,
  constants,
  sources,
  mappings,
  targets,
  showMappingPreview,
  showTypes,
  selectedMappingId,
  onSelectMapping,
  sourceEvents,
  targetEvents,
}) => {
  return (
    <>
      <NodeRefProvider>
        <ColumnMapper role={'main'}>
          <Columns>
            <Column data-testid={'column-source-area'} totalColumns={2}>
              <SourcesColumn
                sourceProperties={sourceProperties}
                constants={constants}
                sources={sources}
                showTypes={showTypes}
                {...sourceEvents}
              />
            </Column>

            <Column data-testid={'column-target-area'} totalColumns={2}>
              <TargetsColumn
                targetProperties={targetProperties}
                showMappingPreview={showMappingPreview}
                showTypes={showTypes}
                targets={targets}
                {...targetEvents}
              />
            </Column>
          </Columns>
        </ColumnMapper>
        <SourceTargetLinks
          mappings={mappings}
          selectedMappingId={selectedMappingId}
          onSelectMapping={onSelectMapping}
        />
      </NodeRefProvider>
    </>
  );
};
