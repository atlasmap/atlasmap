import React, { FunctionComponent, useCallback, useState } from 'react';
import {
  AccordionContent,
  AccordionItem,
  AccordionToggle,
  DataList,
} from '@patternfly/react-core';
import { css, StyleSheet } from '@patternfly/react-styles';

export interface IMappingFieldsProps {
  title: string;
}

const styles = StyleSheet.create({
  accContent: {
    fontSize: 'small',
    fontWeight: 'bold',
  },
  accTogText: {
    fontSize: 'medium',
    fontWeight: 'bold',
  }
});

export const MappingFields: FunctionComponent<IMappingFieldsProps> = ({
  title,
  children,
}) => {
  const [expanded, setExpanded] = useState(true);
  const toggleExpanded = useCallback(() => setExpanded(!expanded), [expanded]);
  return (
    <AccordionItem>
      <AccordionToggle
        className={css(styles.accTogText)}
        id={title}
        isExpanded={expanded}
        onClick={toggleExpanded}
      >
        {title}
      </AccordionToggle>
      <AccordionContent className={css(styles.accContent)} isHidden={!expanded}>
        <DataList aria-label={'Mapping fields'}>
          {children}
        </DataList>
      </AccordionContent>
    </AccordionItem>
  );
};
