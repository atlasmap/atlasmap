import {
  BaseSizes,
  Button,
  Card,
  CardActions,
  CardBody, CardFooter,
  CardHead,
  CardHeader,
  Title,
  Tooltip,
  TooltipPosition,
} from '@patternfly/react-core';
import { css, StyleSheet } from '@patternfly/react-styles';
import {
  EditIcon,
  OutlinedQuestionCircleIcon,
  CaretRightIcon,
  CaretDownIcon,
} from '@patternfly/react-icons';
import React, {
  FunctionComponent,
  useCallback,
  useRef,
  MouseEvent,
  useEffect,
} from 'react';
import { useDrop } from 'react-dnd';
import {
  useBoundingCanvasRect,
  useMappingNode,
} from '../../../canvas';
import { ElementId, DocumentType, IMappings } from '../models';
import { IFieldElementDragSource } from './FieldElement';

const styles = StyleSheet.create({
  element: {
    transition: 'all 0.35s',
    marginBottom: '1rem',
    border: '2px solid transparent',
  },
  selected: {
    borderColor: 'var(--pf-global--primary-color--100) !important',
  },
  dropTarget: {
    borderColor: 'var(--pf-global--primary-color--100) !important',
  },
  canDrop: {
    borderColor: 'var(--pf-global--success-color--100) !important',
  },
  head: {
    padding: '0.5rem !important',
  },
  footer: {
    borderTop: '1px solid #eee',
    paddingTop: '0.5rem',
    paddingBottom: '0.5rem',
    direction: 'ltr',
  },
});

export interface IMappingElementProps {
  node: IMappings;
  boxRef: HTMLElement | null;
  selectedMapping: string | undefined;
  selectMapping: (id: string) => void;
  deselectMapping: () => void;
  editMapping: () => void;
  addToMapping: (
    elementId: ElementId,
    elementType: DocumentType,
    mappingId: string
  ) => void;
  mappingType: string;
}

export const MappingElement: FunctionComponent<IMappingElementProps> = ({
  node,
  boxRef,
  selectedMapping,
  selectMapping,
  deselectMapping,
  editMapping,
  addToMapping,
  mappingType
}) => {
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

  const [{ isOver, canDrop }, dropRef] = useDrop<
    IFieldElementDragSource,
    void,
    { isOver: boolean; canDrop: boolean }
  >({
    accept: ['source', 'target'],
    drop: item => addToMapping(item.id, item.type, node.id),
    collect: monitor => ({
      isOver: monitor.isOver(),
      canDrop: monitor.canDrop(),
    }),
    canDrop: (props, monitor) => {
      const type = monitor.getItemType();
      if (node.sourceFields.length === 1 && node.targetFields.length === 1) {
        if (
          type === 'source' &&
          !node.sourceFields.find(f => f.id === props.id)
        ) {
          return true;
        } else if (!node.targetFields.find(f => f.id === props.id)) {
          return true;
        }
      } else if (
        type === 'source' &&
        node.targetFields.length === 1 &&
        !node.sourceFields.find(f => f.id === props.id)
      ) {
        return true;
      } else if (
        type === 'target' &&
        node.sourceFields.length === 1 &&
        !node.targetFields.find(f => f.id === props.id)
      ) {
        return true;
      }
      return false;
    },
    hover: (_, monitor) => {
      const type = monitor.getItemType();
      const canDrop = monitor.canDrop();
      if (canDrop) {
        setLineNode(
          'dragtarget',
          type === 'source' ? getFromSourceCoords : getToTargetCoords
        );
      }
    },
  });

  setLineNode(`to-${node.id}`, getFromSourceCoords);
  setLineNode(`from-${node.id}`, getToTargetCoords);

  const isSelected = node.id === selectedMapping;

  useEffect(() => {
    if (!isOver) {
      unsetLineNode('dragtarget');
    }
  }, [isOver, isSelected]);


  const handleSelect = useCallback(() => {
    if (isSelected) {
      deselectMapping();
    } else {
      selectMapping(node.id);
    }
  }, [isSelected, node, deselectMapping, selectMapping]);
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
    <div ref={handleRef}>
      <Card
        className={css(
          styles.element,
          isSelected && styles.selected,
          canDrop && styles.canDrop,
          canDrop && isOver && styles.dropTarget
        )}
        onClick={handleSelect}
        isCompact={true}
      >
        <CardHead className={css(styles.head)}>
          <CardActions>
            {isSelected && (
              <Button variant={'plain'} onClick={handleEdit}>
                <EditIcon />
              </Button>
            )}
          </CardActions>
          <CardHeader>
            <Button variant={'link'}>
              {isSelected ? <CaretDownIcon /> : <CaretRightIcon />}{' '}
              {`${mappingTypeLeft} to ${mappingTypeRight}`}
            </Button>
          </CardHeader>
        </CardHead>
        {isSelected && <CardBody>
          <Title size={BaseSizes.md}>Sources</Title>
          {node.sourceFields.map((s, idx) => (
            <p key={idx}>
              {s.name}{' '}
              <Tooltip position={TooltipPosition.top} content={s.tip}>
                <OutlinedQuestionCircleIcon />
              </Tooltip>
            </p>
          ))}
          <br />
          <Title size={BaseSizes.md}>Targets</Title>
          {node.targetFields.map((s, idx) => (
            <p key={idx}>
              {s.name}{' '}
              <Tooltip position={TooltipPosition.top} content={s.tip}>
                <OutlinedQuestionCircleIcon />
              </Tooltip>
            </p>
          ))}
        </CardBody>}
        <CardFooter className={css(styles.footer)}>
          Mapping type: {mappingType}
        </CardFooter>
      </Card>
    </div>
  );
};
