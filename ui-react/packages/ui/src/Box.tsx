import React, {
  useCallback,
  UIEvent,
  forwardRef,
  PropsWithChildren
} from 'react';
import { css, StyleSheet } from '@patternfly/react-styles';

const BoxStyles = StyleSheet.create({
  outer: {
    border: '1px solid black',
    borderRadius: '0.3rem',
    width: '100%',
    height: '100%',
    overflow: 'hidden',
    display: 'flex',
    flexFlow: 'column',
    background: 'white',
  },
  header: {
    flex: '0 1 0',
    padding: '0.5rem 1rem'
  },
  body: {
    flex: '1',
    overflowY: 'scroll',
  },
  footer: {
    flex: '0 1 0',
    padding: '0.3rem 1rem',
    textAlign: 'right',
    fontSize: '0.9rem'
  },
});

export interface IBoxProps {
  header?: React.ReactElement;
  footer?: React.ReactElement;
  onChanges?: () => void;
}

/**
 * `Box` sample doc
 */
export const Box =  forwardRef<HTMLDivElement, PropsWithChildren<IBoxProps>>(({
  header,
  footer,
  children,
  onChanges,
}, ref) => {
  const onScroll = useCallback((e: UIEvent<HTMLDivElement>) => {
    e.stopPropagation();
    onChanges && onChanges();
  }, [onChanges]);

  return (
    <div
      className={css(BoxStyles.outer)}
    >
      <div className={css(BoxStyles.header)}>{header}</div>
      <div className={css(BoxStyles.body)} onScroll={onScroll} ref={ref}>
        {children}
      </div>
      <div className={css(BoxStyles.footer)}>{footer}</div>
    </div>
  );
});
