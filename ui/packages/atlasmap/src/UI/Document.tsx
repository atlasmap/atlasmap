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
import { AngleDownIcon, AngleRightIcon } from '@patternfly/react-icons';
import {
  Button,
  Card,
  CardActions,
  CardBody,
  CardHeader,
  CardTitle,
  Title,
} from '@patternfly/react-core';
import React, {
  HTMLAttributes,
  KeyboardEvent,
  MouseEvent,
  PropsWithChildren,
  ReactElement,
  ReactNode,
  forwardRef,
  useCallback,
} from 'react';

import { TruncatedString } from './TruncatedString';
import { css } from '@patternfly/react-styles';
import styles from './Document.module.css';
import { useToggle } from '../impl/utils';

export interface IDocumentProps
  extends Omit<HTMLAttributes<HTMLDivElement>, 'title'> {
  title?: string;
  actions?: (ReactElement | null | undefined)[];
  footer?: ReactNode;
  selected?: boolean;
  selectable?: boolean;
  dropTarget?: boolean;
  dropAccepted?: boolean;
  stacked?: boolean;
  scrollIntoView?: boolean;
  noPadding?: boolean;
  noShadows?: boolean;
  startExpanded?: boolean;
  onSelect?: () => void;
  onDeselect?: () => void;
}

export const Document = forwardRef<
  HTMLDivElement,
  PropsWithChildren<IDocumentProps>
>(function Document(
  {
    id,
    title,
    actions,
    footer,
    selected = false,
    dropTarget = false,
    dropAccepted = false,
    stacked = true,
    selectable = false,
    scrollIntoView = false,
    noPadding = false,
    noShadows = false,
    startExpanded,
    onSelect,
    onDeselect,
    children,
    ...props
  },
  ref,
) {
  const { state: isExpanded, toggle: toggleExpanded } = useToggle(
    startExpanded!,
  );
  const handleClick = useCallback(
    (event: MouseEvent) => {
      event.stopPropagation();
      if (onSelect) {
        onSelect();
      }
    },
    [onSelect],
  );
  const handleKey = useCallback(
    (event: KeyboardEvent) => {
      switch (event.key) {
        case 'Enter':
        case 'Space':
          if (onSelect) {
            onSelect();
          }
          break;
        case 'Escape':
          if (onDeselect) {
            onDeselect();
          }
          break;
      }
    },
    [onDeselect, onSelect],
  );
  const makeCardSelected = selected || dropTarget || dropAccepted;

  return (
    <div
      className={css(stacked && styles.stacked)}
      onClick={handleClick}
      onKeyDown={handleKey}
      ref={ref}
      {...props}
    >
      <Card
        isCompact={true}
        className={css(
          styles.card,
          noShadows && styles.noShadows,
          dropAccepted && !dropTarget && styles.dropAccepted,
          dropTarget && styles.dropTarget,
        )}
        isSelected={makeCardSelected}
        isSelectable={makeCardSelected || selectable}
        aria-label={title}
      >
        {(title || actions) && (
          <CardHeader data-codemods="true" className={css(styles.head)}>
            <CardActions className={css(styles.actions)}>
              {actions?.filter((a) => a)}
            </CardActions>
            {title && (
              <CardTitle className={css(styles.header)}>
                <Button
                  variant={'plain'}
                  onClick={toggleExpanded}
                  aria-label={'Expand/collapse this card'}
                  data-testid={`expand-collapse-${title}-button`}
                  className={css(styles.headerButton)}
                >
                  <Title size="lg" headingLevel={'h2'} aria-label={title}>
                    <TruncatedString title={title}>
                      {isExpanded ? <AngleDownIcon /> : <AngleRightIcon />}{' '}
                      {title}
                    </TruncatedString>
                  </Title>
                </Button>
              </CardTitle>
            )}
          </CardHeader>
        )}
        <CardBody
          className={css(
            noPadding ? styles.bodyNoPadding : styles.bodyWithPadding,
            !isExpanded && styles.hidden,
          )}
        >
          {children}
        </CardBody>
        {footer}
      </Card>
    </div>
  );
});
