import {
  Button,
  Card,
  CardActions,
  CardFooter,
  CardHead,
  CardHeader,
  Level,
  LevelItem,
} from '@patternfly/react-core';
import { css, StyleSheet } from '@patternfly/react-styles';
import {
  ArrowRightIcon,
  EditIcon,
} from '@patternfly/react-icons';
import React, {
  FunctionComponent,
  useCallback,
  MouseEvent,
  useEffect,
  useRef,
} from 'react';
import { useLinkNode } from '../../../canvas';
import { useCanvasViewFieldsContext } from '../CanvasViewFieldsProvider';

import { IMappings } from '../models';
import { useLinkable } from './useLinkable';

const styles = StyleSheet.create({
  element: {
    transition: 'all 0.35s',
    marginBottom: '1rem',
    border: '2px solid transparent',
  },
  selected: {
    borderColor: 'var(--pf-global--active-color--400) !important',
  },
  dropTarget: {
    borderColor: 'var(--pf-global--active-color--400) !important',
  },
  canDrop: {
    borderColor: 'var(--pf-global--success-color--100) !important',
  },
  head: {
    padding: '0.5rem !important',
    height: '52px'
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
  selectedMapping: string | undefined;
  selectMapping: (id: string) => void;
  deselectMapping: () => void;
  editMapping: () => void;
  canDrop: boolean;
  isOver: boolean;
  boxRef: HTMLElement | null;
}

export const MappingElement: FunctionComponent<IMappingElementProps> = ({
  node,
  selectedMapping,
  selectMapping,
  deselectMapping,
  editMapping,
  canDrop,
  isOver,
  boxRef,
}) => {
  const { ref, getLeftSideCoords, getRightSideCoords } = useLinkable({
    getBoxRef: () => boxRef,
  });
  const { setLineNode, unsetLineNode } = useLinkNode();
  const { requireVisible } = useCanvasViewFieldsContext();
  const wasPreviouslySelected = useRef<boolean>(false);

  const isSelected = node.id === selectedMapping;

  const handleSelect = useCallback(() => {
    wasPreviouslySelected.current = true;
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

  useEffect(() => {
    if (wasPreviouslySelected.current && !isSelected) {
      node.sourceFields.forEach(f => requireVisible(f.id, false));
      node.targetFields.forEach(f => requireVisible(f.id, false));
      wasPreviouslySelected.current = false;
    } else if (isSelected) {
      node.sourceFields.forEach(f => requireVisible(f.id, true));
      node.targetFields.forEach(f => requireVisible(f.id, true));
    }
  }, [isSelected, node.sourceFields, node.targetFields, requireVisible]);

  useEffect(() => {
    setLineNode(`to-${node.id}`, getLeftSideCoords);
    setLineNode(`from-${node.id}`, getRightSideCoords);
    return () => {
      unsetLineNode(`to-${node.id}`);
      unsetLineNode(`from-${node.id}`);
    };
  }, [
    getLeftSideCoords,
    getRightSideCoords,
    node.id,
    setLineNode,
    unsetLineNode,
  ]);

  return (
    <div ref={ref}>
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
              <Button variant={'control'} onClick={handleEdit}>
                <EditIcon />
              </Button>
            )}
          </CardActions>
          <CardHeader>
            {node.name}
          </CardHeader>
        </CardHead>
        <CardFooter className={css(styles.footer)}>
          <Level>
            <LevelItem>{node.sourceFields.length}</LevelItem>
            <LevelItem><ArrowRightIcon /></LevelItem>
            <LevelItem>{node.targetFields.length}</LevelItem>
          </Level>
        </CardFooter>
      </Card>
    </div>
  );
};
