import React from "react";
import { CanvasProvider, Canvas } from ".";

export default {
  title: "Canvas",
  includeStories: [], // or don't load this file at all
};

export const example = () => (
  <CanvasProvider initialHeight={300} allowPanning={true}>
    <Canvas>
      <circle cx={90} cy={50} r={20} fill={"green"} />
      <circle cx={150} cy={150} r={50} fill={"purple"} />
      <circle cx={20} cy={70} r={20} fill={"red"} />
    </Canvas>
  </CanvasProvider>
);

export const panZoomDisabled = () => (
  <CanvasProvider initialHeight={300} allowPanning={false}>
    <Canvas>
      <circle cx={90} cy={50} r={20} fill={"green"} />
      <circle cx={150} cy={150} r={50} fill={"purple"} />
      <circle cx={20} cy={70} r={20} fill={"red"} />
    </Canvas>
  </CanvasProvider>
);

export const panZoomEnabled = () => (
  <CanvasProvider initialHeight={300} allowPanning={true}>
    <Canvas>
      <circle cx={90} cy={50} r={20} fill={"green"} />
      <circle cx={150} cy={150} r={50} fill={"purple"} />
      <circle cx={20} cy={70} r={20} fill={"red"} />
    </Canvas>
  </CanvasProvider>
);

export const initialZoomAndPan = () => (
  <CanvasProvider
    initialHeight={300}
    initialZoom={2}
    initialPanX={250}
    initialPanY={-150}
    allowPanning={true}
  >
    <Canvas>
      <circle cx={90} cy={50} r={20} fill={"green"} />
      <circle cx={150} cy={150} r={50} fill={"purple"} />
      <circle cx={20} cy={70} r={20} fill={"red"} />
    </Canvas>
  </CanvasProvider>
);

export const automaticallySized = () => (
  <div style={{ width: 500, height: 500, background: "red" }}>
    <CanvasProvider allowPanning={false}>
      <Canvas>
        <circle cx={480} cy={480} r={20} fill={"white"} />
      </Canvas>
    </CanvasProvider>
  </div>
);
