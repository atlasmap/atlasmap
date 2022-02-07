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
import { saveAs } from 'file-saver';

export class CommonUtil {
  static removeItemFromArray(item: any, items: any[]): boolean {
    if (item == null || items == null || items.length === 0) {
      return false;
    }
    let i = 0;
    let itemWasRemoved = false;
    while (i < items.length) {
      if (items[i] === item) {
        items.splice(i, 1);
        itemWasRemoved = true;
      } else {
        i++;
      }
    }
    return itemWasRemoved;
  }

  /**
   * Split a source string by the specified substring into a string array.
   *
   * @param inStr
   * @param splitMarker
   */
  static splitByString(inStr: string, splitMarker: string): string[] {
    let splitLoc = 0;
    let splitLocEnd = 0;
    let fragment = null;
    const splitMarkerLen = splitMarker.length;
    const result: string[] = [];

    if (
      inStr === null ||
      inStr.length === 0 ||
      splitMarker === null ||
      splitMarkerLen === 0
    ) {
      return [];
    }
    while (splitLoc !== -1) {
      splitLoc = inStr.indexOf(splitMarker);
      splitLocEnd = inStr.indexOf(splitMarker, splitLoc + 1);
      fragment = inStr.substring(splitLoc, splitLocEnd);
      result.push(fragment);
      inStr = inStr.substring(splitLocEnd + splitMarkerLen);
    }
    return result;
  }

  /**
   * Turn a string into a byte array.
   *
   * @param str
   */
  static str2bytes(str: string): Uint8Array {
    const bytes = new Uint8Array(str.length);
    for (let i = 0; i < str.length; i++) {
      bytes[i] = str.charCodeAt(i);
    }
    return bytes;
  }

  /**
   * Asynchronously read from the specified file and return as a string.
   *
   * @param fileName
   * @param reader
   */
  static async readFile(file: File, reader: FileReader): Promise<string> {
    return new Promise<string>((resolve) => {
      reader.onload = () => {
        const fileBody = reader.result;
        resolve(fileBody as string);
      };
      reader.readAsText(file);
    });
  }

  /**
   *  Perform an asynchronous binary read of the specified file name with the specified reader object.
   *
   * @param fileName - file to read
   * @param reader - reader object
   */
  static async readBinaryFile(
    file: File,
    reader: FileReader
  ): Promise<Int8Array> {
    return new Promise<Int8Array>((resolve) => {
      reader.onload = () => {
        const fileBody = new Int8Array(reader.result as ArrayBuffer);
        resolve(fileBody);
      };
      reader.readAsArrayBuffer(file);
    });
  }

  /**
   * Asynchronously write the specified file content (Blob) to the specified file name.  It will appear
   * in the user's local Downloads directory (or equivalent).
   *
   * @param fileContent
   * @param fName
   */
  static async writeFile(fileContent: Blob, fName: string): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      saveAs(fileContent, fName);
      resolve(true);
    });
  }

  /**
   * Convert a camel-case string into human-readable form.
   *
   * @param camelCaseString
   */
  static toDisplayable(camelCaseString: string): string {
    if (
      typeof camelCaseString === 'undefined' ||
      !camelCaseString ||
      camelCaseString.indexOf(' ') >= 0
    ) {
      return camelCaseString;
    }
    let displayableString: string = camelCaseString.charAt(0).toUpperCase();
    for (let index = 1; index < camelCaseString.length; index++) {
      const chr: string = camelCaseString.charAt(index);
      if (chr !== chr.toLowerCase()) {
        displayableString += ' ';
      }
      displayableString += chr;
    }
    return displayableString;
  }

  /**
   * Return a string path that fits into the width provided.  Capture as much of the leaf
   * as possible, then as much of the beginning with the remaining space.
   *
   * @param path
   * @param fieldWidth
   */
  static extractDisplayPath(path: string, fieldWidth: number): string {
    const MAX_PATH_WIDTH = fieldWidth - 4; // account for length of ellipsis

    if (!path || MAX_PATH_WIDTH <= 0) {
      return '';
    }
    if (path.length <= MAX_PATH_WIDTH) {
      return path;
    }
    const segmentedPath = path.split('/');
    const leaf = '/' + segmentedPath[segmentedPath.length - 1];
    if (leaf.length >= MAX_PATH_WIDTH) {
      return leaf.substr(0, MAX_PATH_WIDTH);
    }
    const delta = MAX_PATH_WIDTH - leaf.length;
    return path.substr(0, delta) + '...' + leaf;
  }

  /**
   * Use the JSON utility to translate the specified buffer into a JSON buffer - then replace any
   * non-ascii character encodings with unicode escape sequences.
   *
   * @param buffer
   */
  static sanitizeJSON(buffer: string): string {
    let jsonBuffer = JSON.stringify(buffer);
    jsonBuffer = jsonBuffer.replace(/[\u007F-\uFFFF]/g, function (chr) {
      return '\\u' + ('0000' + chr.charCodeAt(0).toString(16)).substr(-4);
    });
    return jsonBuffer;
  }

  /**
   * Restrict JSON parsing to the document management service.
   *
   * @param buffer
   */
  static objectize(buffer: any): any {
    if (typeof buffer === 'string') {
      return JSON.parse(buffer);
    } else {
      return buffer;
    }
  }

  /**
   * Returns the given URL with the query parameters replaced with the new
   * values.
   *
   * @param url - the URL
   * @param parameters - the new parameters to set
   */
  static urlWithParameters(
    url: string,
    parameters: string[][] | Record<string, string> | string | URLSearchParams
  ): string {
    const newUrl = new URL(url);
    // we can't invoke delete in a loop over uri.searchParams, results
    // become unreliable, so we gather the keys for deletion, also the
    // URLSearchParams.keys() method is not available for some reason
    const keys: string[] = [];
    newUrl.searchParams.forEach((_, k) => keys.push(k));
    keys.forEach((k) => newUrl.searchParams.delete(k));

    const params = new URLSearchParams(parameters);
    params.forEach((v, k) => newUrl.searchParams.append(k, v));

    return newUrl.toString();
  }
}
