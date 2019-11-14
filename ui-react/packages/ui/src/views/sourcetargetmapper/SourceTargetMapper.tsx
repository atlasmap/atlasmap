import { Title } from '@patternfly/react-core';
import { CanvasLinksProvider, CanvasObject, useCanvas } from '@src/canvas';
import { Coords, Mapping, MappingGroup } from '@src/models';
import { useDimensions } from '@src/useDimensions';
import { Box } from '@src/views/sourcetargetmapper/Box';
import { FieldGroupList } from '@src/views/sourcetargetmapper/FieldGroupList';
import { FieldGroup } from '@src/views/sourcetargetmapper/FieldGroup';
import { Links } from '@src/views/sourcetargetmapper/Links';
import React, { FunctionComponent, useEffect, useRef, useState } from 'react';
import { useDrag } from 'react-use-gesture';
import clamp from 'lodash.clamp'

export interface IMappingCanvasProps {
  sources: MappingGroup[];
  targets: MappingGroup[];
  mappings: Mapping[];
  freeView: boolean;
}

export const SourceTargetMapper: FunctionComponent<IMappingCanvasProps> = ({
  sources,
  targets,
  mappings,
  freeView
}) => {
  const { width, height, redraw } = useCanvas();

  const [sourceAreaRef, sourceAreaDimensions, measureSource] = useDimensions();
  const [targetAreaRef, targetAreaDimensions, measureTarget] = useDimensions();
  const sourceFieldsRef = useRef<HTMLDivElement | null>(null);
  const targetFieldsRef = useRef<HTMLDivElement | null>(null);

  const gutter = 20;
  const boxHeight = height - gutter * 2;
  const boxWidth = Math.max(200, width / 2 - gutter * 3);
  const initialSourceCoords = { x: gutter, y: gutter };
  const [sourceCoords, setSourceCoords] = useState<Coords>(initialSourceCoords);
  const initialTargetCoords = { x: Math.max(width / 2, boxWidth + gutter) + gutter * 2, y: gutter };
  const [targetCoords, setTargetCoords] = useState<Coords>(initialTargetCoords);

  const bindSource = useDrag(
    ({ event, movement: [x, y], memo = [sourceCoords.x, sourceCoords.y] }) => {
      if (freeView) {
        event!.stopPropagation();
        setSourceCoords({
          x: clamp(x + memo[0], -Infinity, targetCoords.x - boxWidth - gutter),
          y: y + memo[1]
        });
        redraw();
      }
      return memo;
    }
  );

  const bindTarget = useDrag(
    ({ event, movement: [x, y], memo = [targetCoords.x, targetCoords.y] }) => {
      if (freeView) {
        event!.stopPropagation();
        setTargetCoords({
          x: clamp(x + memo[0], sourceCoords.x + boxWidth + gutter, +Infinity),
          y: y + memo[1]
        });
        redraw();
      }
      return memo;
    }
  );

  useEffect(() => {
    measureSource();
    measureTarget();
    redraw();
  }, [freeView, measureTarget, measureSource])

  return (
    <CanvasLinksProvider>
      <CanvasObject
        width={boxWidth}
        height={freeView ? sourceAreaDimensions.height : boxHeight}
        x={freeView ? sourceCoords.x : initialSourceCoords.x}
        y={freeView ? sourceCoords.y : initialSourceCoords.y}
      >
        <div
          ref={sourceAreaRef}
          style={{
            height: freeView ? undefined : '100%'
          }}
          {...bindSource()}
        >
          <Box
            header={
              <Title size={'2xl'} headingLevel={'h2'}>
                Source
              </Title>
            }
            footer={<p>{sources.length} fields</p>}
            ref={sourceFieldsRef}
          >
            <FieldGroupList>
              {sources.map(s => {
                return (
                  <FieldGroup
                    isVisible={true}
                    group={s}
                    key={s.id}
                    boxRef={sourceFieldsRef.current}
                    type={'source'}
                  />
                );
              })}
            </FieldGroupList>
          </Box>
        </div>
      </CanvasObject>

      <CanvasObject
        width={boxWidth}
        height={freeView ? targetAreaDimensions.height : boxHeight}
        x={freeView ? targetCoords.x : initialTargetCoords.x}
        y={freeView ? targetCoords.y : initialTargetCoords.y}
      >
        <div
          ref={targetAreaRef}
          style={{
            height: freeView ? undefined : '100%'
          }}
          {...bindTarget()}
        >
          <Box
            header={
              <Title size={'2xl'} headingLevel={'h2'}>
                Target
              </Title>
            }
            footer={<p>{targets.length} fields</p>}
            ref={targetFieldsRef}
            rightAlign={true}
          >
            <FieldGroupList>
              {targets.map(t => {
                return (
                  <FieldGroup
                    isVisible={true}
                    group={t}
                    key={t.id}
                    boxRef={targetFieldsRef.current}
                    type={'target'}
                    rightAlign={true}
                  />
                );
              })}
            </FieldGroupList>
          </Box>
        </div>
      </CanvasObject>

      <Links mappings={mappings} />
    </CanvasLinksProvider>
  );
};
