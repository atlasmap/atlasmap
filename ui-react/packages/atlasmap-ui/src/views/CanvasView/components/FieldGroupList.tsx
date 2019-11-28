import { Accordion, Button, Card, CardActions, CardBody, CardHead, CardHeader } from '@patternfly/react-core';
import { CaretDownIcon, CaretRightIcon, EditIcon } from '@patternfly/react-icons';
import { css, StyleSheet } from '@patternfly/react-styles';
import React, { FunctionComponent, ReactElement, useRef, useState } from 'react';

const styles = StyleSheet.create({
  wrapper: {
    marginBottom: '1rem',
    direction: 'rtl',
    minHeight: '68px',
  },
  wrapperIsExpanded: {
    minHeight: '108px',
  },
  card: {
    height: '100%'
  },
  title: {
    direction: 'ltr',
  },
  titleRightAligned: {
    transform: 'scaleX(-1)'
  },
  body: {
    overflowY: 'auto',
    overflowX: 'hidden',
    fontSize: 'inherit',
    height: '100%',
    flex: '0 1 auto',
    padding: '0',
  },
  bodyIsHidden: {
    height: 1,
  },
  accordion: {
    padding: 0,
    'box-shadow': 'none'
  }
});

export interface IFieldGroupList {
  title: ReactElement | string;
  children: (props: { ref: HTMLElement | null, isExpanded: boolean }) => ReactElement;
  rightAlign?: boolean;
}

export const FieldGroupList: FunctionComponent<IFieldGroupList> = ({ title, children, rightAlign = false }) => {
  const ref = useRef<HTMLDivElement | null>(null);
  const [isExpanded, setIsExpanded] = useState(true);
  const toggleIsExpanded = () => setIsExpanded(!isExpanded);
  return (
    <div ref={ref} className={css(styles.wrapper, isExpanded && styles.wrapperIsExpanded)}>
      <Card isCompact={true} className={css(styles.card)}>
        <CardHead
          className={css(styles.title, rightAlign && styles.titleRightAligned)}
        >
          <CardActions>
            <Button variant={'plain'}>
              <EditIcon />
            </Button>
          </CardActions>
          <CardHeader
            onClick={toggleIsExpanded}
          >
            <Button variant={'link'}>
              {isExpanded ? <CaretDownIcon /> : <CaretRightIcon />}{' '}
              {title}
            </Button>
          </CardHeader>
        </CardHead>
        <CardBody className={css(styles.body, !isExpanded && styles.bodyIsHidden)}>
          <div className={css(styles.body)}>
            <Accordion asDefinitionList={false} className={css(styles.accordion)}>
              {children({ ref: ref.current, isExpanded })}
            </Accordion>
          </div>
        </CardBody>
      </Card>
    </div>
  );
}
