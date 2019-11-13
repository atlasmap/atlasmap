import { useCanvas } from '@src';
import React, {
  forwardRef,
  PropsWithChildren,
  ReactElement,
  HTMLAttributes,
} from 'react';
import { css, StyleSheet } from '@patternfly/react-styles';

const BoxStyles = StyleSheet.create({
  outer: {
    width: '100%',
    height: '100%',
    display: 'flex',
    flexFlow: 'column',
    padding: '1rem',
    userSelect: 'none',
  },
  header: {
    flex: '0 1 0',
    paddingBottom: '0.5rem',
  },
  body: {
    flex: '0 0 1',
    display: 'flex',
    flexFlow: 'column',
    height: '100%',
  },
  footer: {
    flex: '0 1 0',
    padding: '0.3rem 1rem',
    textAlign: 'right',
  },
});

export interface IBoxProps extends HTMLAttributes<HTMLDivElement> {
  header?: ReactElement | string;
  footer?: ReactElement | string;
  onLayout?: () => void;
}

/**
 * `Box` sample doc
 */
export const Box = forwardRef<HTMLDivElement, PropsWithChildren<IBoxProps>>(
  ({ header, footer, children, onLayout, ...props }, ref) => {
    const { redraw } = useCanvas();
    return (
      <div className={css(BoxStyles.outer)}>
        <div className={css(BoxStyles.header)}>{header}</div>
        <div
          className={css(BoxStyles.body)}
          ref={ref}
          onWheel={redraw}
          onScroll={redraw}
          {...props}
        >
          {children}
        </div>
        <div className={css(BoxStyles.footer)}>{footer}</div>
      </div>
    );
  }
);
