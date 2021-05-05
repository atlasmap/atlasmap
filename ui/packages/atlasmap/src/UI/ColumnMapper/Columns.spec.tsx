import * as React from "react";
import { Canvas, CanvasProvider } from "../Canvas";

import { Columns } from "./Columns";
import { render } from "@testing-library/react";

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
