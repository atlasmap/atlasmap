import React, { FunctionComponent } from 'react';
import { DndProvider } from 'react-dnd';
import HTML5Backend from 'react-dnd-html5-backend';
import { CanvasLinksProvider } from '../Canvas';
import { CanvasViewFieldsProvider } from './CanvasViewFieldsProvider';
import {
  CanvasViewLayoutProvider,
  ICanvasViewLayoutProviderProps,
} from './CanvasViewLayoutProvider';
import { CanvasViewCanvas } from './components';

export interface ICanvasViewProps extends ICanvasViewLayoutProviderProps {}

export const CanvasView: FunctionComponent<ICanvasViewProps> = ({
  showMappingColumn,
  children,
}) => {
  return (
    <DndProvider backend={HTML5Backend}>
      <CanvasLinksProvider>
        <CanvasViewCanvas>
          <CanvasViewLayoutProvider
            showMappingColumn={showMappingColumn}
          >
            <CanvasViewFieldsProvider>{children}</CanvasViewFieldsProvider>
          </CanvasViewLayoutProvider>
        </CanvasViewCanvas>
      </CanvasLinksProvider>
    </DndProvider>
  );
};
