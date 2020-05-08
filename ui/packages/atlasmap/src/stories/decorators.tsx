import React, { ReactElement } from "react";
import { FieldsDndProvider, FieldDragLayer } from "../UI";

export default [
  (storyFn: () => ReactElement) => (
    <FieldsDndProvider>
      {storyFn()}
      <FieldDragLayer />
    </FieldsDndProvider>
  ),
];
