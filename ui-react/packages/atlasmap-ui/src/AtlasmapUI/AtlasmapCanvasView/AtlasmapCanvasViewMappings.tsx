import React, { FunctionComponent } from 'react';
import { DropTarget } from './DropTarget';
import { Mapping, MappingElement } from '../../CanvasView';
import { useAtlasmapUI } from '../AtlasmapUIProvider';

export const AtlasmapCanvasViewMappings: FunctionComponent = () => {
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
                onDrop={item => item.onAddToMapping(m)}
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
