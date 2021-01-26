import React, { Children, FunctionComponent, KeyboardEvent } from "react";

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
import { DraggableField, FieldDropTarget, NodeRef } from ".";
import { IAtlasmapField } from "src/Views";

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
    padding: "0.5rem",
    background: "var(--pf-global--BackgroundColor--150)",
  },
  transformations: {},
});

export interface IMappingFieldProps {
  field: IAtlasmapField;
  name: string;
  info: string;
  index: number;
  canShowIndex: boolean;
  mappingExpressionEnabled: boolean;
  hasTransformations: boolean;
  onDelete: () => void;
  onIndexChange?: (value: string | IAtlasmapField) => void;
  onNewTransformation?: () => void;
}

function onKeyDown(event: KeyboardEvent<HTMLDivElement>): void {
  event.preventDefault();
}

export const MappingField: FunctionComponent<IMappingFieldProps> = ({
  field,
  name,
  info,
  index,
  canShowIndex,
  mappingExpressionEnabled,
  hasTransformations,
  onDelete,
  onIndexChange,
  onNewTransformation,
  children,
}) => {
  const id = `mapping-field-${name}`;
  return (
    <FieldDropTarget
      key={field ? field.id : name}
      target={{
        id: field.id,
        name: field.name,
        type: "mapping",
        payload: field,
      }}
      canDrop={() => {
        return true;
      }}
      accept={["mapping"]}
    >
      {({ isTarget }) => (
        <DraggableField
          field={{
            type: "mapping",
            id: field.id,
            name: field.name,
            payload: field,
          }}
          onDrop={(_, target) => {
            if (!onIndexChange) {
              return;
            }
            onIndexChange(target!.payload as IAtlasmapField);
          }}
        >
          {({ isDragging }) => (
            <NodeRef
              id={[
                field.id,
                isDragging ? "dnd-start" : undefined,
                isTarget ? "dnd-target-field" : undefined,
              ]}
            >
              <div
                className={css(styles.field)}
                aria-labelledby={id}
                data-testid={id}
              >
                <Split>
                  <SplitItem isFilled>
                    <Title size={"sm"} id={id} className={css(styles.title)}>
                      {canShowIndex && (
                        <Tooltip
                          position={"auto"}
                          enableFlip={true}
                          entryDelay={750}
                          exitDelay={100}
                          content={
                            <div>
                              Edit the index for this element by selecting the
                              arrows or by dragging and dropping the element to
                              the desired position. Placeholders may be
                              automatically inserted to account for any gaps in
                              the indexing.
                            </div>
                          }
                        >
                          <InputGroup className={css(styles.fieldIndex)}>
                            <InputGroupText>#</InputGroupText>
                            <TextInput
                              type={"number"}
                              value={index}
                              id={"index"}
                              onKeyDown={onKeyDown}
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
                        entryDelay={750}
                        exitDelay={100}
                        content={<div>{info}</div>}
                      >
                        <div className={css(styles.fieldName)}>
                          {name} <InfoAltIcon />
                        </div>
                      </Tooltip>
                    </Title>
                  </SplitItem>
                  {!mappingExpressionEnabled && onNewTransformation && (
                    <SplitItem>
                      <Tooltip
                        position={"auto"}
                        enableFlip={true}
                        entryDelay={750}
                        exitDelay={100}
                        content={"Add a new transformation."}
                      >
                        <Button
                          variant={"plain"}
                          onClick={onNewTransformation}
                          className={css(styles.link)}
                          data-testid={`add-transformation-to-${name}-field-button`}
                        >
                          <BoltIcon />
                        </Button>
                      </Tooltip>
                    </SplitItem>
                  )}
                  <SplitItem>
                    <Tooltip
                      position={"auto"}
                      enableFlip={true}
                      entryDelay={750}
                      exitDelay={100}
                      content={"Delete this field from the mapping."}
                    >
                      <Button
                        variant={"plain"}
                        onClick={onDelete}
                        className={css(styles.link)}
                        data-testid={`remove-${name}-from-mapping-button`}
                      >
                        <TrashIcon />
                      </Button>
                    </Tooltip>
                  </SplitItem>
                </Split>
                {/*
                  Show established field action transformations associated with this
                  field.
                  */}
                {!mappingExpressionEnabled &&
                  hasTransformations &&
                  children &&
                  Children.count(children) > 0 && (
                    <div
                      className={css(
                        "pf-c-form",
                        styles.transformationsWrapper,
                      )}
                    >
                      <Title size={"xs"}>Transformations</Title>
                      <div className={css(styles.transformations)}>
                        {children}
                      </div>
                    </div>
                  )}
              </div>
            </NodeRef>
          )}
        </DraggableField>
      )}
    </FieldDropTarget>
  );
};
