import * as React from "react";
import { render } from "@testing-library/react";
import { ColumnBody } from "./ColumnBody";

describe("ColumnBody tests", () => {
  test("should render", async () => {
    const { getByText } = render(<ColumnBody>test</ColumnBody>);
    getByText("test");
  });
});
