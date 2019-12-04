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
import React, { ReactElement, useEffect, useRef, useState } from 'react';
import { DocumentType, IFieldsGroup, IFieldsNode } from '../models';
import { FieldGroup } from './FieldGroup';

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

export interface IDocumentProps<NodeType> {
  title: ReactElement | string;
  footer: ReactElement | string;
  fields: IFieldsGroup;
  type: DocumentType;
  lineConnectionSide: 'left' | 'right';
  renderNode: (node: NodeType & (IFieldsGroup | IFieldsNode)) => ReactElement;
  onDelete: () => void;
}

export function Document<NodeType>({
  title,
  footer,
  type,
  lineConnectionSide,
  fields,
  renderNode,
  onDelete
}: IDocumentProps<NodeType>) {
  const ref = useRef<HTMLDivElement | null>(null);
  const [isExpanded, setIsExpanded] = useState(true);
  const toggleIsExpanded = () => setIsExpanded(!isExpanded);
  const [showActions, setShowActions] = useState(false);
  const toggleActions = (open: boolean) => setShowActions(open);
  const [expandFields, setExpandField] = useState<boolean | undefined>(
    undefined
  );
  const handleCollapseField = () => setExpandField(false);
  const handleExpandField = () => setExpandField(true);

  const rightAlign = lineConnectionSide === 'left';

  useEffect(() => {
    if (expandFields !== undefined) {
      setExpandField(undefined);
    }
  }, [expandFields]);

  const getRef = () => ref.current;
  return (
    <div
      ref={ref}
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
                    <DropdownToggleAction
                      key="action"
                      onClick={handleExpandField}
                    >
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
                  onClick={handleCollapseField}
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
          <CardHeader onClick={toggleIsExpanded} style={{ width: '70%' }}>
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
                getBoxRef={getRef}
                documentType={type}
                lineConnectionSide={lineConnectionSide}
                rightAlign={rightAlign}
                parentExpanded={isExpanded}
                initiallyExpanded={expandFields}
                renderNode={renderNode}
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
