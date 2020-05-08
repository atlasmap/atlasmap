/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { ErrorInfo, ErrorLevel } from '../src/models/error.model';

describe('ErrorModel', () => {
  it('should ...', () => {
    const ei: ErrorInfo = new ErrorInfo({message: 'test error message', level: ErrorLevel.ERROR, object: null});
    expect(ei.object).toBeNull();
    expect(ei.identifier).toBeGreaterThanOrEqual(0);
    expect(ei.level).toBe(ErrorLevel.ERROR);
    expect(ei.message).toBe('test error message');
  });
});
