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
    const expression = this.configModel.mappings.activeMapping.getCurrentFieldMapping().transition.expression;
    if (!expression) {
      return '';
    }
    // TODO show field name instead and put a full path as a tooltip
    return expression.replace(/\$\{[0-9]+\}/g, '<span class="inline-block label label-default">$&</span>');
  }

}
