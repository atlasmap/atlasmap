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
  MouseEvent,
  useEffect,
} from 'react';
import { useMappingNode } from '../../../canvas';

import { IMappings } from '../models';
import { useLinkable } from './useLinkable';

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
  selectedMapping: string | undefined;
  selectMapping: (id: string) => void;
  deselectMapping: () => void;
  editMapping: () => void;
  mappingType: string;
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
  mappingType,
  canDrop,
  isOver,
  boxRef
}) => {
  const { ref, getLeftSideCoords, getRightSideCoords } = useLinkable({ getBoxRef: () => boxRef });
  const { setLineNode, unsetLineNode } = useMappingNode();

  const isSelected = node.id === selectedMapping;

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


  useEffect(() => {
    setLineNode(`to-${node.id}`, getLeftSideCoords);
    setLineNode(`from-${node.id}`, getRightSideCoords);
    return () => {
      unsetLineNode(`to-${node.id}`);
      unsetLineNode(`from-${node.id}`);
    };
  }, [getLeftSideCoords, getRightSideCoords, node.id, setLineNode, unsetLineNode]);

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
