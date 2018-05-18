import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpInterceptor,
  HttpHandler,
  HttpRequest,
  HttpXsrfTokenExtractor
} from '@angular/common/http';
import { Observable } from 'rxjs';

import { ConfigModel } from '../models/config.model';

@Injectable()
export class ApiXsrfInterceptor implements HttpInterceptor {
  private cfg = ConfigModel.getConfig();

  constructor(private tokenExtractor: HttpXsrfTokenExtractor) { }

  intercept(httpRequest: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (this.cfg.initCfg.xsrfHeaderName && httpRequest.url.startsWith('http')) {
      const token = this.tokenExtractor.getToken() || this.cfg.initCfg.xsrfDefaultTokenValue;
      const headerName = this.cfg.initCfg.xsrfHeaderName;

      if (!httpRequest.headers.has(headerName)) {
        httpRequest = httpRequest.clone({ headers: httpRequest.headers.set(headerName, token) });
      }
    }

    return next.handle(httpRequest);
  }
}
