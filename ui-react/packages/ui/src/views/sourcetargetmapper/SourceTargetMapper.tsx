import { Title } from '@patternfly/react-core';
import { CanvasLinksProvider, CanvasObject, useCanvas } from '@src/canvas';
import { Coords, Mapping, MappingGroup } from '@src/models';
import { useDimensions } from '@src/useDimensions';
import { Box } from '@src/views/sourcetargetmapper/Box';
import { FieldGroupList } from '@src/views/sourcetargetmapper/FieldGroupList';
import { FieldGroup } from '@src/views/sourcetargetmapper/FieldGroup';
import { Links } from '@src/views/sourcetargetmapper/Links';
import React, { FunctionComponent, useRef, useState } from 'react';
import { useDrag } from 'react-use-gesture';
import clamp from 'lodash.clamp'

export interface IMappingCanvasProps {
  sources: MappingGroup[];
  targets: MappingGroup[];
  mappings: Mapping[];
}

export const SourceTargetMapper: FunctionComponent<IMappingCanvasProps> = ({
  sources,
  targets,
  mappings,
}) => {
  const { width } = useCanvas();

  const [sourceAreaRef, sourceAreaDimensions] = useDimensions();
  const [targetAreaRef, targetAreaDimensions] = useDimensions();
  const sourceFieldsRef = useRef<HTMLDivElement | null>(null);
  const targetFieldsRef = useRef<HTMLDivElement | null>(null);

  const gutter = 20;
  const boxWidth = Math.max(200, width / 2 - gutter * 3);
  const [sourceCoords, setSourceCoords] = useState<Coords>({ x: gutter, y: gutter });
  const [targetCoords, setTargetCoords] = useState<Coords>({ x: Math.max(width / 2, boxWidth + gutter) + gutter * 2, y: gutter });

  const bindSource = useDrag(
    ({ event, movement: [x, y], memo = [sourceCoords.x, sourceCoords.y] }) => {
      event!.stopPropagation();
      setSourceCoords({
        x: clamp(x + memo[0], -Infinity, targetCoords.x - boxWidth - gutter) ,
        y: y + memo[1]
      });
      return memo;
    }
  );

  const bindTarget = useDrag(
    ({ event, movement: [x, y], memo = [targetCoords.x, targetCoords.y] }) => {
      event!.stopPropagation();
      setTargetCoords({
        x: clamp(x + memo[0], sourceCoords.x + boxWidth + gutter, +Infinity),
        y: y + memo[1]
      });
      return memo;
    }
  );

  return (
    <CanvasLinksProvider>
      <CanvasObject
        width={boxWidth}
        height={sourceAreaDimensions.height}
        x={sourceCoords.x}
        y={sourceCoords.y}
      >
        <div
          ref={sourceAreaRef}
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
        height={targetAreaDimensions.height}
        x={targetCoords.x}
        y={targetCoords.y}
      >
        <div
          ref={targetAreaRef}
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
