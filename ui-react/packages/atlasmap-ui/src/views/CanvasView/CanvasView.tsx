import React, { FunctionComponent } from 'react';
import { DndProvider } from 'react-dnd';
import HTML5Backend from 'react-dnd-html5-backend';
import { CanvasLinksProvider } from '../../canvas';
import { useDimensions } from '../../common';
import { ElementId, ElementType, IFieldsGroup, IMappings, } from '../../models';
import { CanvasViewCanvas } from './CanvasViewCanvas';
import { DragLayer, FieldGroup, FieldGroupList, FieldsBox, Links, MappingElement, MappingList } from './components';

export interface ICanvasViewProps {
  sources: IFieldsGroup[];
  targets: IFieldsGroup[];
  mappings: IMappings[];
  selectedMapping: string | undefined;
  selectMapping: (id: string) => void;
  deselectMapping: () => void;
  editMapping: () => void;
  addToMapping: (
    elementId: ElementId,
    elementType: ElementType,
    mappingId: string
  ) => void;
}

export const CanvasView: FunctionComponent<ICanvasViewProps> = ({
  sources,
  targets,
  mappings,
  selectedMapping,
  selectMapping,
  deselectMapping,
  editMapping,
  addToMapping
}) => {
  const [dimensionsRef, { width, height, top, left }] = useDimensions();
  const gutter = 30;
  const boxHeight = height - gutter * 2;
  const sourceTargetBoxesWidth = Math.max(250, (width / 6) * 2 - gutter * 2);
  const mappingBoxWidth = Math.max(300, width / 6 - gutter);

  const initialSourceCoords = { x: gutter, y: gutter };
  const initialMappingCoords = {
    x: initialSourceCoords.x + sourceTargetBoxesWidth + gutter * 3,
    y: gutter,
  };
  const initialTargetCoords = {
    x: initialMappingCoords.x + mappingBoxWidth + gutter * 3,
    y: gutter,
  };

  return (
    <DndProvider backend={HTML5Backend}>
      <CanvasLinksProvider>
        <div
          ref={dimensionsRef}
          style={{
            height: '100%',
            flex: '1',
            overflow: 'hidden',
          }}
        >
          <CanvasViewCanvas
            width={width}
            height={height}
            offsetLeft={left}
            offsetTop={top}
          >
            <FieldsBox
              initialWidth={sourceTargetBoxesWidth}
              initialHeight={boxHeight}
              position={initialSourceCoords}
              title={'Source'}
              hidden={false}
            >
              {sources.map(s => {
                return (
                  <FieldGroupList key={s.id} title={s.title}>
                    {({ ref, isExpanded }) => (
                      <FieldGroup
                        isVisible={true}
                        group={s}
                        boxRef={ref}
                        type={'source'}
                        rightAlign={false}
                        parentExpanded={isExpanded}
                      />
                    )}
                  </FieldGroupList>
                );
              })}
            </FieldsBox>

            <FieldsBox
              initialWidth={mappingBoxWidth}
              initialHeight={boxHeight}
              position={initialMappingCoords}
              title={'Mapping'}
            >
              <MappingList>
                {({ ref }) => (
                  <>
                    {mappings.map(m => {
                      return (
                        <MappingElement
                          key={m.id}
                          node={m}
                          boxRef={ref}
                          selectedMapping={selectedMapping}
                          selectMapping={selectMapping}
                          deselectMapping={deselectMapping}
                          editMapping={editMapping}
                          addToMapping={addToMapping}
                        />
                      );
                    })}
                  </>
                )}
              </MappingList>
            </FieldsBox>

            <FieldsBox
              initialWidth={sourceTargetBoxesWidth}
              initialHeight={boxHeight}
              position={initialTargetCoords}
              title={'Target'}
              rightAlign={true}
              hidden={false}
            >
              {targets.map(t => {
                return (
                  <FieldGroupList key={t.id} title={t.title} rightAlign={true}>
                    {({ ref, isExpanded }) => (
                      <FieldGroup
                        isVisible={true}
                        group={t}
                        boxRef={ref}
                        type={'target'}
                        rightAlign={true}
                        parentExpanded={isExpanded}
                      />
                    )}
                  </FieldGroupList>
                );
              })}
            </FieldsBox>

            <Links
              mappings={mappings}
              selectedMapping={selectedMapping}
            />

            <DragLayer />
          </CanvasViewCanvas>
        </div>
      </CanvasLinksProvider>
    </DndProvider>
  );
};
