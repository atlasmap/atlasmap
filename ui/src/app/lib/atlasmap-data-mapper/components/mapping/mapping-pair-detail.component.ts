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

import { Component, Input, ViewChild } from '@angular/core';

import { ConfigModel } from '../../models/config.model';
import { FieldMappingPair } from '../../models/mapping.model';

import { CollapsableHeaderComponent } from '../collapsable-header.component';
import { ModalWindowComponent } from '../modal-window.component';

@Component({
  selector: 'mapping-pair-detail',
  templateUrl: './mapping-pair-detail.component.html',
})

export class MappingPairDetailComponent {
  @Input() cfg: ConfigModel;
  @Input() fieldPair: FieldMappingPair;
  @Input() modalWindow: ModalWindowComponent;

  @ViewChild('sourcesHeader')
  sourcesHeader: CollapsableHeaderComponent;
  @ViewChild('actionsHeader')
  actionsHeader: CollapsableHeaderComponent;
  @ViewChild('targetsHeader')
  targetsHeader: CollapsableHeaderComponent;
}
