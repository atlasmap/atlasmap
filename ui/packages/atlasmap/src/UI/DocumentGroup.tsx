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
import { FolderCloseIcon, FolderOpenIcon } from '@patternfly/react-icons';
import React, {
  PropsWithChildren,
  ReactElement,
  forwardRef,
  useRef,
} from 'react';
import { Tooltip } from '@patternfly/react-core';
import styles from './DocumentGroup.module.css';

export interface IDocumentGroupProps {
  name: string;
  icon?: ReactElement;
  iconTooltip?: string;
  type?: string;
  showType?: boolean;
  expanded?: boolean;
}

export const DocumentGroup = forwardRef<
  HTMLSpanElement,
  PropsWithChildren<IDocumentGroupProps>
>(function DocumentGroup(
  { name, type, icon, iconTooltip, showType = false, expanded },
  ref,
) {
  const spanRef = useRef<HTMLSpanElement | null>(null);

  const handleRef = (el: HTMLSpanElement | null) => {
    spanRef.current = el;
    if (ref) {
      if (typeof ref === 'function') {
        ref(el);
      } else {
        // @ts-ignore
        ref.current = el;
      }
    }
  };

  return (
    <span
      className={styles.buttonContent}
      ref={handleRef}
      data-testid={`field-group-${name}-expanded-${expanded}-field`}
    >
      <span className={styles.buttonIcon}>
        {expanded ? <FolderOpenIcon /> : <FolderCloseIcon />}
      </span>
      {name}
      {showType && ` (${type})`}
      {icon && (
        <Tooltip enableFlip content={iconTooltip}>
          <span className={styles.icon}>{icon}</span>
        </Tooltip>
      )}
    </span>
  );
});
