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

export class DataMapperUtil {
  static removeItemFromArray(item: any, items: any[]): boolean {
    if (item == null || items == null || items.length == 0) {
      return false;
    }
    let i = 0;
    let itemWasRemoved = false;
    while (i < items.length) {
      if (items[i] == item) {
        items.splice(i, 1);
        itemWasRemoved = true;
      } else {
        i++;
      }
    }
    return itemWasRemoved;
  }

  static debugLogJSON(object: any, description: string, loggingEnabled: boolean, url: string): void {
    if (!loggingEnabled) {
      return;
    }
    object = (object == null) ? '[none]' : object;
    url = (url == null) ? '[none]' : url;
  }
}
