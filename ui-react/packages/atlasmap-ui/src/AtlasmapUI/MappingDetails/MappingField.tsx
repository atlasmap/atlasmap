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

export interface IMappingFieldProps {
  name: string;
  info: string;
  index: number;
  showIndex: boolean;
  canEditIndex: boolean;
  onAdd: () => void;
  onDelete: () => void;
}

export const MappingField: FunctionComponent<IMappingFieldProps> = ({
  name,
  info,
  index,
  showIndex,
  canEditIndex,
  onAdd,
  onDelete,
  children,
}) => {
  const [showActions, setShowActions] = useState(false);
  const toggleActions = (open: boolean) => setShowActions(open);

  const id = `mapping-field-${name}`;
  const actionsId = `${id}-actions`;
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
                    style={{
                      background: 'transparent',
                      color: 'inherit',
                      border: '0 none',
                      width: 40,
                    }}
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
                splitButtonItems={[
                  <DropdownToggleAction key='action' onClick={onAdd}>
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
              <DropdownItem variant={'icon'} key={'delete'} onClick={onDelete}>
                <DropdownItemIcon>
                  <TrashIcon />
                </DropdownItemIcon>
                Remove mapping element
              </DropdownItem>,
            ]}
          />
        </DataListAction>
      </DataListItemRow>
      {Children.count(children) > 0 && (
        <DataListContent
          aria-label={'Field transformations'}
          style={{ boxShadow: 'none' }}
        >
          <Title size={'xs'} headingLevel={'h4'}>Transformations</Title>
          {children}
        </DataListContent>
      )}
    </DataListItem>
  );
};
