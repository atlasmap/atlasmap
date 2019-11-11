import { Title } from '@patternfly/react-core';
import { Box } from '@src/Box';
import { useCanvasInfo } from '@src/CanvasContext';
import { CanvasLink } from '@src/CanvasLink';
import { CanvasObject } from '@src/CanvasObject';
import { FieldGroupList } from '@src/FieldGroupList';
import { FieldGroup } from '@src/FieldsGroup';
import { FieldElement, FieldsGroup, Mapping } from '@src/models';
import { useDimensions } from '@src/useDimensions';
import { useMappingLines } from '@src/useMappingLines';
import React, { FunctionComponent } from 'react';

export interface IMappingCanvasProps {
  sources: FieldsGroup[];
  targets: FieldsGroup[];
  mappings: Mapping[];
}

export const SourceTargetMapper: FunctionComponent<
  IMappingCanvasProps
> = ({ sources, targets, mappings }) => {
  const { width, height, zoom } = useCanvasInfo();

  const [sourceRef, sourceDimensions] = useDimensions();
  const [targetRef, targetDimensions] = useDimensions();

  const gutter = 20;
  const boxWidth = Math.max(200, width / 2 - gutter * 3);
  const boxHeight = Math.max(300, height - gutter * 3);
  const startY = gutter;
  const sourceStartX = gutter;
  const targetStartX = Math.max(width / 2, boxWidth + gutter) + gutter * 2;

  const { lines, calcLines, addFieldRef, addFieldsGroupRef } = useMappingLines({
    sourcesContainerRect: sourceDimensions,
    targetsContainerRect: targetDimensions,
    mappings
  });

  const makeFieldGroup = (type: string, { id, title, fields }: FieldsGroup) => (
    <FieldGroup
      key={id}
      id={id}
      title={title}
      onLayout={calcLines}
      ref={el => el && addFieldsGroupRef(el, `${type}-${id}`)}
    >
      {fields.map((f, fdx) => (
        <div
          style={{
            padding: `calc(0.3rem * ${zoom}) 0`,
            borderTop: '1px solid #eee',
            borderBottom: '1px solid #eee',
            marginTop: '-1px',
            fontSize: `${zoom}rem`,
          }}
          key={f.id || fdx}
          ref={el => el && f.id && addFieldRef(el, f.id, `${type}-${id}`)}
        >
          {(f as FieldElement).element ||
            makeFieldGroup(type, f as FieldsGroup)}
        </div>
      ))}
    </FieldGroup>
  );

  const makeSourceFieldGroup = (f: FieldsGroup) => makeFieldGroup('source', f);
  const makeTargetFieldGroup = (f: FieldsGroup) => makeFieldGroup('target', f);

  return (
    <>
      {lines.map(({ start, end, color }, idx) => (
        <CanvasLink key={idx} start={start} end={end} color={color} />
      ))}

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
          onLayout={calcLines}
        >
          <FieldGroupList>{sources.map(makeSourceFieldGroup)}</FieldGroupList>
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
          onLayout={calcLines}
        >
          <FieldGroupList>{targets.map(makeTargetFieldGroup)}</FieldGroupList>
        </Box>
      </CanvasObject>
    </>
  );
};
