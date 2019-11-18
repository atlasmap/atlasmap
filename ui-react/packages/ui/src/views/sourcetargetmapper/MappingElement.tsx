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
  MouseEvent
} from 'react';
import { useBoundingCanvasRect, useMappingNode } from '../../canvas';
import { ElementType, IMappings } from '../../models';

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
});

export interface IMappingElementProps {
  node: IMappings;
  type: ElementType;
  boxRef: HTMLElement | null;
  selectedMapping: string | undefined;
  selectMapping: (id: string) => void;
  deselectMapping: () => void;
  editMapping: () => void;
}

export const MappingElement: FunctionComponent<IMappingElementProps> = ({
  node,
  type,
  boxRef,
  selectedMapping,
  selectMapping,
  deselectMapping,
  editMapping,
}) => {
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
