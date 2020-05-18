import * as React from "react";
import { render } from "@testing-library/react";
import { Columns } from "./Columns";
import { CanvasProvider, Canvas } from "../Canvas";

describe("Columns tests", () => {
  test("should render", async () => {
    const { getByText } = render(
      <CanvasProvider>
        <Canvas>
          <Columns>test</Columns>
        </Canvas>
      </CanvasProvider>,
    );
    getByText("test");
  });
});
