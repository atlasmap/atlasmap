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
