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
import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  field: {
    padding: "1rem",
    background: "var(--pf-global--BackgroundColor--100)",
    "& + &": {
      borderTop:
        "var(--pf-global--BorderWidth--md) solid var(--pf-global--BorderColor--300)",
    },
    "&:last-child": {
      borderBottom:
        "var(--pf-global--BorderWidth--sm) solid var(--pf-global--BorderColor--200)",
    },
  },
  title: {
    display: "flex",
    alignItems: "center",
  },
  fieldIndex: {
    marginRight: "1rem",
  },
  fieldName: {
    flex: "2 0 calc(100% - 6rem)",
  },
  link: {
    padding: 0,
    marginRight: 5,
  },
  transformationsWrapper: {
    marginTop: "0.5rem",
  },
  transformations: {
    background: "var(--pf-global--BackgroundColor--150)",
  },
});

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
    <div className={css(styles.field)} aria-labelledby={id}>
      <Split>
        <SplitItem isFilled>
          <Title size={"sm"} id={id} className={css(styles.title)}>
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
        <SplitItem>
          {onNewTransformation && (
            <Button
              variant={"plain"}
              onClick={onNewTransformation}
              className={css(styles.link)}
              data-testid={`add-transformation-to-${name}-field-button`}
            >
              <BoltIcon />
            </Button>
          )}
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
        <div className={css(styles.transformationsWrapper)}>
          <Title size={"xs"}>Transformations</Title>
          <div className={css(styles.transformations)}>{children}</div>
        </div>
      )}
    </div>
  );
};
