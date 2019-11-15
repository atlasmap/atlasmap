import React, { FunctionComponent, useEffect, useState } from 'react';
import { CanvasLinksProvider, useCanvas } from '../../canvas';
import { useDimensions, useMovable } from '../../common';
import { Coords, IFieldsGroup, IMappings } from '../../models';
import { FieldsBox } from './FieldsBox';
import { Links } from './Links';
import { MappingsBox } from './MappingsBox';

export interface IMappingCanvasProps {
  sources: IFieldsGroup[];
  targets: IFieldsGroup[];
  mappings: IMappings[];
  freeView: boolean;
}

export const SourceTargetMapper: FunctionComponent<IMappingCanvasProps> = ({
  sources,
  targets,
  mappings,
  freeView,
}) => {
  const { width, height, redraw } = useCanvas();

  const [sourceAreaRef, sourceAreaDimensions, measureSource] = useDimensions();
  const [mappingAreaRef, mappingAreaDimensions, measureMapping] = useDimensions();
  const [targetAreaRef, targetAreaDimensions, measureTarget] = useDimensions();

  const gutter = 20;
  const boxHeight = height - gutter * 2;
  const sourceTargetBoxesWidth = Math.max(200, width / 7 * 3 - gutter * 2);
  const mappingBoxWidth = Math.max(100, width / 7);

  const initialSourceCoords = { x: gutter, y: gutter };
  const [sourceCoords, setSourceCoords] = useState<Coords>(initialSourceCoords);

  const initialMappingCoords = {
    x: initialSourceCoords.x + sourceTargetBoxesWidth + gutter,
    y: gutter,
  };
  const [mappingCoords, setMappingCoords] = useState<Coords>(initialMappingCoords);

  const initialTargetCoords = {
    x: initialMappingCoords.x + mappingBoxWidth + gutter,
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
    measureSource();
    measureTarget();
    measureMapping();
    redraw();
  }, [freeView, measureTarget, measureSource, measureMapping, redraw]);

  return (
    <CanvasLinksProvider>
      <FieldsBox
        width={sourceTargetBoxesWidth}
        height={freeView ? sourceAreaDimensions.height : boxHeight}
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
        height={freeView ? mappingAreaDimensions.height : boxHeight}
        position={freeView ? mappingCoords : initialMappingCoords}
        scrollable={!freeView}
        mappings={mappings}
        type={'mapping'}
        title={'Mapping'}
        ref={mappingAreaRef}
        {...bindMapping()}
      />

      <FieldsBox
        width={sourceTargetBoxesWidth}
        height={freeView ? targetAreaDimensions.height : boxHeight}
        position={freeView ? targetCoords : initialTargetCoords}
        scrollable={!freeView}
        fields={targets}
        type={'target'}
        title={'Target'}
        rightAlign={true}
        ref={targetAreaRef}
        {...bindTarget()}
      />

      <Links mappings={mappings} />
    </CanvasLinksProvider>
  );
};
