import React, { FunctionComponent, useEffect, useState } from 'react';
import { CanvasLinksProvider, useCanvas } from '../../canvas';
import { useDimensions, useMovable } from '../../common';
import { Coords, IFieldsGroup, IMappings } from '../../models';
import { FieldsBox } from './FieldsBox';
import { Links } from './Links';
import { MappingsBox } from './MappingsBox';
import { SourceTargetMapperProvider } from './SourceTargetMapperContext';

export interface IMappingCanvasProps {
  sources: IFieldsGroup[];
  targets: IFieldsGroup[];
  mappings: IMappings[];
  selectedMapping?: string;
  materializedMappings?: boolean;
  freeView: boolean;
}

export const SourceTargetMapper: FunctionComponent<IMappingCanvasProps> = ({
  sources,
  targets,
  mappings,
  selectedMapping,
                                                                             materializedMappings = true,
  freeView,
}) => {
  const { width, height, redraw, addRedrawListener, removeRedrawListener, yDomain } = useCanvas();

  const [sourceAreaRef, sourceAreaDimensions, measureSource] = useDimensions();
  const [mappingAreaRef, mappingAreaDimensions, measureMapping] = useDimensions();
  const [targetAreaRef, targetAreaDimensions, measureTarget] = useDimensions();

  const gutter = 30;
  const boxHeight = height - gutter * 2;
  const sourceTargetBoxesWidth = Math.max(250, width / 6 * 2 - gutter * 2);
  const mappingBoxWidth = Math.max(300, width / 6 - gutter);

  const initialSourceCoords = { x: gutter, y: gutter };
  const [sourceCoords, setSourceCoords] = useState<Coords>(initialSourceCoords);

  const initialMappingCoords = {
    x: initialSourceCoords.x + sourceTargetBoxesWidth + gutter * 3,
    y: gutter,
  };
  const [mappingCoords, setMappingCoords] = useState<Coords>(initialMappingCoords);

  const initialTargetCoords = {
    x: initialMappingCoords.x + mappingBoxWidth + gutter * 3,
    y: gutter,
  };
  const [targetCoords, setTargetCoords] = useState<Coords>(initialTargetCoords);

  const bindSource = useMovable({
    enabled: freeView,
    initialPosition: sourceCoords,
    onDrag: (coords: Coords) => {
      setSourceCoords(coords);
      redraw();
    },
    xBoundaries: [-Infinity, mappingCoords.x - sourceTargetBoxesWidth - gutter],
  });

  const bindMapping = useMovable({
    enabled: freeView,
    initialPosition: mappingCoords,
    onDrag: (coords: Coords) => {
      setMappingCoords(coords);
      redraw();
    },
    xBoundaries: [sourceCoords.x + sourceTargetBoxesWidth + gutter, targetCoords.x - sourceTargetBoxesWidth - gutter],
  });

  const bindTarget = useMovable({
    enabled: freeView,
    initialPosition: targetCoords,
    onDrag: (coords: Coords) => {
      setTargetCoords(coords);
      redraw();
    },
    xBoundaries: [mappingCoords.x + mappingBoxWidth + gutter, +Infinity],
  });

  useEffect(() => {
    addRedrawListener(measureSource);
    addRedrawListener(measureTarget);
    addRedrawListener(measureMapping);
    return () => {
      removeRedrawListener(measureSource);
      removeRedrawListener(measureTarget);
      removeRedrawListener(measureMapping);
    }
  }, [addRedrawListener, removeRedrawListener, measureMapping, measureSource, measureTarget]);

  useEffect(() => {
    redraw();
  }, [freeView, materializedMappings, redraw]);

  return (
    <SourceTargetMapperProvider selectedMapping={selectedMapping}>
      <CanvasLinksProvider>
        <FieldsBox
          width={sourceTargetBoxesWidth}
          height={freeView ? yDomain(sourceAreaDimensions.height) : boxHeight}
          position={freeView ? sourceCoords : initialSourceCoords}
          scrollable={!freeView}
          fields={sources}
          type={'source'}
          title={'Source'}
          ref={sourceAreaRef}
          {...bindSource()}
        />

        <MappingsBox
          width={mappingBoxWidth}
          height={freeView ? yDomain(mappingAreaDimensions.height) : boxHeight}
          position={freeView ? mappingCoords : initialMappingCoords}
          scrollable={!freeView}
          mappings={mappings}
          type={'mapping'}
          title={'Mapping'}
          ref={mappingAreaRef}
          hidden={!materializedMappings}
          {...bindMapping()}
        />

        <FieldsBox
          width={sourceTargetBoxesWidth}
          height={freeView ? yDomain(targetAreaDimensions.height) : boxHeight}
          position={freeView ? targetCoords : initialTargetCoords}
          scrollable={!freeView}
          fields={targets}
          type={'target'}
          title={'Target'}
          rightAlign={true}
          ref={targetAreaRef}
          {...bindTarget()}
        />

        <Links
          mappings={mappings}
          materializedMappings={materializedMappings}
        />
      </CanvasLinksProvider>
    </SourceTargetMapperProvider>
  );
};
