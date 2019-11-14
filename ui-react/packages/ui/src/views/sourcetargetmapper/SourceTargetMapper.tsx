import { CanvasLinksProvider, useCanvas } from '@src/canvas';
import { useMovable } from '@src/common';
import { Coords, IMappings, IFieldsGroup } from '@src/models';
import { useDimensions } from '@src/common/useDimensions';
import { FieldsBox } from '@src/views/sourcetargetmapper/FieldsBox';
import { Links } from '@src/views/sourcetargetmapper/Links';
import { MappingsBox } from '@src/views/sourcetargetmapper/MappingsBox';
import React, { FunctionComponent, useEffect, useState } from 'react';

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
  const boxWidth = Math.max(200, width / 3 - gutter * 2);

  const initialSourceCoords = { x: gutter, y: gutter };
  const [sourceCoords, setSourceCoords] = useState<Coords>(initialSourceCoords);

  const initialMappingCoords = {
    x: Math.max(width / 3 - gutter, boxWidth + gutter) + gutter,
    y: gutter,
  };
  const [mappingCoords, setMappingCoords] = useState<Coords>(initialMappingCoords);

  const initialTargetCoords = {
    x: (Math.max(width / 3 - gutter, boxWidth + gutter) + gutter) * 2,
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
    xBoundaries: [-Infinity, mappingCoords.x - boxWidth - gutter],
  });

  const bindMapping = useMovable({
    enabled: freeView,
    initialPosition: mappingCoords,
    onDrag: (coords: Coords) => {
      setMappingCoords(coords);
      redraw();
    },
    xBoundaries: [sourceCoords.x + boxWidth + gutter, targetCoords.x - boxWidth - gutter],
  });

  const bindTarget = useMovable({
    enabled: freeView,
    initialPosition: targetCoords,
    onDrag: (coords: Coords) => {
      setTargetCoords(coords);
      redraw();
    },
    xBoundaries: [mappingCoords.x + boxWidth + gutter, +Infinity],
  });

  useEffect(() => {
    measureSource();
    measureTarget();
    measureMapping();
    redraw();
  }, [freeView, measureTarget, measureSource, measureMapping]);

  return (
    <CanvasLinksProvider>
      <FieldsBox
        width={boxWidth}
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
        width={boxWidth}
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
        width={boxWidth}
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
