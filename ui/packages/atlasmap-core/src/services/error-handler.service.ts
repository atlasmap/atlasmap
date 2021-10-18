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

import {
  ErrorInfo,
  ErrorLevel,
  ErrorScope,
  ErrorType,
} from '../models/error.model';
import { Subject, Subscription } from 'rxjs';
import { MappingModel } from '../models/mapping.model';

/**
 * ErrorHandlerService handles global errors, mapping validation errors, preview errors,
 * mapped field level errors as well as instant form validation errors in a modal windows.
 * Global errors, mapping validation errors, preview errors and mapped field level errors
 * are stored in a same array at this moment. We might want to split them when we show
 * them grouped by {@link ErrorType}.
 * Errors with {@link ErrorScope.MAPPING} and {@link ErrorScope.FIELD} are cleared
 * everytime active mapping is switched. {@link ErrorScope.FIELD} errors are mostly
 * instant and cleared more frequently.
 * Form validation errors are supposed to be instant. Channel should be created by
 * {@link createFormErrorChannel()} when modal window is initialized, and should be
 * completed when modal window is closed.
 */
export class ErrorHandlerService {
  private errors: ErrorInfo[] = [];
  private formErrors: ErrorInfo[] = [];
  private errorUpdatedSource = new Subject<ErrorInfo[]>();
  private formErrorUpdatedSource: Subject<ErrorInfo[]> | undefined;

  /**
   * FIlter an array of {@link ErrorInfo} with specified condition.
   * @param errors An array of {@link ErrorInfo} to filter
   * @param mapping {@link MappingModel} to filter {@link ErrorScope.MAPPING} errors
   * @param level {@link ErrorLevel} to filter with
   */
  static filterWith(
    errors: ErrorInfo[],
    mapping?: MappingModel,
    level?: ErrorLevel
  ): ErrorInfo[] {
    if (!errors || errors.length === 0) {
      return [];
    }
    return errors.filter(
      (e) =>
        (!e.mapping || (mapping && e.mapping === mapping)) &&
        (!level || !e.level || e.level === level)
    );
  }

  /**
   * Add one or more {@link ErrorInfo} object(s) into error store.
   * @param errors one or more {@link ErrorInfo} object(s)
   */
  addError(...errors: ErrorInfo[]): void {
    errors.forEach((error) => {
      if (error.object && error.object.message) {
        // TODO show error.object in more polished way... maybe with better error console
        error.message += '\n' + error.object.message;
      }
      const store =
        ErrorScope.FORM === error.scope ? this.formErrors : this.errors;

      // Remove identical error/warning messages - not info messages.
      if (
        store.find((e) => e.message === error.message) &&
        error.level !== ErrorLevel.INFO
      ) {
        return;
      }
      store.unshift(error);
    });
    this.emitUpdatedEvent();
  }

  /**
   * An utility method to add backend error. It's a network error if {@link error.status} is 0.
   *
   * @param message error message to put if it's not a network error
   * @param error raw error object
   */
  addBackendError(message: string, error?: any): void {
    if (error?.status === 0) {
      this.addError(
        new ErrorInfo({
          message:
            'Fatal network error: Unable to connect to the AtlasMap design runtime service.',
          level: ErrorLevel.ERROR,
          scope: ErrorScope.APPLICATION,
          type: ErrorType.INTERNAL,
          object: error,
        })
      );
    } else {
      this.addError(
        new ErrorInfo({
          message: message,
          level: ErrorLevel.ERROR,
          scope: ErrorScope.APPLICATION,
          type: ErrorType.INTERNAL,
          object: error,
        })
      );
    }
  }

  /**
   * Return all errors in the store.
   * @return An array of {@link ErrorInfo}
   */
  getErrors(): ErrorInfo[] {
    return Object.assign([], this.errors);
  }

  /**
   * Remove one {@link ErrorInfo} by specifying ID.
   * @param identifier Error ID
   */
  removeError(identifier: string, scope?: ErrorScope): void {
    if (
      scope === ErrorScope.FORM &&
      this.formErrorUpdatedSource &&
      !this.formErrorUpdatedSource.closed
    ) {
      this.formErrors = this.excludeByIdentifier(this.formErrors, identifier);
    } else {
      this.errors = this.excludeByIdentifier(this.errors, identifier);
    }
    this.emitUpdatedEvent();
  }

  /**
   * Clear all global/mapping errors as well as form validation erros and its Subject
   * if it exists.
   */
  resetAll(): void {
    this.clearAllErrors();
    this.formErrors = [];
    if (this.formErrorUpdatedSource && !this.formErrorUpdatedSource.closed) {
      this.formErrorUpdatedSource.complete();
    }
  }

  /**
   * Remova all errors except form validation errors.
   */
  clearAllErrors() {
    this.errors = [];
    this.emitUpdatedEvent();
  }

  /**
   * Remove all preview errors.
   */
  clearPreviewErrors(): void {
    this.errors = this.errors.filter((e) => e.type !== ErrorType.PREVIEW);
    this.emitUpdatedEvent();
  }

  /**
   * Remove all mapping validation errors.
   */
  clearValidationErrors(mapping?: MappingModel): void {
    this.errors = this.errors.filter(
      (e) =>
        e.type !== ErrorType.VALIDATION &&
        (!mapping || !e.mapping || e.mapping !== mapping)
    );
    this.emitUpdatedEvent();
  }

  /**
   * Remove all field scoped errors.
   */
  clearFieldErrors() {
    this.errors = this.errors.filter((e) => e.scope !== ErrorScope.FIELD);
    this.emitUpdatedEvent();
  }

  /**
   * Remove all form validation errors.
   */
  clearFormErrors() {
    this.formErrors = [];
    this.emitUpdatedEvent(ErrorScope.FORM);
  }

  /**
   * Subscribe an error updated event. Observer will be notified
   * when an error is added or removed.
   * @param observer Observer
   */
  subscribe(observer: (errors: ErrorInfo[]) => void): Subscription {
    return this.errorUpdatedSource.subscribe(observer);
  }

  /**
   * Create a Subject for form validation error. This ErrorHandlerService assumes only
   * one form validation happens at once, as it's used in modal window. Revisit this if there
   * needs to be more than one channel and manage a list of {@link Subject}.
   */
  createFormErrorChannel(): Subject<ErrorInfo[]> {
    if (this.formErrorUpdatedSource && !this.formErrorUpdatedSource.closed) {
      this.formErrorUpdatedSource.complete();
    }
    this.formErrors = [];
    this.formErrorUpdatedSource = new Subject();
    this.formErrorUpdatedSource.subscribe({
      complete: () => (this.formErrors = []),
    });
    return this.formErrorUpdatedSource;
  }

  /**
   * Validate the specified field value in a form, generating a form validation error if not defined.
   * @param value - A form field to validate
   * @param fieldDescription - used in error diagnostic
   */
  isRequiredFieldValid(
    value: string | null,
    fieldDescription: string
  ): boolean {
    if (value == null || '' === value) {
      const errorMessage: string = fieldDescription + ' is required.';
      this.addError(
        new ErrorInfo({
          message: errorMessage,
          level: ErrorLevel.ERROR,
          scope: ErrorScope.FORM,
        })
      );
      this.emitUpdatedEvent(ErrorScope.FORM);
      return false;
    }
    return true;
  }

  private emitUpdatedEvent(scope?: ErrorScope) {
    if (ErrorScope.FORM === scope) {
      if (this.formErrorUpdatedSource && !this.formErrorUpdatedSource.closed) {
        this.formErrorUpdatedSource.next(this.formErrors);
      }
    } else {
      this.errorUpdatedSource.next(this.errors);
    }
  }

  private excludeByIdentifier(
    errors: ErrorInfo[],
    identifier: string
  ): ErrorInfo[] {
    return errors.filter((e) => e.identifier !== identifier);
  }
}
