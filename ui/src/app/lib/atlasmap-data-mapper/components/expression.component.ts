/*
Copyright (C) 2017 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
import { Component, ViewChild, Input, HostListener, ElementRef } from '@angular/core';
import { ConfigModel } from '../models/config.model';
import { MappedField } from '../models/mapping.model';

@Component({
  selector: 'expression',
  templateUrl: 'expression.component.html'
})

export class ExpressionComponent {

  @Input()
  configModel: ConfigModel;

  @ViewChild('expressionTextareaRef')
  textarea: ElementRef;

  @HostListener('click')
  onClick() {
    this.textarea.nativeElement.focus();
  }

  @HostListener('keyup', ['$event'])
  onkeyup(event: KeyboardEvent) {
    // TODO handle deleting field refs... just one [backspace] should delete a ref entirely, but not only a closing bracket
    // TODO show a cursor
    // TODO text selection
  }

  generateExpressionMarkup(): string {
    const pair = this.configModel.mappings.activeMapping.getCurrentFieldMapping();
    const expression = pair.transition.expression;
    if (!expression) {
      return '';
    }

    const mappedSourceFields = pair.getMappedFields(true);
    return expression.replace(/\$\{[0-9]+\}/g, (match) => {
      const index = parseInt(match.substring(2, match.length - 1), 10);
      let field: MappedField;
      if (mappedSourceFields.length === 1 && index === 0) {
        field = mappedSourceFields[0];
      } else if (mappedSourceFields.length > 1) {
        field = mappedSourceFields.find(f => {
          return f.getFieldIndex() != null && (index + 1) === +f.getFieldIndex();
        });
      }
      if (field) {
        return `<span title="${field.field.docDef.name}:${field.field.path}"
          class="inline-block label label-default">${field.field.name}</span>`;
      } else {
        return `<span title="N/A" class="inline-block label label-danger">${match}</span>`;
      }
    });
  }

}
