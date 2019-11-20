export interface IMapperUtilsToolbarProps {
  importAtlasFile: (selectedFile: File) => void;
}

export interface IProcessImportedFileArgs {
  selectedFile: File;
}

export function processImportedFile({ selectedFile }: IProcessImportedFileArgs) {
  console.log('processImportedFile: ' + selectedFile.name);
  importAtlasFile(selectedFile);
}