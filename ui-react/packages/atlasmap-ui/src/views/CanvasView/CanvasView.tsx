import React, { FunctionComponent } from 'react';
import { DndProvider } from 'react-dnd';
import HTML5Backend from 'react-dnd-html5-backend';
import { CanvasLinksProvider } from '../../canvas';
import { CanvasViewFieldsProvider } from './CanvasViewFieldsProvider';
import { CanvasViewLayoutProvider } from './CanvasViewLayoutProvider';
import { CanvasViewCanvas } from './components';

export const CanvasView: FunctionComponent = ({
  children
}) => {
  return (
    <DndProvider backend={HTML5Backend}>
      <CanvasLinksProvider>
        <CanvasViewCanvas>
          <CanvasViewLayoutProvider>
            <CanvasViewFieldsProvider>
              {children}
            </CanvasViewFieldsProvider>
          </CanvasViewLayoutProvider>
        </CanvasViewCanvas>
      </CanvasLinksProvider>
    </DndProvider>
  );
};
