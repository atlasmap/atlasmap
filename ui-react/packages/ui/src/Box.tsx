import React, {
  useCallback,
  UIEvent,
  forwardRef,
  PropsWithChildren,
  WheelEvent
} from 'react';
import { css, StyleSheet } from '@patternfly/react-styles';

const BoxStyles = StyleSheet.create({
  outer: {
    width: '100%',
    height: '100%',
    display: 'flex',
    flexFlow: 'column',
    padding: '1rem',
  },
  header: {
    flex: '0 1 0',
    paddingBottom: '0.5rem'
  },
  body: {
    flex: '0 0 1',
    height: '100%',
    display: 'flex',
    flexFlow: 'column'
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
  onLayout?: () => void;
}

/**
 * `Box` sample doc
 */
export const Box =  forwardRef<HTMLDivElement, PropsWithChildren<IBoxProps>>(({
  header,
  footer,
  children,
  onLayout,
}, ref) => {
  const onScroll = useCallback((e: UIEvent<HTMLDivElement>) => {
    e.stopPropagation();
    onLayout && onLayout();
  }, [onLayout]);

  const handleWheel = (e: WheelEvent) => {
    e.stopPropagation()
  };

  return (
    <div
      className={css(BoxStyles.outer)}
    >
      <div className={css(BoxStyles.header)}>{header}</div>
      <div className={css(BoxStyles.body)} onScroll={onScroll} onWheel={handleWheel} ref={ref}>
        {children}
      </div>
      <div className={css(BoxStyles.footer)}>{footer}</div>
    </div>
  );
});
