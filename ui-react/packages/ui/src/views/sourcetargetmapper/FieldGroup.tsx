import {
  AccordionContent,
  AccordionItem,
  AccordionToggle,
} from '@patternfly/react-core';
import { FolderOpenIcon, FolderCloseIcon } from '@patternfly/react-icons';
import { css, StyleSheet } from '@patternfly/react-styles';
import React, {
  FunctionComponent,
  useCallback,
  useEffect,
  useRef,
  useState,
} from 'react';
import { useCanvas } from '../../canvas';
import { ElementType, IFieldsGroup, IFieldsNode } from '../../models';
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
      flex: 1
    }
  },
  content: {
    marginRight:
      'calc(-1 * var(--pf-c-accordion__expanded-content-body--PaddingRight)) !important',
    fontSize: 'inherit !important',
  },
  contentRightAligned: {
    paddingRight: 'var(--pf-c-accordion__expanded-content-body--PaddingRight) !important'
  },
  buttonContentRightAligned: {
    transform: 'scaleX(-1)',
    display: 'inline-block',
    textAlign: 'left',
    width: '100%',
    order: 1,
    paddingLeft: 'var(--pf-c-accordion__toggle--PaddingRight)'
  }
});

export interface IFieldGroupProps {
  isVisible: boolean;
  group: IFieldsGroup;
  type: ElementType;
  parentRef?: HTMLElement | null;
  boxRef?: HTMLElement | null;
  rightAlign?: boolean;
}
export const FieldGroup: FunctionComponent<IFieldGroupProps> = ({
  isVisible,
  group,
  type,
  parentRef = null,
  boxRef = null,
  rightAlign = false
}) => {
  const { redraw } = useCanvas();
  const ref = useRef<HTMLElement | null>(null);
  const [isExpanded, setIsExpanded] = useState(true);
  const toggleExpand = useCallback(() => setIsExpanded(!isExpanded), [
    isExpanded,
    setIsExpanded,
  ]);
  useEffect(() => {
    redraw();
  }, [isExpanded, redraw]);
  return (
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
          {isExpanded ? <FolderOpenIcon /> : <FolderCloseIcon />} {group.title}
        </span>
      </AccordionToggle>
      <AccordionContent
        id={`source-field-group-${group.id}-content`}
        isHidden={!isExpanded}
        className={css(styles.content, rightAlign && styles.contentRightAligned)}
      >
        {group.fields.map(f =>
          (f as IFieldsNode).element ? (
            <FieldElement
              key={f.id}
              type={type}
              parentRef={
                isVisible && isExpanded
                  ? ref.current
                  : isVisible || !parentRef
                  ? ref.current
                  : parentRef
              }
              boxRef={boxRef}
              node={f as IFieldsNode}
              rightAlign={rightAlign}
            />
          ) : (
            <FieldGroup
              isVisible={isVisible && isExpanded}
              type={type}
              parentRef={isVisible || !parentRef ? ref.current : parentRef}
              group={f as IFieldsGroup}
              boxRef={boxRef}
              rightAlign={rightAlign}
              key={f.id}
            />
          )
        )}
      </AccordionContent>
    </AccordionItem>
  );
};
