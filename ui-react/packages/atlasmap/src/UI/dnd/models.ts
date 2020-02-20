export interface IDragAndDropField {
  type: "source" | "target" | "mapping";
  id: string;
  name: string;
  payload?: any; // TODO: I hate this
}
