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

import { Component, Input } from '@angular/core';

@Component({
    selector: 'collapsable-header',
    template: `
        <div class="CollapsableCardHeader" (click)="handleMouseClick($event)">
            <h2 class="card-pf-title"><i [attr.class]="getCSSClass()"></i>{{ title }}</h2>
        </div>
    `
})

export class CollapsableHeaderComponent {
    @Input() title: string;
    @Input() collapsed: boolean = false;

    public handleMouseClick(event: MouseEvent): void {
        this.collapsed = !this.collapsed;
    }

    public getCSSClass() {
        return "arrow fa fa-angle-" + (this.collapsed ? "right" : "down");
    }
}
