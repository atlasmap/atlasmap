import React from "react";

import { RedhatIcon } from "@patternfly/react-icons";
import { action } from "@storybook/addon-actions";
import { boolean } from "@storybook/addon-knobs";

import { Canvas, CanvasProvider } from "../UI";
import { CanvasControlBar } from "./CanvasControlBar";

export default {
  title: "AtlasMap|Layout/CanvasControlBar",
};

export const example = () => {
  const freeView = boolean("Enable canvas panning", true);
  return (
    <div>
      <CanvasProvider initialHeight={300} allowPanning={freeView}>
        <Canvas>
          <circle cx={90} cy={50} r={20} fill={"green"} />
          <circle cx={150} cy={150} r={50} fill={"purple"} />
          <circle cx={20} cy={70} r={20} fill={"red"} />
        </Canvas>
        <CanvasControlBar
          disabled={!freeView}
          extraButtons={[
            {
              id: "id1",
              icon: <RedhatIcon />,
              callback: action("Custom button clicked"),
            },
          ]}
        />
      </CanvasProvider>
    </div>
  );
};
