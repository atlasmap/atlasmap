export type Coords = {
  x: number;
  y: number;
};
export type Rect = {
  x: number;
  y: number;
  width: number;
  height: number;
};
export type RectWithId = {
  id: string;
} & Rect;
export type Rects = Array<RectWithId>;
