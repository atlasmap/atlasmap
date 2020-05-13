import React, { FunctionComponent } from "react";

import { NodeRef } from "../../../UI";
import { AtlasmapFields, IAtlasmapField, IAtlasmapGroup } from "../../../Views";
import {
  ITreeItemFieldAndNodeRefsAndDnDProps,
  TreeItemWithFieldAndNodeRefsAndDnD,
} from "./TreeItemFieldAndNodeRefsAndDnD";
import {
  ITreeGroupAndNodeRefsAndDnDProps,
  TreeGroupAndNodeRefsAndDnD,
} from "./TreeGroupAndNodeRefsAndDnD";

function getChildrenIds(
  fields: AtlasmapFields,
  idPrefix: string,
): (string | undefined)[] {
  return fields.reduce<(string | undefined)[]>(
    (ids, f) => [
      ...ids,
      `${idPrefix}${f.id}`,
      ...((f as IAtlasmapGroup).fields
        ? getChildrenIds((f as IAtlasmapGroup).fields, idPrefix)
        : []),
    ],
    [],
  );
}

export interface ITraverseFieldsProps
  extends Omit<Omit<IFieldOrGroupProps, "field">, "fieldId"> {
  fields: AtlasmapFields;
}

export const TraverseFields: FunctionComponent<ITraverseFieldsProps> = ({
  fields,
  idPrefix,
  isVisible = true,
  ...props
}) => {
  return isVisible ? (
    <>
      {fields.map((field, idx) => (
        <FieldOrGroup
          key={idx}
          field={field}
          idPrefix={idPrefix}
          setSize={fields.length}
          position={idx + 1}
          isVisible={isVisible}
          {...props}
        />
      ))}
    </>
  ) : (
    <NodeRef
      id={getChildrenIds(fields, idPrefix)}
      boundaryId={props.boundaryId}
      parentId={props.parentId}
    >
      <div>&nbsp;</div>
    </NodeRef>
  );
};

export interface IFieldOrGroupProps
  extends Omit<Omit<ITreeItemFieldAndNodeRefsAndDnDProps, "field">, "fieldId">,
    Omit<
      Omit<Omit<ITreeGroupAndNodeRefsAndDnDProps, "group">, "fieldId">,
      "children"
    > {
  idPrefix: string;
  field: IAtlasmapGroup | IAtlasmapField;
  isVisible?: boolean;
}

const FieldOrGroup: FunctionComponent<IFieldOrGroupProps> = ({
  field,
  idPrefix,
  level = 1,
  ...props
}) => {
  const fieldId = `${idPrefix}${field.id}`;
  const maybeGroup = field as IAtlasmapGroup;
  const maybeField = field as IAtlasmapField;
  if (maybeGroup.fields) {
    return (
      <TreeGroupAndNodeRefsAndDnD
        fieldId={fieldId}
        group={maybeGroup}
        level={level}
        {...props}
      >
        {({ expanded }) => (
          <TraverseFields
            {
              ...props /* spreading the props must be done before everything else so to override the values fed to the Group */
            }
            fields={maybeGroup.fields as AtlasmapFields}
            parentId={fieldId}
            level={level + 1}
            idPrefix={idPrefix}
            isVisible={expanded}
          />
        )}
      </TreeGroupAndNodeRefsAndDnD>
    );
  }
  return (
    <TreeItemWithFieldAndNodeRefsAndDnD
      fieldId={fieldId}
      field={maybeField}
      level={level}
      {...props}
    />
  );
};
