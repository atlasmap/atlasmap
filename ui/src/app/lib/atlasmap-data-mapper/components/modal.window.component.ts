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

import { Component, Input, ViewChildren, QueryList,
    ViewContainerRef, Type, ComponentFactoryResolver, AfterViewInit, ChangeDetectorRef } from '@angular/core';
import { ConfigModel } from '../models/config.model';

// source: http://www.w3schools.com/howto/howto_css_modals.asp

export interface ModalWindowValidator {
    isDataValid(): boolean;
}

@Component({
    selector: 'empty-modal-body',
    template: '',
})

export class EmptyModalBodyComponent { }

@Component({
    selector: 'modal-window',
    template: `
        <div id="modalWindow" [attr.class]="visible ? 'modal fade in' : 'modal fade dm-out'" *ngIf="visible">
            <div class="modalWindow">
                <div class="modal-content">
                    <div class="modal-header">
                        <a (click)="closeClicked($event)"><span class='close'><i class="fa fa-close"></i></span></a>
                        {{ headerText }}
                    </div>
                    <div class="modal-error">
                        <data-mapper-error [isValidation]="true" [errorService]="cfg.errorService"></data-mapper-error>
                    </div>
                    <div class="modal-body">
                        <div class="modal-message" *ngIf="message">{{ message }}</div>
                        <template #dyn_target></template>
                    </div>
                    <div class="modal-footer">
                        <div class="modal-buttons">
                            <button class="pull-right btn btn-primary" (click)="buttonClicked(true)">{{ confirmButtonText }}</button>
                            <button class="pull-right btn btn-cancel" (click)="buttonClicked(false)">Cancel</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
})

export class ModalWindowComponent implements AfterViewInit {
    @Input() headerText = '';
    @Input() nestedComponentType: Type<any>;
    @Input() nestedComponentInitializedCallback: Function;
    @Input() okButtonHandler: Function;
    @Input() cancelButtonHandler: Function;
    @Input() cfg: ConfigModel;

    public message: string = null;
    public nestedComponent: Component;
    public confirmButtonText = 'OK';
    public visible = false;

    @ViewChildren('dyn_target', {read: ViewContainerRef}) myTarget: QueryList<ViewContainerRef>;

    private componentLoaded = false;

    constructor(private componentFactoryResolver: ComponentFactoryResolver, public detector: ChangeDetectorRef) { }

    ngAfterViewInit() {
        //from: http://stackoverflow.com/questions/40811809/add-component-dynamically-inside-an-ngif
        this.myTarget.changes.subscribe(changes => {
            setTimeout(() => {
                if (!this.componentLoaded && this.visible && this.myTarget && (this.myTarget.toArray().length)) {
                    this.loadComponent();
                }
                setTimeout(() => {
                    this.detector.detectChanges();
                }, 10);
            }, 10);
        });
    }

    public loadComponent(): void {
        const viewContainerRef: ViewContainerRef = this.myTarget.toArray()[0];
        viewContainerRef.clear();
        const componentFactory = this.componentFactoryResolver.resolveComponentFactory(this.nestedComponentType);
        this.nestedComponent = viewContainerRef.createComponent(componentFactory).instance;
        if (this.nestedComponentInitializedCallback != null) {
            this.nestedComponentInitializedCallback(this);
        }
    }

    public closeClicked(event: MouseEvent): void { this.buttonClicked(false); }
    public close(): void { this.visible = false; }
    public show(): void {
        this.visible = true;
    }

    public reset(): void {
        this.cfg.errorService.clearValidationErrors();
        this.nestedComponentInitializedCallback = null;
        this.confirmButtonText = 'OK';
        this.message = '';
        this.headerText = '';
        this.componentLoaded = false;
        this.nestedComponentType = EmptyModalBodyComponent;
        this.okButtonHandler = null;
        this.cancelButtonHandler = null;
    }

    private buttonClicked(okClicked: boolean): void {
        if (okClicked) {
            const anyComponent: any = this.nestedComponent;
            if ((anyComponent != null) && (anyComponent.isDataValid)) {
                this.cfg.errorService.clearValidationErrors();
                if (!(anyComponent.isDataValid())) {
                    return;
                }
            }
            if (this.okButtonHandler) {
                this.okButtonHandler(this);
            }
        } else { // cancel clicked
            if (this.cancelButtonHandler) {
                this.cancelButtonHandler(this);
            }
        }
        this.close();
    }

}
