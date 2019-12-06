import React, { FunctionComponent } from 'react';
import { CanvasViewCanvasProvider } from './CanvasViewCanvasProvider';
import { CanvasViewOptionsProvider } from './CanvasViewOptionsProvider';

export const CanvasViewProvider: FunctionComponent = ({ children }) => (
  <CanvasViewOptionsProvider>
    <CanvasViewCanvasProvider>
      {children}
    </CanvasViewCanvasProvider>
  </CanvasViewOptionsProvider>
);
