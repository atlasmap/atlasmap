import * as React from "react";
import { render } from "./setup";
import { Column } from "../src";

describe("Column tests", () => {
  test("should render", async () => {
    const { getByText } = render(<Column totalColumns={1}>test</Column>);
    getByText("test");
  });
});
