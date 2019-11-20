/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';
import { ErrorHandlerService } from '../src/services/error-handler.service';
import { ErrorInfo, ErrorScope, ErrorType, ErrorLevel } from '../src/models/error.model';
import { MappingModel } from '../src/models/mapping.model';
import { CompileTemplateMetadata } from '@angular/compiler';

describe('ErrorHandlerService', () => {
  beforeEach(() => {
    TestBed.resetTestEnvironment();
    TestBed.initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting());
    TestBed.configureTestingModule({
      providers: [ErrorHandlerService],
    });
  });

  afterEach(() => {
    const service = TestBed.get(ErrorHandlerService);
    service.resetAll();
  });

  it('should support filterWith()', () => {
    const mapping1 = new MappingModel();
    const mapping2 = new MappingModel();
    const errors = [
      new ErrorInfo({
        message: 'app', scope: ErrorScope.APPLICATION, type: ErrorType.USER,
        level: ErrorLevel.ERROR
      }),
      new ErrorInfo({
        message: 'mapping1', mapping: mapping1, scope: ErrorScope.MAPPING,
        type: ErrorType.VALIDATION, level: ErrorLevel.ERROR
      }),
      new ErrorInfo({
        message: 'mapping2', mapping: mapping2, scope: ErrorScope.MAPPING,
        type: ErrorType.VALIDATION, level: ErrorLevel.WARN
      })
    ];
    const noFilter = ErrorHandlerService.filterWith(errors);
    expect(noFilter.length).toEqual(1);
    expect(noFilter[0].message).toEqual('app');
    const m1 = ErrorHandlerService.filterWith(errors, mapping1);
    expect(m1.length).toEqual(2);
    expect(m1[0].message).toEqual('app');
    expect(m1[1].message).toEqual('mapping1');
    const m2 = ErrorHandlerService.filterWith(errors, mapping2);
    expect(m2.length).toEqual(2);
    expect(m2[0].message).toEqual('app');
    expect(m2[1].message).toEqual('mapping2');
    const m2Level = ErrorHandlerService.filterWith(errors, mapping2, ErrorLevel.ERROR);
    expect(m2Level.length).toEqual(1);
    expect(m2Level[0].message).toEqual('app');
  }
  );

  it('should support addError() and removeError()',
    inject([ErrorHandlerService], (service: ErrorHandlerService) => {
      expect(service.getErrors().length).toEqual(0);
      service.addError(new ErrorInfo({message: 'test'}));
      const errors = service.getErrors();
      expect(errors.length).toEqual(1);
      expect(errors[0].message).toEqual('test');
      service.removeError(errors[0].identifier);
      expect(service.getErrors().length).toEqual(0);
    })
  );

  it('should support clearAllErrors()',
    inject([ErrorHandlerService], (service: ErrorHandlerService) => {
      expect(service.getErrors().length).toEqual(0);
      service.addError(new ErrorInfo({message: 'cae'}));
      service.addError(new ErrorInfo({message: 'cae2'}));
      const errors = service.getErrors();
      expect(errors.length).toEqual(2);
      expect(errors[0].message).toEqual('cae2');
      expect(errors[1].message).toEqual('cae');
      service.clearAllErrors();
      service.removeError(errors[0].identifier);
      expect(service.getErrors().length).toEqual(0);
    })
  );

  it('should support clearPreviewErrors()',
    inject([ErrorHandlerService], (service: ErrorHandlerService) => {
      expect(service.getErrors().length).toEqual(0);
      service.addError(
        new ErrorInfo({message: 'cpe'}),
        new ErrorInfo({message: 'cpe2', type: ErrorType.PREVIEW}),
        new ErrorInfo({message: 'cpe3', type: ErrorType.USER}));
      let errors = service.getErrors();
      expect(errors.length).toEqual(3);
      expect(errors[0].message).toEqual('cpe3');
      expect(errors[1].message).toEqual('cpe2');
      expect(errors[2].message).toEqual('cpe');
      service.clearPreviewErrors();
      errors = service.getErrors();
      expect(errors.length).toEqual(2);
      expect(errors[0].message).toEqual('cpe3');
      expect(errors[1].message).toEqual('cpe');
    })
  );

  it('should support clearValidationErrors()',
    inject([ErrorHandlerService], (service: ErrorHandlerService) => {
      expect(service.getErrors().length).toEqual(0);
      service.addError(
        new ErrorInfo({message: 'cve'}),
        new ErrorInfo({message: 'cve2', type: ErrorType.VALIDATION}),
        new ErrorInfo({message: 'cve3', type: ErrorType.PREVIEW}));
      let errors = service.getErrors();
      expect(errors.length).toEqual(3);
      expect(errors[0].message).toEqual('cve3');
      expect(errors[1].message).toEqual('cve2');
      expect(errors[2].message).toEqual('cve');
      service.clearValidationErrors();
      errors = service.getErrors();
      expect(errors.length).toEqual(2);
      expect(errors[0].message).toEqual('cve3');
      expect(errors[1].message).toEqual('cve');
    })
  );

  it('should support clearFieldErrors()',
    inject([ErrorHandlerService], (service: ErrorHandlerService) => {
      expect(service.getErrors().length).toEqual(0);
      service.addError(
        new ErrorInfo({message: 'cfe'}),
        new ErrorInfo({message: 'cfe2', scope: ErrorScope.FIELD}),
        new ErrorInfo({message: 'cfe3', scope: ErrorScope.MAPPING}));
      let errors = service.getErrors();
      expect(errors.length).toEqual(3);
      expect(errors[0].message).toEqual('cfe3');
      expect(errors[1].message).toEqual('cfe2');
      expect(errors[2].message).toEqual('cfe');
      service.clearFieldErrors();
      errors = service.getErrors();
      expect(errors.length).toEqual(2);
      expect(errors[0].message).toEqual('cfe3');
      expect(errors[1].message).toEqual('cfe');
    })
  );

  it('should support async subscription', (done) => {
    inject([ErrorHandlerService], (service: ErrorHandlerService) => {
      const subscription = service.subscribe((errors) => {
        expect(errors.length).toEqual(1);
        expect(errors[0].message).toEqual('async');
        subscription.unsubscribe();
        done();
      });
      service.addError(new ErrorInfo({message: 'async'}));
    })();
  });

  it('should support form error async subscription', (done) => {
    inject([ErrorHandlerService], (service: ErrorHandlerService) => {
      const subject = service.createFormErrorChannel();
      subject.subscribe((errors) => {
        expect(errors.length).toEqual(1);
        expect(errors[0].message).toEqual('form async is required.');
        expect(errors[0].level).toEqual(ErrorLevel.ERROR);
        expect(errors[0].scope).toEqual(ErrorScope.FORM);
        subject.complete();
        done();
      });
      service.isRequiredFieldValid(null, 'form async');
    })();
  });

});
