import { Title } from '@patternfly/react-core';
import { CanvasLinksProvider, CanvasObject, useCanvas } from '@src/canvas';
import { Mapping, MappingGroup } from '@src/models';
import { Box } from '@src/views/sourcetargetmapper/Box';
import { FieldGroupList } from '@src/views/sourcetargetmapper/FieldGroupList';
import { FieldGroup } from '@src/views/sourcetargetmapper/FieldGroup';
import { Links } from '@src/views/sourcetargetmapper/Links';
import React, { FunctionComponent, useRef } from 'react';

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
  const { width, height, zoom } = useCanvas();

  const sourceRef = useRef<HTMLDivElement | null>(null);
  const targetRef = useRef<HTMLDivElement | null>(null);

  const gutter = 20;
  const boxWidth = Math.max(200, width / 2 - gutter * 3);
  const boxHeight = Math.max(300, height - gutter * 3);
  const startY = gutter;
  const sourceStartX = gutter;
  const targetStartX = Math.max(width / 2, boxWidth + gutter) + gutter * 2;

  return (
    <CanvasLinksProvider>
      <Links mappings={mappings} />

      <CanvasObject
        width={boxWidth}
        height={boxHeight}
        x={sourceStartX}
        y={startY}
      >
        <Box
          header={
            <Title size={'2xl'} headingLevel={'h2'}>
              Source
            </Title>
          }
          footer={<p>{sources.length} fields</p>}
          ref={sourceRef}
          style={{
            fontSize: `${zoom}rem`,
          }}
        >
          <FieldGroupList>
            {sources.map(s => {
              return (
                <FieldGroup
                  isVisible={true}
                  group={s}
                  key={s.id}
                  boxRef={sourceRef.current}
                  type={'source'}
                />
              );
            })}
          </FieldGroupList>
        </Box>
      </CanvasObject>

      <CanvasObject
        width={boxWidth}
        height={boxHeight}
        x={targetStartX}
        y={startY}
      >
        <Box
          header={
            <Title size={'2xl'} headingLevel={'h2'}>
              Target
            </Title>
          }
          footer={<p>{targets.length} fields</p>}
          ref={targetRef}
          style={{
            fontSize: `${zoom}rem`,
          }}
        >
          <FieldGroupList>
            {targets.map(t => {
              return (
                <FieldGroup
                  isVisible={true}
                  group={t}
                  key={t.id}
                  boxRef={targetRef.current}
                  type={'target'}
                />
              );
            })}
          </FieldGroupList>
        </Box>
      </CanvasObject>
    </CanvasLinksProvider>
  );
};
