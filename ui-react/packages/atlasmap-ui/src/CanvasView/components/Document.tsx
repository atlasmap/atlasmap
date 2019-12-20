import {
  Accordion,
  Button,
  Card,
  CardActions,
  CardBody,
  CardFooter,
  CardHead,
  CardHeader,
  Dropdown,
  DropdownItem,
  DropdownItemIcon,
  DropdownSeparator,
  DropdownToggle,
  DropdownToggleAction,
} from '@patternfly/react-core';
import {
  CaretDownIcon,
  CaretRightIcon,
  TrashIcon,
  FolderCloseIcon,
  FolderOpenIcon,
} from '@patternfly/react-icons';
import { css, StyleSheet } from '@patternfly/react-styles';
import React, { ReactElement, useState } from 'react';
import { IFieldsGroup } from '../models';
import { FieldGroup, IFieldGroupProps } from './FieldGroup';

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
    height: '100%',
  },
  cardHeader: {
    width: '70%',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
  },
  title: {
    direction: 'ltr',
    padding: '0.5rem !important',
  },
  titleRightAligned: {
    transform: 'scaleX(-1)',
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
    overflow: 'hidden',
  },
  accordion: {
    padding: '0 !important',
    'box-shadow': 'none',
  },
  footer: {
    borderTop: '1px solid #eee',
    paddingTop: '0.5rem',
    paddingBottom: '0.5rem',
    direction: 'ltr',
  },
  footerRightAligned: {
    transform: 'scaleX(-1)',
  },
});

export interface IDocumentProps
  extends Pick<IFieldGroupProps, 'renderNode'>,
    Pick<IFieldGroupProps, 'renderGroup'> {
  title: ReactElement | string;
  footer: ReactElement | string;
  fields: IFieldsGroup;
  lineConnectionSide: 'left' | 'right';
  onDelete: () => void;
}

export function Document({
  title,
  footer,
  lineConnectionSide,
  fields,
  renderNode,
  renderGroup,
  onDelete,
}: IDocumentProps) {
  const [isUserExpanded, setIsUserExpanded] = useState(true);
  const [shouldBeExpanded, setShouldBeExpanded] = useState(false);
  const toggleIsExpanded = () => setIsUserExpanded(!isUserExpanded);
  const [showActions, setShowActions] = useState(false);
  const toggleActions = (open: boolean) => setShowActions(open);

  const isExpanded = shouldBeExpanded || isUserExpanded;
  const rightAlign = lineConnectionSide === 'left';

  return (
    <div
      className={css(styles.wrapper, isExpanded && styles.wrapperIsExpanded)}
    >
      <Card isCompact={true} className={css(styles.card)}>
        <CardHead
          className={css(styles.title, rightAlign && styles.titleRightAligned)}
        >
          <CardActions>
            <Dropdown
              toggle={
                <DropdownToggle
                  splitButtonItems={[
                    <DropdownToggleAction key="action" onClick={() => void 0}>
                      <FolderOpenIcon />
                    </DropdownToggleAction>,
                  ]}
                  splitButtonVariant="action"
                  onToggle={toggleActions}
                />
              }
              isOpen={showActions}
              position={'right'}
              dropdownItems={[
                <DropdownItem
                  variant={'icon'}
                  key={'collapse'}
                  onClick={() => void 0}
                >
                  <DropdownItemIcon>
                    <FolderCloseIcon />
                  </DropdownItemIcon>
                  Collapse all
                </DropdownItem>,
                <DropdownSeparator key={'sep-1'} />,
                <DropdownItem
                  variant={'icon'}
                  key={'delete'}
                  onClick={onDelete}
                >
                  <DropdownItemIcon>
                    <TrashIcon />
                  </DropdownItemIcon>
                  Remove instance or schema file
                </DropdownItem>,
              ]}
            />
          </CardActions>
          <CardHeader
            onClick={toggleIsExpanded}
            className={css(styles.cardHeader)}
          >
            <Button variant={'link'}>
              {isExpanded ? <CaretDownIcon /> : <CaretRightIcon />} {title}
            </Button>
          </CardHeader>
        </CardHead>
        <CardBody
          className={css(styles.body, !isExpanded && styles.bodyIsHidden)}
        >
          <div className={css(styles.body, !isExpanded && styles.bodyIsHidden)}>
            <Accordion
              asDefinitionList={false}
              className={css(styles.accordion)}
            >
              <FieldGroup
                isVisible={true}
                group={fields}
                lineConnectionSide={lineConnectionSide}
                rightAlign={rightAlign}
                parentExpanded={isExpanded}
                expandParent={setShouldBeExpanded}
                renderNode={renderNode}
                renderGroup={renderGroup}
              />
            </Accordion>
          </div>
        </CardBody>
        <CardFooter
          className={css(
            styles.footer,
            rightAlign && styles.footerRightAligned
          )}
        >
          {footer}
        </CardFooter>
      </Card>
    </div>
  );
}
