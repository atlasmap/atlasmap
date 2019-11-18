import {
  BaseSizes,
  Button,
  Level,
  LevelItem,
  Title,
  Tooltip,
  TooltipPosition,
} from '@patternfly/react-core';
import { css, StyleSheet } from '@patternfly/react-styles';
import { EditIcon, OutlinedQuestionCircleIcon, CaretRightIcon, CaretDownIcon } from '@patternfly/react-icons';
import React, {
  FunctionComponent,
  useCallback,
  useRef,
  MouseEvent,
  useEffect,
} from 'react';
import { useDrop } from 'react-dnd';
import { useBoundingCanvasRect, useCanvas, useMappingNode } from '../../canvas';
import { ElementId, ElementType, IMappings } from '../../models';
import { IFieldElementDragSource } from './FieldElement';

const styles = StyleSheet.create({
  element: {
    boxShadow: 'var(--pf-global--BoxShadow--md)',
    width: '100%',
    background: '#fff',
    borderRadius: '5px',
    padding: '0.5rem 0 0.5rem 0.5rem',
    marginBottom: '1rem',
    border: '3px solid #fff',
    cursor: 'pointer',
    'font-weight': 'var(--pf-global--FontWeight--bold)',
    transition: 'all 0.35s',
  },
  selected: {
    fontSize: '1.5rem',
    borderColor: 'var(--pf-global--primary-color--100)',
  },
  dropTarget: {
    backgroundColor: 'var(--pf-global--success-color--100)',
  },
  canDrop: {
    borderColor: 'var(--pf-global--success-color--100)'
  }
});

export interface IMappingElementProps {
  node: IMappings;
  boxRef: HTMLElement | null;
  selectedMapping: string | undefined;
  selectMapping: (id: string) => void;
  deselectMapping: () => void;
  editMapping: () => void;
  addToMapping: (elementId: ElementId, elementType: ElementType, mappingId: string) => void;
}

export const MappingElement: FunctionComponent<IMappingElementProps> = ({
  node,
  boxRef,
  selectedMapping,
  selectMapping,
  deselectMapping,
  editMapping,
  addToMapping
}) => {
  const { redraw } = useCanvas();
  const ref = useRef<HTMLDivElement | null>(null);

  const getBoundingCanvasRect = useBoundingCanvasRect();
  const { setLineNode, unsetLineNode } = useMappingNode();

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
  }, [ref, boxRef, getBoundingCanvasRect]);

  const getFromSourceCoords = () => {
    const { left, y } = getCoords();
    return {
      x: left,
      y,
    };
  };
  const getToTargetCoords = () => {
    const { right, y } = getCoords();
    return {
      x: right,
      y,
    };
  };

  const [{ isOver, canDrop }, dropRef] = useDrop<IFieldElementDragSource, void, { isOver: boolean; canDrop: boolean; }>({
    accept: ['source', 'target'],
    drop: (item) => addToMapping(item.id, item.type, node.id),
    collect: monitor => ({
      isOver: monitor.isOver(),
      canDrop: monitor.canDrop(),
    }),
    canDrop: (props, monitor) => {
      const type = monitor.getItemType();
      if (node.sourceFields.length === 1 && node.targetFields.length === 1) {
        if (type === 'source' && !node.sourceFields.find(f => f.id === props.id)) {
          return true;
        } else if (!node.targetFields.find(f => f.id === props.id)) {
          return true;
        }
      } else if (type === 'source' && node.targetFields.length === 1 && !node.sourceFields.find(f => f.id === props.id)) {
        return true;
      } else if (type === 'target' && node.sourceFields.length === 1 && !node.targetFields.find(f => f.id === props.id)) {
        return true;
      }
      return false;
    },
    hover: (_, monitor) => {
      const type = monitor.getItemType();
      const canDrop = monitor.canDrop();
      if (canDrop) {
        setLineNode('dragtarget', type === 'source' ? getFromSourceCoords : getToTargetCoords);
      }
    }
  });

  setLineNode(`to-${node.id}`, getFromSourceCoords);
  setLineNode(`from-${node.id}`, getToTargetCoords);

  useEffect(() => {
    if (!isOver) {
      unsetLineNode('dragtarget');
    }
    redraw();
  }, [isOver]);

  const isSelected = node.id === selectedMapping;

  const handleSelect = useCallback(
    () => {
      if (isSelected) {
        deselectMapping()
      } else {
        selectMapping(node.id);
      }
    },
    [isSelected, node, deselectMapping, selectMapping]
  );
  const handleEdit = useCallback(
    (e: MouseEvent) => {
      e.stopPropagation();
      editMapping();
    },
    [editMapping]
  );
  const mappingTypeLeft = node.sourceFields.length <= 1 ? 'One' : 'Many';
  const mappingTypeRight = node.targetFields.length <= 1 ? 'One' : 'Many';

  const handleRef = (el: HTMLDivElement) => {
    ref.current = el;
    dropRef(el);
  };

  return (
    <div
      ref={handleRef}
      className={css(
        styles.element,
        isSelected && styles.selected,
        isOver && canDrop && styles.dropTarget,
        canDrop && styles.canDrop
      )}
      onClick={handleSelect}
    >
      <Level>
        <LevelItem style={{flex: '1'}}>
          <Title size={BaseSizes.lg}>
            {isSelected ? <CaretDownIcon />: <CaretRightIcon/>}
            {' '}
            {`${mappingTypeLeft} to ${mappingTypeRight} (Split)`}
          </Title>
          {isSelected && <div>
            <br />
            <Title size={BaseSizes.md}>Sources</Title>
            {node.sourceFields.map((s, idx) =>
              <p key={idx}>
                {s.name}{' '}
                <Tooltip
                  position={TooltipPosition.top}
                  content={s.tip}
                >
                  <OutlinedQuestionCircleIcon />
                </Tooltip>
              </p>
            )}
            <br />
            <Title size={BaseSizes.md}>Targets</Title>
            {node.targetFields.map((s, idx) =>
              <p key={idx}>
                {s.name}{' '}
                <Tooltip
                  position={TooltipPosition.top}
                  content={s.tip}
                >
                  <OutlinedQuestionCircleIcon />
                </Tooltip>
              </p>
            )}
          </div>}
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
