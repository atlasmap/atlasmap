import * as React from "react";

import { ColumnBody } from "./ColumnBody";
import { render } from "@testing-library/react";

describe("ColumnBody tests", () => {
  test("should render", async () => {
    const { getByText } = render(<ColumnBody>test</ColumnBody>);
    getByText("test");
  });
});
