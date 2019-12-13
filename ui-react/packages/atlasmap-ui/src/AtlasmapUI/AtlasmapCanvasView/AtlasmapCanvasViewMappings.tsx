import React, { FunctionComponent } from 'react';
import { DropTarget } from './DropTarget';
import {ElementId, Mapping, MappingElement} from '../../CanvasView';
import { useAtlasmapUI } from '../AtlasmapUIProvider';

export interface IAtlasmapCanvasViewMappingsProps {
  onAddToMapping: (elementId: ElementId, mappingId: string) => void;
}

export const AtlasmapCanvasViewMappings: FunctionComponent<IAtlasmapCanvasViewMappingsProps> = ({ onAddToMapping }) => {
  const {
    mappings,
    isFieldAddableToSelection,
    selectedMapping,
    selectMapping,
    deselectMapping,
    editMapping,
  } = useAtlasmapUI();
  return (
    <Mapping>
      {({ ref }) => (
        <>
          {mappings.map(m => {
            return (
              <DropTarget
                key={m.id}
                boxRef={ref}
                onDrop={itemId => onAddToMapping(itemId, m.id)}
                isFieldDroppable={(documentType, fieldId) =>
                  isFieldAddableToSelection(m, documentType, fieldId)
                }
              >
                {({ canDrop, isOver }) => (
                  <MappingElement
                    boxRef={ref}
                    node={m}
                    selectedMapping={selectedMapping}
                    selectMapping={selectMapping}
                    deselectMapping={deselectMapping}
                    editMapping={editMapping}
                    canDrop={canDrop}
                    isOver={isOver}
                  />
                )}
              </DropTarget>
            );
          })}
        </>
      )}
    </Mapping>
  );
};
