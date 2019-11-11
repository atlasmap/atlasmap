import React, { forwardRef, HTMLAttributes, PropsWithChildren } from 'react';

export const Field = forwardRef<HTMLDivElement, PropsWithChildren<HTMLAttributes<HTMLDivElement>>>((props, ref ) => {
  return (
    <div
      {...props}
      ref={ref}
    />
  );
});
