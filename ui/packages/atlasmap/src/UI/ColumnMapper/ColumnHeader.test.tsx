import { render } from "@testing-library/react";
import { example } from "./ColumnHeader.stories";

describe("ColumnHeader tests", () => {
  test("should render", async () => {
    const { getByText } = render(example());
    getByText("Source");
  });
});
