import { Button, Level, LevelItem, } from '@patternfly/react-core';
import { css, StyleSheet } from '@patternfly/react-styles';
import { EditIcon } from '@patternfly/react-icons';
import React, {
  FunctionComponent,
  useCallback,
  useRef,
  MouseEvent
} from 'react';
import { useBoundingCanvasRect, useMappingNode } from '../../canvas';
import { useMappingDetails } from '../../mapper/MapperContext';
import { ElementType, IFieldsNode } from '../../models';
import { useSourceTargetMapper } from './SourceTargetMapperContext';

const styles = StyleSheet.create({
  element: {
    boxShadow: 'var(--pf-global--BoxShadow--md)',
    width: '100%',
    background: '#fff',
    borderRadius: '5px',
    padding: '0.5rem 0 0.5rem 1.5rem',
    marginBottom: '1rem',
    border: '3px solid #fff',
    cursor: 'pointer',
    'font-weight': 'var(--pf-global--FontWeight--bold)',
    transition: 'all 0.2s',
  },
  selected: {
    fontSize: '1.5rem',
    borderColor: 'var(--pf-global--primary-color--100)',
  },
});

export interface IMappingElementProps {
  node: IFieldsNode;
  type: ElementType;
  boxRef: HTMLElement | null;
}

export const MappingElement: FunctionComponent<IMappingElementProps> = ({
  node,
  type,
  boxRef,
}) => {
  const {
    focusMapping,
    blurMapping,
    focusedMapping,
  } = useSourceTargetMapper();
  const showMappingDetails = useMappingDetails();
  const ref = useRef<HTMLDivElement | null>(null);
  const getBoundingCanvasRect = useBoundingCanvasRect();
  const setLineNode = useMappingNode();
  const getCoords = useCallback(() => {
    if (ref.current && boxRef) {
      let boxRect = getBoundingCanvasRect(boxRef);
      let dimensions = getBoundingCanvasRect(ref.current);
      return {
        left: dimensions.left,
        right: dimensions.right,
        y: Math.min(
          Math.max(dimensions.top + dimensions.height / 2, boxRect.top),
          boxRect.height + boxRect.top
        ),
      };
    } else {
      return { left: 0, right: 0, y: 0 };
    }
  }, [ref, type, boxRef, getBoundingCanvasRect]);
  setLineNode(`to-${node.id}`, () => {
    const { left, y } = getCoords();
    return {
      x: left,
      y,
    };
  });
  setLineNode(`from-${node.id}`, () => {
    const { right, y } = getCoords();
    return {
      x: right,
      y,
    };
  });
  const handleSelect = useCallback(
    () => {
      if (focusedMapping === node.id) {
        blurMapping()
      } else {
        focusMapping(node.id);
      }
    },
    [focusMapping, node]
  );
  const handleEdit = useCallback(
    (e: MouseEvent) => {
      e.stopPropagation();
      showMappingDetails(node.id);
    },
    [showMappingDetails, node]
  );
  const isSelected = node.id === focusedMapping;
  return (
    <div
      ref={ref}
      className={css(
        styles.element,
        isSelected && styles.selected
      )}
      onClick={handleSelect}
    >
      <Level>
        <LevelItem style={{flex: '1'}}>
          {node.element}
        </LevelItem>
        {isSelected && <LevelItem>
          <Button variant={'plain'} onClick={handleEdit}>
            <EditIcon />
          </Button>
        </LevelItem>}
      </Level>
    </div>
  );
};
