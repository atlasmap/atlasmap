import {
  AccordionContent,
  AccordionItem,
  AccordionToggle,
} from '@patternfly/react-core';
import { FolderOpenIcon, FolderCloseIcon } from '@patternfly/react-icons';
import { css, StyleSheet } from '@patternfly/react-styles';
import React, {
  forwardRef,
  PropsWithChildren,
  ReactElement,
  useCallback, useEffect,
  useState,
} from 'react';

const styles = StyleSheet.create({
  button: {
    paddingRight: '0.5rem !important'
  },
  content: {
    marginRight: 'calc(-1 * var(--pf-c-accordion__expanded-content-body--PaddingRight)) !important',
  }
});

export interface IFieldGroupProps {
  id: string | number;
  title: ReactElement | string;
  onLayout?: () => void;
}
export const FieldGroup = forwardRef<HTMLElement, PropsWithChildren<IFieldGroupProps>>(({
  id,
  title,
  onLayout,
  children,
}, ref) => {
  const [isExpanded, setIsExpanded] = useState(true);
  const toggleExpand = useCallback(() => setIsExpanded(!isExpanded), [
    isExpanded,
    setIsExpanded,
    onLayout
  ]);
  useEffect(() => onLayout && onLayout(), [onLayout, isExpanded]);
  return (
    <AccordionItem>
      <AccordionToggle
        onClick={toggleExpand}
        isExpanded={isExpanded}
        id={`source-field-group-${id}-toggle`}
        className={css(styles.button)}
      >
        <span ref={ref}>
          {isExpanded ? <FolderOpenIcon /> : <FolderCloseIcon />} {title}
        </span>
      </AccordionToggle>
      <AccordionContent
        id={`source-field-group-${id}-content`}
        isHidden={!isExpanded}
        className={css(styles.content)}
      >
        {children}
      </AccordionContent>
    </AccordionItem>
  );
});
