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
  CollectionType,
  ConfigModel,
  DocumentDefinition,
} from "@atlasmap/core";

export class ClassNameComponent {
  cfg: ConfigModel = ConfigModel.getConfig();
  isSource: boolean = false;
  userClassName: string = "";
  userCollectionType = CollectionType.NONE;
  userCollectionClassName = "";
  docDef: DocumentDefinition | null = null;

  constructor(className: string, collectionType: string, isSource: boolean) {
    this.userClassName = className;
    let typedCollectionString: keyof typeof CollectionType = "NONE";
    typedCollectionString = collectionType as keyof typeof CollectionType;
    this.userCollectionType = CollectionType[typedCollectionString];
    this.isSource = isSource;
  }

  isDataValid(): boolean {
    return this.cfg.errorService.isRequiredFieldValid(
      this.userClassName,
      "Class name",
    );
  }
}
