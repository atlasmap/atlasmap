import { NgxLoggerLevel } from 'ngx-logger';

export const environment = {
  production: true,
  ngxLoggerConfig: {
    level: NgxLoggerLevel.OFF,
    disableConsoleLogging: true
  },
  xsrf: {
    headerName: 'ATLASMAP-XSRF-TOKEN',
    cookieName: 'ATLASMAP-XSRF-COOKIE',
    defaultTokenValue: 'awesome',
  },
  backendUrls: {
    atlasServiceUrl: 'http://localhost:8585/v2/atlas/',
    javaInspectionServiceUrl: 'http://localhost:8585/v2/atlas/java/',
    xmlInspectionServiceUrl: 'http://localhost:8585/v2/atlas/xml/',
    jsonInspectionServiceUrl: 'http://localhost:8585/v2/atlas/json/',
  },
};
