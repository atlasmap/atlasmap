import { example } from "./ColumnHeader.stories";
import { render } from "@testing-library/react";

describe("ColumnHeader tests", () => {
  test("should render", async () => {
    const { getByText } = render(example());
    getByText("Source");
  });
});
