import {
  DataListAction,
  DataListCell,
  DataListContent,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  Dropdown,
  DropdownItem,
  DropdownItemIcon,
  DropdownToggle,
  DropdownToggleAction,
  Label,
  Title,
  Tooltip,
} from '@patternfly/react-core';
import React, { FunctionComponent, useState, Children } from 'react';
import { BoltIcon, InfoAltIcon, TrashIcon } from '@patternfly/react-icons';
import { css, StyleSheet } from '@patternfly/react-styles';

const styles = StyleSheet.create({
  dataListContent: { boxShadow: 'none' },
  indexInput: {
    background: 'transparent',
    color: 'inherit',
    border: '0 none',
    width: 40,
  },
  bodyDropdown: {
    background: '#d4d4d4',
    border: '1px solid #eee',
    zIndex: 1,
  },
});

export interface IMappingFieldProps {
  name: string;
  info: string;
  index: number;
  showIndex: boolean;
  canEditIndex: boolean;
  onDelete: () => void;
  onNewTransformation: () => void;
}

export const MappingField: FunctionComponent<IMappingFieldProps> = ({
  name,
  info,
  index,
  showIndex,
  canEditIndex,
  onDelete,
  onNewTransformation,
  children,
}) => {
  const [showActions, setShowActions] = useState(false);
  const toggleActions = (open: boolean) => setShowActions(open);

  const id = `mapping-field-${name}`;
  const actionsId = `${id}-actions`;
  const dropdownId = `${actionsId}-dropdown`;
  return (
    <DataListItem aria-labelledby={id}>
      <DataListItemRow>
        <DataListItemCells
          dataListCells={[
            <DataListCell key={id} isFilled={true}>
              <Title size={'md'} headingLevel={'h3'} id={id}>
                <Tooltip
                  position={'auto'}
                  enableFlip={true}
                  content={<div>{info}</div>}
                >
                  <span>
                    <InfoAltIcon /> {name}
                  </span>
                </Tooltip>
              </Title>
            </DataListCell>,
            <DataListCell key={'index'} isFilled={false}>
              {showIndex && (
                <Label>
                  #{' '}
                  <input
                    type={'number'}
                    value={index}
                    id={'index'}
                    disabled={!canEditIndex}
                    className={css(styles.indexInput)}
                  />
                </Label>
              )}
            </DataListCell>,
          ]}
        />
        <DataListAction
          aria-labelledby={actionsId}
          id={actionsId}
          aria-label='Actions'
        >
          <Dropdown
            toggle={
              <DropdownToggle
                id={dropdownId}
                data-testid="fieldDetailsSelect"

                splitButtonItems={[
                  <DropdownToggleAction
                   key='action'
                   onClick={onNewTransformation}
                  >
                    <Tooltip
                      position={'auto'}
                      enableFlip={true}
                      content={<div>Add transformation</div>}
                    >
                      <BoltIcon />
                    </Tooltip>
                  </DropdownToggleAction>,
                ]}
                splitButtonVariant='action'
                onToggle={toggleActions}
              />
            }
            isOpen={showActions}
            position={'right'}
            dropdownItems={[
              <DropdownItem variant={'icon'}
                key={'delete'}
                onClick={onDelete}
                className={css(styles.bodyDropdown)}>
                <DropdownItemIcon>
                  <TrashIcon />
                </DropdownItemIcon>
                Remove mapping element
              </DropdownItem>,
            ]}
          />
        </DataListAction>
      </DataListItemRow>

      // Show established field action transformations associated with this field.
      {Children.count(children) > 0 && (
        <DataListContent
          aria-label={'Field transformations'}
          className={css(styles.dataListContent)}
        >
          <Title size={'xs'} headingLevel={'h4'}>Transformations</Title>
          {children}
        </DataListContent>
      )}
    </DataListItem>
  );
};
