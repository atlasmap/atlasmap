import {
  AccordionContent,
  AccordionItem,
  AccordionToggle,
  Split,
  SplitItem,
} from '@patternfly/react-core';
import { FolderOpenIcon, FolderCloseIcon } from '@patternfly/react-icons';
import { css, StyleSheet } from '@patternfly/react-styles';
import React, {
  ReactElement,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import { useBoundingCanvasRect, useMappingNode } from '../../../canvas';
import { DocumentType, IFieldsGroup, IFieldsNode } from '../models';
import { FieldElement } from './FieldElement';

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
  buttonContentRightAligned: {
    transform: 'scaleX(-1)',
    display: 'inline-block',
    textAlign: 'left',
    width: '100%',
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

export interface IFieldGroupProps<NodeType> {
  isVisible: boolean;
  group: IFieldsGroup;
  lineConnectionSide: 'left' | 'right';
  documentType: DocumentType;
  getParentRef?: () => HTMLElement | null;
  getBoxRef: () => HTMLElement | null;
  rightAlign?: boolean;
  level?: number;
  parentExpanded: boolean;
  initiallyExpanded?: boolean;
  renderNode: (
    node: NodeType & IFieldsGroup | NodeType & IFieldsNode
  ) => ReactElement;
}
export function FieldGroup<NodeType>({
  isVisible,
  group,
  lineConnectionSide,
  documentType,
  getParentRef,
  getBoxRef,
  rightAlign = false,
  level = 0,
  parentExpanded,
  initiallyExpanded,
  renderNode,
}: IFieldGroupProps<NodeType>) {
  const getBoundingCanvasRect = useBoundingCanvasRect();
  const { setLineNode } = useMappingNode();
  const ref = useRef<HTMLDivElement | null>(null);
  const [isExpanded, setIsExpanded] = useState(level === 0);
  const toggleExpand = useCallback(() => setIsExpanded(!isExpanded), [
    isExpanded,
    setIsExpanded,
  ]);

  useEffect(() => {
    if (initiallyExpanded !== undefined) {
      setIsExpanded(initiallyExpanded);
    }
  }, [initiallyExpanded]);

  const getChildCoords = useCallback(() => {
    const boxRef = getBoxRef();
    if (ref.current && boxRef) {
      let dimensions = getBoundingCanvasRect(ref.current);
      let boxRect = getBoundingCanvasRect(boxRef);
      return {
        x: lineConnectionSide === 'right' ? boxRect.right : boxRect.left,
        y: Math.min(
          Math.max(dimensions.top + dimensions.height / 2, boxRect.top),
          boxRect.height + boxRect.top
        ),
      };
    }
    return null;
  }, [getBoxRef, getBoundingCanvasRect, lineConnectionSide]);

  const handleChildLines = useCallback(() => {
    if (!isExpanded) {
      const traverseChildren = (f: IFieldsGroup | IFieldsNode) => {
        if ((f as IFieldsGroup).fields) {
          (f as IFieldsGroup).fields.forEach(traverseChildren);
        } else {
          setLineNode(f.id, getChildCoords);
        }
      };
      group.fields.forEach(traverseChildren);
    }
  }, [getChildCoords, group.fields, isExpanded, setLineNode]);

  useEffect(() => {
    handleChildLines();
  }, [handleChildLines, parentExpanded]);

  const content = useMemo(
    () =>
      group.fields.map(f =>
        !(f as IFieldsGroup).fields ? (
          <FieldElement
            key={f.id}
            documentType={documentType}
            lineConnectionSide={lineConnectionSide}
            getParentRef={() =>
              isVisible && isExpanded
                ? ref.current
                : isVisible || !getParentRef
                ? ref.current
                : getParentRef()
            }
            getBoxRef={getBoxRef}
            node={f as IFieldsNode}
            rightAlign={rightAlign}
          >
            {renderNode(f as NodeType & IFieldsNode)}
          </FieldElement>
        ) : (
          <FieldGroup
            isVisible={isVisible && isExpanded}
            lineConnectionSide={lineConnectionSide}
            documentType={documentType}
            getParentRef={() =>
              isVisible || !getParentRef ? ref.current : getParentRef()
            }
            group={f as IFieldsGroup}
            getBoxRef={getBoxRef}
            rightAlign={rightAlign}
            key={f.id}
            level={level + 1}
            parentExpanded={parentExpanded}
            initiallyExpanded={initiallyExpanded}
            renderNode={renderNode}
          />
        )
      ),
    [
      getBoxRef,
      group.fields,
      isExpanded,
      isVisible,
      level,
      parentExpanded,
      getParentRef,
      rightAlign,
      lineConnectionSide,
    ]
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
    <AccordionItem>
      <AccordionToggle
        onClick={toggleExpand}
        isExpanded={isExpanded}
        id={`source-field-group-${group.id}-toggle`}
        className={css(styles.button, rightAlign && styles.buttonRightAlign)}
      >
        <span
          ref={ref}
          className={css(rightAlign && styles.buttonContentRightAligned)}
        >
          <Split>
            <SplitItem style={{ paddingRight: '0.5rem' }}>
              {isExpanded ? <FolderOpenIcon /> : <FolderCloseIcon />}
            </SplitItem>
            <SplitItem isFilled={true}>
              {renderNode(group as NodeType & IFieldsGroup)}
            </SplitItem>
          </Split>
        </span>
      </AccordionToggle>
      <AccordionContent
        id={`source-field-group-${group.id}-content`}
        isHidden={!isExpanded}
        className={css(
          styles.content
        )}
      >
        {content}
      </AccordionContent>
    </AccordionItem>
  );
}
