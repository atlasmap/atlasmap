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

import { ConfigModel } from '../models/config.model';
import { Field } from '../models/field.model';

const MAX_SEARCH_MATCH = 10000;

function markChildrenVisible(field: Field): void {
  field.visibleInCurrentDocumentSearch = true;
  field.collapsed = false;
  // if (this.searchFieldCount++ >= this.maxSearchMatch) {
  //   throw new Error('The maximum number of fields matching the specified search filter has beeen exceeded  ' +
  //     'Try using a longer field filter.');
  // }
  for (const childField of field.children) {
    markChildrenVisible(childField);
  }
}

export function search(searchFilter: string | undefined, isSource: boolean) {
  const cfg = ConfigModel.getConfig();

  let searchResultsExist = false;
  const searchIsEmpty: boolean =
    undefined === searchFilter || '' === searchFilter;
  const defaultVisibility: boolean = searchIsEmpty;
  for (const docDef of cfg.getDocs(isSource)) {
    docDef.visibleInCurrentDocumentSearch = defaultVisibility;
    for (const field of docDef.getAllFields()) {
      field.visibleInCurrentDocumentSearch = defaultVisibility;
    }
    if (!searchIsEmpty) {
      let searchFieldCount = 0;
      for (const field of docDef.getAllFields()) {
        // Skip this field if it's already determined to be visible.
        if (field.visibleInCurrentDocumentSearch && !field.collapsed) {
          continue;
        }
        field.visibleInCurrentDocumentSearch = field.name
          .toLowerCase()
          .includes(searchFilter!.toLowerCase());
        searchResultsExist =
          searchResultsExist || field.visibleInCurrentDocumentSearch;

        // The current field matches the user-specified filter.
        if (field.visibleInCurrentDocumentSearch) {
          docDef.visibleInCurrentDocumentSearch = true;
          let parentField = field.parentField;

          // Direct lineage is then visible.
          while (
            parentField != null &&
            !parentField.visibleInCurrentDocumentSearch
          ) {
            parentField.visibleInCurrentDocumentSearch = true;
            parentField.collapsed = false;
            parentField = parentField.parentField;
            searchFieldCount++;
          }

          // All fields below the matching field are also visible.
          try {
            markChildrenVisible(field);
          } catch (error) {
            cfg.errorService.addError(
              new ErrorInfo({
                message: error.message,
                level: ErrorLevel.INFO,
                scope: ErrorScope.APPLICATION,
                type: ErrorType.USER,
              })
            );
            break;
          }

          // The total number of matches is limited to allow the UI to perform.
          if (searchFieldCount++ >= MAX_SEARCH_MATCH) {
            cfg.errorService.addError(
              new ErrorInfo({
                message:
                  'The maximum number of fields matching the specified search filter has beeen exceeded  ' +
                  'Try using a longer field filter.',
                level: ErrorLevel.INFO,
                scope: ErrorScope.APPLICATION,
                type: ErrorType.USER,
              })
            );
            break;
          }
        }
      }
    }
  }
  cfg.mappingService.notifyLineRefresh();
}
