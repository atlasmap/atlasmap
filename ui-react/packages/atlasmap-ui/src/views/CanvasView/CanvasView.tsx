import React, {
  FunctionComponent, ReactNode,
  useCallback,
  useMemo,
  useState,
} from 'react';
import { DndProvider } from 'react-dnd';
import HTML5Backend from 'react-dnd-html5-backend';
import { useGesture } from 'react-use-gesture';
import { Canvas, CanvasLinksProvider } from '../../canvas';
import { useDimensions } from '../../common';
import {
  Coords,
  ElementId,
  ElementType,
  IFieldsGroup,
  IMappings,
} from '../../models';
import { ControlBar } from './ControlBar';
import { DragLayer } from './DragLayer';
import { FieldGroup } from './FieldGroup';
import { FieldGroupList } from './FieldGroupList';
import { Links } from './Links';
import { ViewToolbar } from './ViewToolbar';
import { MappingElement } from './MappingElement';
import { FieldsBox } from './FieldsBox';
import { MappingList } from './MappingList';

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
  setViewToolbar: (el: ReactNode) => void;
  setControlBar: (el: ReactNode) => void;
}

export const CanvasView: FunctionComponent<ICanvasViewProps> = ({
  sources,
  targets,
  mappings,
  selectedMapping,
  selectMapping,
  deselectMapping,
  editMapping,
  addToMapping,
  setViewToolbar,
  setControlBar
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

  const [freeView, setFreeView] = useState(false);
  const [materializedMappings, setMaterializedMappings] = useState(true);

  const toggleFreeView = useCallback(() =>
    setFreeView(!freeView),
    [
    freeView,
    setFreeView,
  ]);
  const toggleMaterializedMappings = useCallback(
    () => setMaterializedMappings(!materializedMappings),
    [setMaterializedMappings, materializedMappings]
  );

  const [zoom, setZoom] = useState(1);
  const [isPanning, setIsPanning] = useState(false);
  const [{ x: panX, y: panY }, setPan] = useState<Coords>({ x: 0, y: 0 });
  const bind = useGesture(
    {
      onDrag: ({ movement: [x, y], first, last, memo = [panX, panY] }) => {
        if (freeView) {
          if (first) setIsPanning(true);
          if (last) setIsPanning(false);
          setPan({ x: x + memo[0], y: y + memo[1] });
        }
        return memo;
      },
      onWheel: ({ delta }) => {
        if (freeView) {
          updateZoom(delta[1] * -0.001);
        }
      },
    },
    { dragDelay: true }
  );

  const updateZoom = useCallback(
    (tick: number) => {
      setZoom(currentZoom => Math.max(0.2, Math.min(2, currentZoom + tick)));
    },
    [setZoom]
  );

  const resetPan = useCallback(() => {
    setPan({ x: 0, y: 0 });
  }, [setPan]);

  const handleZoomIn = useCallback(() => {
    updateZoom(0.2);
  }, [updateZoom]);
  const handleZoomOut = useCallback(() => {
    updateZoom(-0.2);
  }, [updateZoom]);
  const handleViewReset = useCallback(() => {
    setZoom(1);
    resetPan();
  }, [setZoom, resetPan]);

  const viewToolbar = useMemo(
    () => (
      <ViewToolbar
        freeView={freeView}
        toggleFreeView={toggleFreeView}
        materializedMappings={materializedMappings}
        toggleMaterializedMappings={toggleMaterializedMappings}
      />
    ),
    [freeView, materializedMappings, toggleFreeView, toggleMaterializedMappings]
  );

  const controlBar = useMemo(
    () => (
      <ControlBar
        onZoomIn={handleZoomIn}
        onZoomOut={handleZoomOut}
        onZoomReset={handleViewReset}
      />
    ),
    [handleViewReset, handleZoomIn, handleZoomOut]
  );

  setViewToolbar(viewToolbar);
  setControlBar(freeView ? controlBar : undefined);

  return (
    <DndProvider backend={HTML5Backend}>
      <div
        ref={dimensionsRef}
        style={{
          height: '100%',
          flex: '1',
          overflow: 'hidden',
        }}
      >
        <CanvasLinksProvider>
          <Canvas
            width={width}
            height={height}
            offsetLeft={left}
            offsetRight={top}
            allowPanning={freeView}
            isPanning={freeView ? isPanning : false}
            panX={freeView ? panX : 0}
            panY={freeView ? panY : 0}
            zoom={freeView ? zoom : 1}
            {...bind()}
          >
            <FieldsBox
              width={sourceTargetBoxesWidth}
              height={freeView ? undefined : boxHeight}
              position={initialSourceCoords}
              scrollable={!freeView}
              title={'Source'}
              hidden={false}
            >
              {sources.map(s => {
                return (
                  <FieldGroupList key={s.id}>
                    {({ ref }) => (
                      <FieldGroup
                        isVisible={true}
                        group={s}
                        boxRef={ref}
                        type={'source'}
                        rightAlign={false}
                      />
                    )}
                  </FieldGroupList>
                );
              })}
            </FieldsBox>

            <FieldsBox
              width={mappingBoxWidth}
              height={freeView ? undefined : boxHeight}
              position={initialMappingCoords}
              scrollable={!freeView}
              title={'Mapping'}
              hidden={!materializedMappings}
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
              width={sourceTargetBoxesWidth}
              height={freeView ? undefined : boxHeight}
              position={initialTargetCoords}
              scrollable={!freeView}
              title={'Target'}
              rightAlign={true}
              hidden={false}
            >
              {targets.map(s => {
                return (
                  <FieldGroupList key={s.id}>
                    {({ ref }) => (
                      <FieldGroup
                        isVisible={true}
                        group={s}
                        boxRef={ref}
                        type={'target'}
                        rightAlign={true}
                      />
                    )}
                  </FieldGroupList>
                );
              })}
            </FieldsBox>

            <Links
              mappings={mappings}
              materializedMappings={materializedMappings}
              selectedMapping={selectedMapping}
            />

            <DragLayer />
          </Canvas>
        </CanvasLinksProvider>
      </div>
    </DndProvider>
  );
};
