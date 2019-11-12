import {
  AccordionContent,
  AccordionItem,
  AccordionToggle,
} from '@patternfly/react-core';
import { FolderOpenIcon, FolderCloseIcon } from '@patternfly/react-icons';
import { css, StyleSheet } from '@patternfly/react-styles';
import { useCanvas } from '@src';
import { MappingNode, MappingGroup, MappingNodeType, Rect } from '@src/models';
import { useDimensions } from '@src/useDimensions';
import { FieldElement } from '@src/views/sourcetargetmapper/FieldElement';
import React, {
  FunctionComponent,
  useCallback,
  useEffect,
  useState,
} from 'react';

const styles = StyleSheet.create({
  button: {
    paddingRight: '0.5rem !important',
  },
  content: {
    marginRight:
      'calc(-1 * var(--pf-c-accordion__expanded-content-body--PaddingRight)) !important',
    fontSize: 'inherit !important',
  },
});

export interface IFieldGroupProps {
  isVisible: boolean;
  group: MappingGroup;
  type: MappingNodeType;
  parentRect: Rect;
  boxRect: Rect;
}
export const FieldGroup: FunctionComponent<IFieldGroupProps> = ({
  isVisible,
  group,
  type,
  parentRect,
  boxRect,
}) => {
  const { redraw } = useCanvas();
  const [ref, dimensions] = useDimensions();
  const [isExpanded, setIsExpanded] = useState(true);
  const toggleExpand = useCallback(() => setIsExpanded(!isExpanded), [
    isExpanded,
    setIsExpanded,
  ]);
  useEffect(redraw, [isExpanded]);
  return (
    <AccordionItem>
      <AccordionToggle
        onClick={toggleExpand}
        isExpanded={isExpanded}
        id={`source-field-group-${group.id}-toggle`}
        className={css(styles.button)}
      >
        <span ref={ref}>
          {isExpanded ? <FolderOpenIcon /> : <FolderCloseIcon />} {group.title}
        </span>
      </AccordionToggle>
      <AccordionContent
        id={`source-field-group-${group.id}-content`}
        isHidden={!isExpanded}
        className={css(styles.content)}
      >
        {group.fields.map(f =>
          (f as MappingNode).element ? (
            <FieldElement
              key={f.id}
              type={type}
              parentRect={isVisible ? dimensions : parentRect}
              boxRect={boxRect}
              node={f as MappingNode}
            />
          ) : (
            <FieldGroup
              isVisible={isVisible && isExpanded}
              type={type}
              parentRect={isVisible ? dimensions : parentRect}
              group={f as MappingGroup}
              boxRect={boxRect}
              key={f.id}
            />
          )
        )}
      </AccordionContent>
    </AccordionItem>
  );
};
