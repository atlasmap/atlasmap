import React, { FunctionComponent, useCallback, useState } from 'react';
import {
  AccordionContent,
  AccordionItem,
  AccordionToggle,
  DataList,
} from '@patternfly/react-core';

export interface IMappingFieldsProps {
  title: string;
}

export const MappingFields: FunctionComponent<IMappingFieldsProps> = ({
  title,
  children,
}) => {
  const [expanded, setExpanded] = useState(true);
  const toggleExpanded = useCallback(() => setExpanded(!expanded), [expanded]);
  return (
    <AccordionItem>
      <AccordionToggle
        id={title}
        isExpanded={expanded}
        onClick={toggleExpanded}
      >
        {title}
      </AccordionToggle>
      <AccordionContent isHidden={!expanded}>
        <DataList aria-label={'Mapping fields'}>{children}</DataList>
      </AccordionContent>
    </AccordionItem>
  );
};
