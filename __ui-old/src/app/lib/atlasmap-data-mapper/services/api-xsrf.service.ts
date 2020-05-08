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

/**
 * Override the stock @angular HTTP package token extractor to *not* fetch cookies from
 * the Window document.  Return a default token value instead.
 */
@Injectable()
export class ApiHttpXsrfTokenExtractor implements HttpXsrfTokenExtractor {
  private cfg = ConfigModel.getConfig();
  getToken(): string {
    return this.cfg.initCfg.xsrfDefaultTokenValue;
  }
}

/**
 * Override the stock @angular HTTP package request interceptor method to wrap the request
 * with default XSRF header name and token value.
 */
@Injectable()
export class ApiXsrfInterceptor implements HttpInterceptor {

  private cfg = ConfigModel.getConfig();
  intercept(httpRequest: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    /*
      ADM doesn't need/use cookies so we'll use the default token value initialized from the environment when
      adding the XSRF header to HTTP requests.  This will avoid the need to read cookies from local storage.
    */
    if (this.cfg.initCfg.xsrfHeaderName) {
      // const token = this.tokenExtractor.getToken() || this.cfg.initCfg.xsrfDefaultTokenValue;
      const token = this.cfg.initCfg.xsrfDefaultTokenValue;
      const headerName = this.cfg.initCfg.xsrfHeaderName;

      if (!httpRequest.headers.has(headerName)) {
        httpRequest = httpRequest.clone({ headers: httpRequest.headers.set(headerName, token) });
      }
    }
    return next.handle(httpRequest);
  }
}
