import React from "react";
import { Canvas } from "./Canvas";
import { CanvasProvider } from "./CanvasContext";
import { Arc } from "./Arc";

export default {
  title: "Canvas",
  includeStories: [], // or don't load this file at all
};

export const example = () => (
  <CanvasProvider initialHeight={200} allowPanning={false}>
    <Canvas>
      <circle cx={20} cy={30} r={20} fill={"purple"} />
      <circle cx={250} cy={150} r={50} fill={"red"} />
      <Arc start={{ x: 40, y: 30 }} end={{ x: 200, y: 150 }} />
    </Canvas>
  </CanvasProvider>
);

export const customWidthAndColor = () => (
  <CanvasProvider initialHeight={300} allowPanning={false}>
    <Canvas>
      <circle cx={90} cy={50} r={20} fill={"green"} />
      <circle cx={20} cy={240} r={20} fill={"red"} />
      <circle cx={350} cy={150} r={50} fill={"purple"} />
      <Arc
        start={{ x: 110, y: 50 }}
        end={{ x: 300, y: 150 }}
        color={"green"}
        strokeWidth={1}
      />
      <Arc
        start={{ x: 40, y: 240 }}
        end={{ x: 300, y: 150 }}
        color={"red"}
        strokeWidth={4}
      />
    </Canvas>
  </CanvasProvider>
);
