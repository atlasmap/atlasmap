import {
  ConfigModel,
  MappedField,
  MappingModel,
  ExpressionModel,
} from "@atlasmap/core";

export const trailerId = "expression-trailer";

function activeMapping(): boolean {
  const cfg = ConfigModel.getConfig();
  return !!cfg?.mappings?.activeMapping;
}

export abstract class ExpressionNode {
  protected static sequence = 0;
  protected uuid: string;
  constructor(prefix: string) {
    this.uuid = prefix + ExpressionNode.sequence++;
  }
  getUuid() {
    return this.uuid;
  }
  abstract toText(): string;
  abstract toHTML(): string;
}

export class TextNode extends ExpressionNode {
  static readonly PREFIX = "expression-text-";
  constructor(public str: string) {
    super(TextNode.PREFIX);
  }
  toText(): string {
    return this.str;
  }
  toHTML(): string {
    return `<span id="${this.uuid}">${this.str.replace(/ /g, "&nbsp;")}</span>`;
  }
}

export class FieldNode extends ExpressionNode {
  protected field: MappedField | null | undefined;
  static readonly PREFIX = "expression-field-";

  constructor(
    private mapping: MappingModel,
    public mfield?: MappedField,
    index?: number,
  ) {
    super(FieldNode.PREFIX);
    if (!mfield && index) {
      this.field = mapping.getMappedFieldForIndex((index + 1).toString(), true);
    }
  }

  toText(): string {
    if (!this.mapping || !this.field) {
      return "";
    }
    return "${" + (this.mapping.getIndexForMappedField(this.field)! - 1) + "}";
  }

  toHTML(): string {
    if (!this.field) {
      return "";
    }
    if (this.field && this.field.field) {
      return `<span contenteditable="false" id="${this.uuid}" title="${this.field.field.docDef.name}:${this.field.field.path}"
        class="expressionFieldLabel label label-default">${this.field.field.name}</span>`;
    } else {
      return `<span contenteditable="false" id="${this.uuid}"
        title="Field index '${
          this.mapping.getIndexForMappedField(this.field)! - 1
        }' is not available"
        class="expressionFieldLabel label label-danger">N/A</span>`;
    }
  }
}

export function getExpression(): ExpressionModel | null | undefined {
  const cfg = ConfigModel.getConfig();
  if (!activeMapping() || !cfg.mappings?.activeMapping) {
    return null;
  }
  const mapping = cfg.mappings!.activeMapping;
  let expression = mapping!.transition.expression;
  if (!expression) {
    mapping!.transition.expression = new ExpressionModel(mapping!, cfg);
    expression = mapping!.transition.expression;
    expression.generateInitialExpression();
    expression.updateFieldReference(mapping!);
  } else {
    if (mapping!.transition.expression) {
      mapping!.transition.expression.setConfigModel(cfg);
    }
  }
  return mapping!.transition.expression;
}

export function getMappingExpression(): string {
  const cfg = ConfigModel.getConfig();
  if (!activeMapping()) {
    return "";
  }
  if (!cfg.mappings?.activeMapping?.transition?.expression) {
    if (!getExpression()) {
      return "";
    }
  }
  return cfg.mappings!.activeMapping!.transition.expression &&
    cfg.mappings!.activeMapping!.transition.enableExpression
    ? cfg.mappings!.activeMapping!.transition.expression.toHTML()
    : "";
}
