import React from "react";
import { Canvas } from "./Canvas";
import { CanvasProvider } from "./CanvasContext";
import { HTMLObject } from "./HTMLObject";

export default {
  title: "Canvas",
  includeStories: [], // or don't load this file at all
};

export const example = () => (
  <CanvasProvider initialHeight={300} allowPanning={false}>
    <Canvas>
      <circle cx={280} cy={50} r={20} fill={"purple"} />
      <circle cx={50} cy={100} r={50} fill={"red"} />
      <HTMLObject width={200} height={200} x={50} y={10}>
        <div style={{ border: "1px solid #333", background: "#fff" }}>
          <p>Plain html content inside an SVG.</p>
          <p>
            This is rendered with respect to SVG rules, so this element will
            stay in front of the red circle.
          </p>
          <button>A button</button>
        </div>
      </HTMLObject>
    </Canvas>
  </CanvasProvider>
);
