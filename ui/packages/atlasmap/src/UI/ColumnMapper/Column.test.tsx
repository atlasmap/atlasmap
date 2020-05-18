import * as React from "react";
import { render } from "@testing-library/react";
import { Column } from "./Column";
import { example } from "./Column.stories";

describe("Column tests", () => {
  test("should render", async () => {
    const { getByText } = render(example());
    getByText("I can scroll.");
  });

  /* the following tests are not really meant to be unit tests since checking
   * the actual rendering of the DOM is better handled in a real browser, like
   * Puppeteer. But I don't like Jest's snapshots, so...
   * */

  test("should respect the parent size", async () => {
    const { getByTestId } = render(
      <Column totalColumns={2} data-testid={"column"}>
        test
      </Column>,
    );
    expect(getByTestId("column").style).toHaveProperty("flex", "0 0 50%");
  });

  test("should respect the visible prop", async () => {
    const { getByTestId } = render(
      <Column visible={false} data-testid={"column"}>
        test
      </Column>,
    );
    expect(getComputedStyle(getByTestId("column"))).toHaveProperty(
      "opacity",
      "0",
    );
  });
});
