import {
  Accordion,
  Button,
  Card,
  CardActions,
  CardBody,
  CardFooter,
  CardHead,
  CardHeader,
} from '@patternfly/react-core';
import { CaretDownIcon, CaretRightIcon, EditIcon } from '@patternfly/react-icons';
import { css, StyleSheet } from '@patternfly/react-styles';
import React, { FunctionComponent, ReactElement, useEffect, useRef, useState } from 'react';
import { useCanvas } from '../../../canvas';

const styles = StyleSheet.create({
  wrapper: {
    marginBottom: '1rem',
    direction: 'rtl',
    minHeight: '90px',
  },
  wrapperIsExpanded: {
    minHeight: '148px',
  },
  card: {
    height: '100%'
  },
  title: {
    direction: 'ltr',
    padding: '0.5rem !important',
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
    padding: '0 !important',
  },
  bodyIsHidden: {
    height: 0,
    overflow: 'hidden'
  },
  accordion: {
    padding: '0 !important',
    'box-shadow': 'none'
  },
  footer: {
    borderTop: '1px solid #eee',
    paddingTop: '0.5rem',
    paddingBottom: '0.5rem',
    direction: 'ltr',
  },
  footerRightAligned: {
    transform: 'scaleX(-1)'
  },
});

export interface IDocumentProps {
  title: ReactElement | string;
  footer: ReactElement | string;
  children: (props: { ref: HTMLElement | null, isExpanded: boolean }) => ReactElement;
  rightAlign?: boolean;
}

export const Document: FunctionComponent<IDocumentProps> = ({ title, footer, children, rightAlign = false }) => {
  const { redraw } = useCanvas();
  const ref = useRef<HTMLDivElement | null>(null);
  const [isExpanded, setIsExpanded] = useState(true);
  const toggleIsExpanded = () => setIsExpanded(!isExpanded);
  useEffect(redraw, [isExpanded]);
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
            style={{ width: '70%' }}
          >
            <Button variant={'link'}>
              {isExpanded ? <CaretDownIcon /> : <CaretRightIcon />}{' '}
              {title}
            </Button>
          </CardHeader>
        </CardHead>
        <CardBody className={css(styles.body, !isExpanded && styles.bodyIsHidden)}>
          <div className={css(styles.body, !isExpanded && styles.bodyIsHidden)}>
            <Accordion asDefinitionList={false} className={css(styles.accordion)}>
              {children({ ref: ref.current, isExpanded })}
            </Accordion>
          </div>
        </CardBody>
        <CardFooter className={css(styles.footer, rightAlign && styles.footerRightAligned)}>
          {footer}
        </CardFooter>
      </Card>
    </div>
  );
}
