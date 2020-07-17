import React, { Children, FunctionComponent } from "react";

import {
  Button,
  InputGroup,
  InputGroupText,
  Split,
  SplitItem,
  TextInput,
  Title,
  Tooltip,
} from "@patternfly/react-core";
import { BoltIcon, TrashIcon, InfoAltIcon } from "@patternfly/react-icons";
import { css } from "@patternfly/react-styles";

import styles from "./MappingField.css";

export interface IMappingFieldProps {
  name: string;
  info: string;
  index: number;
  canShowIndex: boolean;
  onDelete: () => void;
  onIndexChange?: (value: string) => void;
  onNewTransformation?: () => void;
}

export const MappingField: FunctionComponent<IMappingFieldProps> = ({
  name,
  info,
  index,
  canShowIndex,
  onDelete,
  onIndexChange,
  onNewTransformation,
  children,
}) => {
  const id = `mapping-field-${name}`;
  return (
    <div className={css(styles.field)} aria-labelledby={id} data-testid={id}>
      <Split>
        <SplitItem isFilled>
          <Title
            headingLevel="h2"
            size="md"
            id={id}
            className={css(styles.title)}
          >
            {canShowIndex && (
              <Tooltip
                position={"auto"}
                enableFlip={true}
                content={
                  <div>
                    Edit the index for this element by selecting the arrows.
                    Placeholders may be automatically inserted to account for
                    any gaps in the indexing
                  </div>
                }
              >
                <InputGroup className={css(styles.fieldIndex)}>
                  <InputGroupText>#</InputGroupText>
                  <TextInput
                    type={"number"}
                    value={index}
                    id={"index"}
                    onChange={onIndexChange}
                    data-testid={`change-${name}-input-index`}
                    isDisabled={!onIndexChange}
                  />
                </InputGroup>
              </Tooltip>
            )}
            <Tooltip
              position={"auto"}
              enableFlip={true}
              content={<div>{info}</div>}
            >
              <div className={css(styles.fieldName)}>
                {name} <InfoAltIcon />
              </div>
            </Tooltip>
          </Title>
        </SplitItem>
        {onNewTransformation && (
          <SplitItem>
            <Button
              variant={"plain"}
              onClick={onNewTransformation}
              className={css(styles.link)}
              data-testid={`add-transformation-to-${name}-field-button`}
            >
              <BoltIcon />
            </Button>
          </SplitItem>
        )}
        <SplitItem>
          <Button
            variant={"plain"}
            onClick={onDelete}
            className={css(styles.link)}
            data-testid={`remove-${name}-from-mapping-button`}
          >
            <TrashIcon />
          </Button>
        </SplitItem>
      </Split>
      {/*
        Show established field action transformations associated with this
        field.
        */}
      {children && Children.count(children) > 0 && (
        <div className={css("pf-c-form", styles.transformationsWrapper)}>
          <Title headingLevel="h2" size="md">
            Transformations
          </Title>
          <div className={css(styles.transformations)}>{children}</div>
        </div>
      )}
    </div>
  );
};
