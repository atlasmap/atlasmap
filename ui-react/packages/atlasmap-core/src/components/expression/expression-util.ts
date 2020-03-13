import { ConfigModel } from '../../models/config.model';
import { ExpressionModel } from '../../models/expression.model';
import { MappedField, MappingModel } from '../../models/mapping.model';

const trailerId = 'expression-trailer';

function activeMapping(): boolean {
  const cfg = ConfigModel.getConfig();
  return !(!cfg || !cfg.mappings || !cfg.mappings.activeMapping);
}

function updateExpressionMarkup() {
  // this.markup.nativeElement.innerHTML = getExpression()!.toHTML()
  //  + `<span id="${trailerId}">&nbsp;</span>`;
}

function moveCaretToEnd() {
  /*
  const trailerNode = this.markup.nativeElement.querySelector('#' +
    trailerId);
  this.markup.nativeElement.focus();
  let range;
  if (window.getSelection()!.rangeCount > 0) {
    range = window.getSelection()!.getRangeAt(0);
  } else {
    range = document.createRange();
    window.getSelection()!.addRange(range);
  }
  range.selectNode(trailerNode.childNodes[0]);
  range.setStart(trailerNode.childNodes[0], 0);
  range.collapse(true);
  */
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
  static readonly PREFIX = 'expression-text-';
  constructor(public str: string) {
    super(TextNode.PREFIX);
  }
  toText(): string {
    return this.str;
  }
  toHTML(): string {
    return `<span id="${this.uuid}">${this.str.replace(/ /g, '&nbsp;')}</span>`;
  }
}

export class FieldNode extends ExpressionNode {
  protected field: MappedField | null;
  static readonly PREFIX = 'expression-field-';

  constructor(
    private mapping: MappingModel,
    public mfield?: MappedField,
    private index?: number)
  {
    super(FieldNode.PREFIX);
    if (!index) {
      index = 0;
    }
    if (!mfield) {
      this.field = mapping.getMappedFieldForIndex((index + 1).toString(), true);
    }
  }

  toText(): string {
    if (!this.mapping || !this.field) {
      return '';
    }
    return '${' + (this.mapping.getIndexForMappedField(this.field)! - 1) + '}';
  }

  toHTML(): string {
    if (!this.field) {
      return '';
    }
    if (this.field && this.field.field) {
      return `<span contenteditable="false" id="${this.uuid}" title="${this.field.field.docDef.name}:${this.field.field.path}"
        class="expressionFieldLabel label label-default">${this.field.field.name}</span>`;
    } else {
      return `<span contenteditable="false" id="${this.uuid}"
        title="Field index '${this.mapping.getIndexForMappedField(this.field)! - 1}' is not available"
        class="expressionFieldLabel label label-danger">N/A</span>`;
    }
  }
}

export function getExpression(): ExpressionModel | null {
  const cfg = ConfigModel.getConfig();
  if (!activeMapping()) {
    return null;
  }
  const mapping = cfg.mappings!.activeMapping;
  let expression = mapping!.transition.expression;
  if (!expression) {
    mapping!.transition.expression = 
      new ExpressionModel(mapping!, cfg);
    expression = mapping!.transition.expression;
    expression.generateInitialExpression();
    expression.updateFieldReference(mapping!);
    updateExpressionMarkup();
    moveCaretToEnd();
  } else {
    mapping!.transition.expression.setConfigModel(cfg);
  }
  return mapping!.transition.expression;
}

export function getExpressionStr(): string {
  const cfg = ConfigModel.getConfig();
  if (!activeMapping() || !cfg.mappings!.activeMapping!.transition || 
    !cfg.mappings!.activeMapping!.transition.expression) {
    getExpression();
  }
  return cfg.mappings!.activeMapping!.transition.expression.toText();
}
