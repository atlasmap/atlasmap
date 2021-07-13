/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import React, {
  PropsWithChildren,
  ReactChild,
  ReactElement,
  forwardRef,
} from 'react';

import { Tooltip } from '@patternfly/react-core';
import { TruncatedString } from './TruncatedString';
import { css } from '@patternfly/react-styles';
import styles from './DocumentField.module.css';
import { useToggle } from '../impl/utils';

export interface IDocumentFieldProps {
  name: ReactChild;
  type: string;
  scope?: string;
  icon?: ReactElement;
  statusIcons?: (ReactElement | null)[];
  actions?: (ReactElement | null)[];
  showType?: boolean;
  isDragging?: boolean;
  isFocused?: boolean;
  isSelected?: boolean;
  isDisabled?: boolean;
}

export const DocumentField = forwardRef<
  HTMLDivElement,
  PropsWithChildren<IDocumentFieldProps>
>(function DocumentField(
  {
    name,
    type,
    scope,
    icon,
    statusIcons,
    actions,
    showType = false,
    isDragging = false,
    isFocused = false,
    isSelected = false,
    isDisabled = false,
    children,
  },
  ref,
) {
  const {
    state: isHovering,
    toggleOff: hideActions,
    toggleOn: showActions,
  } = useToggle(false);
  return (
    <div
      ref={ref}
      className={css(
        styles.element,
        isDragging && styles.isDragging,
        isSelected && styles.isSelected,
        isDisabled && styles.isDisabled,
      )}
      onMouseEnter={!isDisabled ? showActions : undefined}
      onMouseLeave={!isDisabled ? hideActions : undefined}
    >
      <div className={styles.row}>
        {icon && <div className={styles.nameIcon}>{icon}</div>}
        <Tooltip
          key={`${name}`}
          position={'auto'}
          enableFlip={true}
          content={scope ? `${name} <${scope}>` : `${name}`}
          entryDelay={750}
          exitDelay={100}
        >
          <div
            className={styles.nameWrapper}
            data-testid={`document-${name}-field`}
          >
            <TruncatedString>{name}</TruncatedString>
            <span>{showType && ` (${type})`}</span>
            <span className={styles.statusIcons}>
              {statusIcons && statusIcons?.filter((a) => a)}
            </span>
          </div>
        </Tooltip>
        {(isHovering || isFocused) && actions && !isDisabled && (
          <div className={styles.actions}>{actions?.filter((a) => a)}</div>
        )}
      </div>
      {children}
    </div>
  );
});
