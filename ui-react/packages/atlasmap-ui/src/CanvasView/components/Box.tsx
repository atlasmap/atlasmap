import React, {
  CSSProperties,
  forwardRef,
  PropsWithChildren,
  ReactElement,
  useMemo,
  useRef,
} from 'react';
import { css, StyleSheet } from '@patternfly/react-styles';
import { BoxProvider } from './BoxProvider';

const styles = StyleSheet.create({
  outer: {
    width: '100%',
    height: '100%',
    flexFlow: 'column',
    userSelect: 'none',
    display: 'flex',
  },
  header: {
    flex: '0 1 0',
    paddingBottom: '0.5rem',
  },
  body: {
    flex: '1',
    display: 'flex',
    flexFlow: 'column',
    height: '100%',
  },
  bodyRightAligned: {
    direction: 'rtl',
    transform: 'scaleX(-1)',
  },
  footer: {
    flex: '0 1 0',
    padding: '0.3rem 1rem',
    textAlign: 'right',
  },
});

export interface IBoxProps {
  scrollable?: boolean;
  visible?: boolean;
  header?: ReactElement | string;
  footer?: ReactElement | string;
  rightAlign?: boolean;
}

/**
 * `Box` sample doc
 */
export const Box = forwardRef<HTMLDivElement, PropsWithChildren<IBoxProps>>(
  (
    {
      scrollable = false,
      visible = true,
      header,
      footer,
      children,
      rightAlign = false,
    },
    ref
  ) => {
    const scrollableArea = useRef<HTMLDivElement | null>(null);
    const getScrollableAreaRef = useMemo(
      () => () => scrollableArea.current,
      []
    );
    const style = useMemo(
      () =>
        ({
          height: scrollable ? '100%' : undefined,
          overflow: scrollable ? 'auto' : undefined,
          opacity: visible ? 1 : 0,
        } as CSSProperties),
      [scrollable, visible]
    );
    return (
      <BoxProvider getScrollableAreaRef={getScrollableAreaRef}>
        <div className={css(styles.outer)} ref={ref}>
          {header && <div className={css(styles.header)}>{header}</div>}
          <div
            className={css(styles.body, rightAlign && styles.bodyRightAligned)}
            ref={scrollableArea}
            style={style}
          >
            {children}
          </div>
          {footer && <div className={css(styles.footer)}>{footer}</div>}
        </div>
      </BoxProvider>
    );
  }
);
