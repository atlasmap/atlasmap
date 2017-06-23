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

import { Component, OnInit, Input, ViewChild, ViewChildren, DoCheck, QueryList,
    ViewContainerRef, Directive, Type, ComponentFactoryResolver, AfterViewInit,
    SimpleChange, Inject, ChangeDetectorRef} from '@angular/core';

// source: http://www.w3schools.com/howto/howto_css_modals.asp


@Component({
    selector: 'empty-modal-body',
    template: ""
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
                        {{headerText}}
                    </div>
                    <div class="modal-body">
                        <div class="modal-message" *ngIf="message">{{message}}</div>
                        <template #dyn_target></template>
                    </div>
                    <div class="modal-footer">
                        <div class="modal-buttons">
                            <button class="pull-right btn btn-primary" (click)="buttonClicked(true)">{{confirmButtonText}}</button>
                            <button class="pull-right btn btn-cancel" (click)="buttonClicked(false)">Cancel</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `
})

export class ModalWindowComponent implements AfterViewInit {
    @Input() headerText: string = "";
    @Input() parentComponent: Component;
    @Input() nestedComponentType: Type<any>;
    @Input() nestedComponentInitializedCallback: Function;
    @Input() okButtonHandler: Function;
    @Input() cancelButtonHandler: Function;

    public message: string = null;
    public nestedComponent: Component;
    public confirmButtonText: string = "OK";

    private componentLoaded: boolean = false;
    public visible: boolean = false;

    @ViewChildren('dyn_target', {read: ViewContainerRef}) myTarget: QueryList<ViewContainerRef>;

    constructor(private componentFactoryResolver: ComponentFactoryResolver, public detector: ChangeDetectorRef) { }

    ngAfterViewInit() {
        //from: http://stackoverflow.com/questions/40811809/add-component-dynamically-inside-an-ngif
        this.myTarget.changes.subscribe(changes => {
            setTimeout(() => {
                if (!this.componentLoaded && this.visible && this.myTarget && (this.myTarget.toArray().length)) {
                    this.loadComponent()
                }
                setTimeout(() => {
                    this.detector.detectChanges();
                }, 10);
            }, 10);
        });
    }

    public loadComponent(): void {
        var viewContainerRef: ViewContainerRef = this.myTarget.toArray()[0];
        viewContainerRef.clear();
        let componentFactory = this.componentFactoryResolver.resolveComponentFactory(this.nestedComponentType);
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

    private buttonClicked(okClicked: boolean): void {
        if (okClicked) {
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

    public reset(): void {
        this.nestedComponentInitializedCallback = null;
        this.confirmButtonText = "OK";
        this.message = "";
        this.headerText = "";
        this.parentComponent = null;
        this.componentLoaded = false;
        this.nestedComponentType = EmptyModalBodyComponent;
        this.okButtonHandler = null;
        this.cancelButtonHandler = null;
    }
}
