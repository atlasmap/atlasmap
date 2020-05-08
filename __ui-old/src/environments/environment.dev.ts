// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `.angular-cli.json`.
import { NgxLoggerLevel } from 'ngx-logger';

export const environment = {
  production: false,
  ngxLoggerConfig: {
    level: NgxLoggerLevel.DEBUG,
    disableConsoleLogging: false
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
