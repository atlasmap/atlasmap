import {
  AccordionContent,
  AccordionItem,
  AccordionToggle,
} from '@patternfly/react-core';
import { FolderOpenIcon, FolderCloseIcon } from '@patternfly/react-icons';
import { css, StyleSheet } from '@patternfly/react-styles';
import React, {
  ReactChild,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from 'react';
import { useLinkNode } from '../../Canvas';
import { IFieldsGroup, IFieldsNode } from '../models';
import { FieldElement, IFieldElementProps } from './FieldElement';
import { useLinkable } from './useLinkable';
import { useDocumentContext } from "./DocumentProvider";

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
    display: 'flex',
    alignItems: 'center',
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
    direction: 'ltr',
  },
});

export interface IFieldGroupProps
  extends Pick<IFieldElementProps, 'renderNode'> {
  isVisible: boolean;
  group: IFieldsGroup;
  lineConnectionSide: 'left' | 'right';
  getParentRef?: () => HTMLElement | null;
  rightAlign?: boolean;
  level?: number;
  parentExpanded: boolean;
  expandParent?: (expanded: boolean) => void;
  renderGroup: (node: IFieldsGroup) => ReactChild;
}
export function FieldGroup({
  isVisible,
  group,
  lineConnectionSide,
  getParentRef,
  rightAlign = false,
  level = 0,
  parentExpanded,
  expandParent,
  renderNode,
  renderGroup,
}: IFieldGroupProps) {
  const { setLineNode } = useLinkNode();
  const { getScrollableAreaRef } = useDocumentContext();
  const { ref, getLeftSideCoords, getRightSideCoords } = useLinkable({
    getScrollableAreaRef,
    getParentRef,
  });
  const [isExpandedByUser, setIsExpandedByUser] = useState(level === 0);
  const [isExpandedByField, setIsExpandedByField] = useState(false);
  const toggleExpand = useCallback(
    () => setIsExpandedByUser(!isExpandedByUser),
    [isExpandedByUser, setIsExpandedByUser]
  );

  const setExpanded = useCallback(
    (expanded: boolean) => {
      expandParent && expandParent(expanded);
      setIsExpandedByField(expanded);
    },
    [expandParent]
  );

  const isExpanded = isExpandedByField || isExpandedByUser;

  const getCoords =
    lineConnectionSide === 'right' ? getRightSideCoords : getLeftSideCoords;

  const handleChildLines = useCallback(() => {
    if (!isExpanded) {
      const traverseChildren = (f: IFieldsGroup | IFieldsNode) => {
        if ((f as IFieldsGroup).fields) {
          (f as IFieldsGroup).fields.forEach(traverseChildren);
        } else {
          setLineNode(f.id, getCoords);
        }
      };
      group.fields.forEach(traverseChildren);
    }
  }, [getCoords, group.fields, isExpanded, setLineNode]);

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
              isVisible && isExpanded
                ? ref.current
                : isVisible || !getParentRef
                ? ref.current
                : getParentRef()
            }
            node={f as IFieldsNode}
            rightAlign={rightAlign}
            renderNode={renderNode}
            expandParent={setExpanded}
          />
        ) : (
          <FieldGroup
            isVisible={isVisible && isExpanded}
            lineConnectionSide={lineConnectionSide}
            getParentRef={() =>
              isVisible || !getParentRef ? ref.current : getParentRef()
            }
            group={f as IFieldsGroup}
            rightAlign={rightAlign}
            key={f.id}
            level={level + 1}
            parentExpanded={parentExpanded}
            renderGroup={renderGroup}
            renderNode={renderNode}
            expandParent={setExpanded}
          />
        )
      ),
    [
      group.fields,
      lineConnectionSide,
      rightAlign,
      renderNode,
      renderGroup,
      isVisible,
      isExpanded,
      level,
      parentExpanded,
      ref,
      getParentRef,
      setExpanded,
    ]
  );

  return level === 0 ? (
    <div ref={ref} className={css(styles.fieldsWrapper)}>
      {content}
    </div>
  ) : (
    <div ref={ref}>
      <AccordionItem>
        <AccordionToggle
          onClick={toggleExpand}
          isExpanded={isExpanded}
          id={`source-field-group-${group.id}-toggle`}
          className={css(styles.button, rightAlign && styles.buttonRightAlign)}
        >
          <span
            className={css(
              styles.buttonContent,
              rightAlign && styles.buttonContentRightAligned
            )}
          >
            {isExpanded ? <FolderOpenIcon /> : <FolderCloseIcon />}
            <span>&nbsp;</span>
            {renderGroup(group)}
          </span>
        </AccordionToggle>
        <AccordionContent
          id={`source-field-group-${group.id}-content`}
          isHidden={!isExpanded}
          className={css(styles.content)}
        >
          {content}
        </AccordionContent>
      </AccordionItem>
    </div>
  );
}
