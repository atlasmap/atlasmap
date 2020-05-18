import { render, act } from "@testing-library/react";
import { example } from "./ColumnMapper.stories";

describe("ColumnMapper tests", () => {
  test("should render", async () => {
    const { getByText, findByTestId } = render(example());
    getByText("Source");
    getByText("Mapping");
    getByText("Target");
    // lines are rendered after a DOM layout event
    await act(async () => {
      await findByTestId("Fiz:Mapping 1");
      await findByTestId("Mapping 1:Foo");
      await findByTestId("Foo bar:Mapping 2");
      await findByTestId("Mapping 2:Baz");
    });
  });
});
