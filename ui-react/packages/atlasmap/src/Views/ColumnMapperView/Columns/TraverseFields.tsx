import React, { FunctionComponent } from "react";

import { AtlasmapFields, IAtlasmapField, IAtlasmapGroup } from "../../../Views";
import {
  ITreeItemFieldAndNodeRefsAndDnDProps,
  TreeItemWithFieldAndNodeRefsAndDnD,
} from "./TreeItemFieldAndNodeRefsAndDnD";
import {
  ITreeGroupAndNodeRefsAndDnDProps,
  TreeGroupAndNodeRefsAndDnD,
} from "./TreeGroupAndNodeRefsAndDnD";

export interface ITraverseFields
  extends Omit<Omit<IFieldOrGroupProps, "field">, "fieldId"> {
  fields: AtlasmapFields;
}

export const TraverseFields: FunctionComponent<ITraverseFields> = ({
  fields,
  idPrefix,
  ...props
}) => {
  return (
    <>
      {fields.map((field, idx) => (
        <FieldOrGroup
          key={idx}
          field={field}
          idPrefix={idPrefix}
          setSize={fields.length}
          position={idx + 1}
          {...props}
        />
      ))}
    </>
  );
};

export interface IFieldOrGroupProps
  extends Omit<Omit<ITreeItemFieldAndNodeRefsAndDnDProps, "field">, "fieldId">,
    Omit<Omit<ITreeGroupAndNodeRefsAndDnDProps, "group">, "fieldId"> {
  idPrefix: string;
  field: IAtlasmapGroup | IAtlasmapField;
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
        <TraverseFields
          {
            ...props /* spreading the props must be done before everything else so to override the values fed to the Group */
          }
          fields={maybeGroup.fields as AtlasmapFields}
          parentId={fieldId}
          level={level + 1}
          idPrefix={idPrefix}
        />
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
