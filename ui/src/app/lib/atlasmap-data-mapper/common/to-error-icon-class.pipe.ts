import { Pipe, PipeTransform } from '@angular/core';
import { ErrorInfo, ErrorLevel } from '../models/error.model';

@Pipe({ name: 'toErrorIconClass' })
export class ToErrorIconClassPipe implements PipeTransform {

  transform(value: ErrorInfo[]): string {
    if (value.some(e => e.level >= ErrorLevel.ERROR)) {
      return 'pficon pficon-error-circle-o';
    } else if (value.some(e => e.level === ErrorLevel.WARN)) {
      return 'pficon pficon-warning-triangle-o';
    } else {
      return '';
    }
  }

}
