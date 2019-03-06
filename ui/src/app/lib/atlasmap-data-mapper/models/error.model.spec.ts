/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { ErrorInfo, ErrorLevel } from './error.model';

describe('ErrorModel', () => {
  it('should ...', () => {
    const ei: ErrorInfo = new ErrorInfo('test error message', ErrorLevel.ERROR, null);
    expect(ei.error).toBeNull();
    expect(ei.identifier).toBeGreaterThanOrEqual(0);
    expect(ei.level).toBe(ErrorLevel.ERROR);
    expect(ei.message).toBe('test error message');
  });
});
