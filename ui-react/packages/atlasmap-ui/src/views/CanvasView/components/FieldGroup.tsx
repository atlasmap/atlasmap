import {
  AccordionContent,
  AccordionItem,
  AccordionToggle,
} from '@patternfly/react-core';
import { FolderOpenIcon, FolderCloseIcon } from '@patternfly/react-icons';
import { css, StyleSheet } from '@patternfly/react-styles';
import React, {
  useCallback,
  useEffect,
  useMemo,
  useState,
} from 'react';
import { useMappingNode } from '../../../canvas';
import { IFieldsGroup, IFieldsNode } from '../models';
import { FieldElement, IFieldElementProps } from './FieldElement';
import { useLinkable } from './useLinkable';

const styles = StyleSheet.create({
  button: {
    paddingRight: '0.5rem !important',
    direction: 'ltr',
  },
  buttonRightAlign: {
    direction: 'rtl',
    '& > span': {
      direction: 'ltr',
      flex: 1,
      order: 1,
    },
  },
  buttonContent: {
    display: 'inline-block',
    width: '100%',
  },
  buttonContentRightAligned: {
    transform: 'scaleX(-1)',
    textAlign: 'left',
  },
  content: {
    fontSize: 'inherit !important',
    '& > div': {
      padding:
        'var(--pf-c-accordion__expanded-content-body--PaddingTop) 0.5rem var(--pf-c-accordion__expanded-content-body--PaddingBottom) 0.5rem !important',
    },
  },
  fieldsWrapper: {
    direction: 'ltr'
  },
});

export interface IFieldGroupProps extends Pick<IFieldElementProps, 'renderNode'> {
  isVisible: boolean;
  group: IFieldsGroup;
  lineConnectionSide: 'left' | 'right';
  getParentRef?: () => HTMLElement | null;
  getBoxRef: () => HTMLElement | null;
  rightAlign?: boolean;
  level?: number;
  parentExpanded: boolean;
}
export function FieldGroup({
  isVisible,
  group,
  lineConnectionSide,
  getParentRef,
  getBoxRef,
  rightAlign = false,
  level = 0,
  parentExpanded,
  renderNode,
}: IFieldGroupProps) {
  const { setLineNode } = useMappingNode();
  const { ref, getLeftSideCoords, getRightSideCoords } = useLinkable({ getBoxRef, getParentRef });
  const [isExpandedByUser, setIsExpandedByUser] = useState(level === 0);
  const toggleExpand = useCallback(() => setIsExpandedByUser(!isExpandedByUser), [
    isExpandedByUser,
    setIsExpandedByUser,
  ]);

  const getCoords = lineConnectionSide === 'right' ? getRightSideCoords : getLeftSideCoords;

  const handleChildLines = useCallback(() => {
    if (!isExpandedByUser) {
      const traverseChildren = (f: IFieldsGroup | IFieldsNode) => {
        if ((f as IFieldsGroup).fields) {
          (f as IFieldsGroup).fields.forEach(traverseChildren);
        } else {
          setLineNode(f.id, getCoords);
        }
      };
      group.fields.forEach(traverseChildren);
    }
  }, [getCoords, group.fields, isExpandedByUser, setLineNode]);

  useEffect(() => {
    handleChildLines();
  }, [handleChildLines, parentExpanded]);

  const content = useMemo(
    () =>
      group.fields.map(f =>
        !(f as IFieldsGroup).fields ? (
          <FieldElement
            key={f.id}
            lineConnectionSide={lineConnectionSide}
            getParentRef={() =>
              isVisible && isExpandedByUser
                ? ref.current
                : isVisible || !getParentRef
                ? ref.current
                : getParentRef()
            }
            getBoxRef={getBoxRef}
            node={f as IFieldsNode}
            rightAlign={rightAlign}
            renderNode={renderNode}
          />
        ) : (
          <FieldGroup
            isVisible={isVisible && isExpandedByUser}
            lineConnectionSide={lineConnectionSide}
            getParentRef={() =>
              isVisible || !getParentRef ? ref.current : getParentRef()
            }
            group={f as IFieldsGroup}
            getBoxRef={getBoxRef}
            rightAlign={rightAlign}
            key={f.id}
            level={level + 1}
            parentExpanded={parentExpanded}
            renderNode={renderNode}
          />
        )
      ),
    [group.fields, lineConnectionSide, getBoxRef, rightAlign, renderNode, isVisible, isExpandedByUser, level, parentExpanded, getParentRef]
  );

  return level === 0 ? (
    <div
      ref={ref}
      className={css(
        styles.fieldsWrapper
      )}
    >
      {content}
    </div>
  ) : (
    <div ref={ref}>
      <AccordionItem>
        <AccordionToggle
          onClick={toggleExpand}
          isExpanded={isExpandedByUser}
          id={`source-field-group-${group.id}-toggle`}
          className={css(styles.button, rightAlign && styles.buttonRightAlign)}
        >
          <span
            className={css(
              styles.buttonContent,
              rightAlign && styles.buttonContentRightAligned
            )}
          >
            {isExpandedByUser ? <FolderOpenIcon /> : <FolderCloseIcon />}
            {' '}
            {renderNode(group as IFieldsGroup, getCoords)}
          </span>
        </AccordionToggle>
        <AccordionContent
          id={`source-field-group-${group.id}-content`}
          isHidden={!isExpandedByUser}
          className={css(
            styles.content
          )}
        >
          {content}
        </AccordionContent>
      </AccordionItem>
    </div>
  );
}
