import { action } from '@storybook/addon-actions';
import { boolean } from '@storybook/addon-knobs';
import React, { createElement, FunctionComponent, useState } from 'react';
import { CanvasObject } from '../../../src/Canvas';
import {
  BoxProvider,
  CanvasView,
  CanvasViewProvider,
  FieldsBox,
} from '../../../src/CanvasView';
import { MappingElement } from '../../../src/CanvasView/components';
import { mappings } from '../../sampleData';

export default {
  title: 'CanvasView/MappingElement',
};

const Wrapper: FunctionComponent = ({ children }) => (
  <CanvasViewProvider>
    <CanvasView onSelection={action('onSelection')}>
      <BoxProvider getScrollableAreaRef={() => null}>
        <CanvasObject height={300} width={200} id={'id'} x={10} y={10}>
          <FieldsBox
            id={'sample'}
            initialWidth={300}
            initialHeight={400}
            position={{ x: 10, y: 10 }}
            header={'Mappings'}
            visible={false}
          >
            {children}
          </FieldsBox>
        </CanvasObject>
      </BoxProvider>
    </CanvasView>
  </CanvasViewProvider>
);

export const interactive = () => {
  return createElement(() => {
    const [selectedMapping, setSelectedMapping] = useState<
      string | undefined
    >();
    const handleSelectMapping = (id: string) => {
      setSelectedMapping(id);
    };
    const handleDeselectMapping = () => {
      setSelectedMapping(undefined);
    };
    return (
      <Wrapper>
        <MappingElement
          boxRef={null}
          node={mappings[0]}
          selectedMapping={selectedMapping}
          selectMapping={handleSelectMapping}
          deselectMapping={handleDeselectMapping}
          editMapping={action('editMapping')}
          canDrop={boolean('Can drop', false)}
          isOver={boolean('Is over', false)}
          closeMappingDetails={action('closeMappingDetails')}
        />
      </Wrapper>
    );
  });
};

export const canDrop = () => (
  <Wrapper>
    <MappingElement
      boxRef={null}
      node={mappings[0]}
      selectedMapping={undefined}
      selectMapping={() => void 0}
      deselectMapping={() => void 0}
      editMapping={() => void 0}
      isOver={false}
      canDrop={true}
      closeMappingDetails={() => void 0}
    />
  </Wrapper>
);

export const isHoverCanDrop = () => (
  <Wrapper>
    <MappingElement
      boxRef={null}
      node={mappings[0]}
      selectedMapping={undefined}
      selectMapping={() => void 0}
      deselectMapping={() => void 0}
      editMapping={() => void 0}
      isOver={true}
      canDrop={true}
      closeMappingDetails={() => void 0}
    />
  </Wrapper>
);
