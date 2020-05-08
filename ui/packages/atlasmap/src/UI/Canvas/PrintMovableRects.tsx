import React, { useCallback, useEffect, useState } from "react";
import { RectWithId } from "./models";
import { useCanvas } from "./CanvasContext";

export default function () {
  const { addRedrawListener, removeRedrawListener, getRects } = useCanvas();
  const [rects, setRects] = useState<RectWithId[]>([]);

  const updateRects = useCallback(() => {
    setRects(getRects());
  }, [getRects]);

  useEffect(() => {
    addRedrawListener(updateRects);
    return () => {
      removeRedrawListener(updateRects);
    };
  }, [addRedrawListener, removeRedrawListener, updateRects]);

  return (
    <g>
      {rects.map((r, idx) => (
        <rect
          x={r.x}
          y={r.y}
          width={r.width}
          height={r.height}
          stroke={"black"}
          strokeDasharray={2}
          fillOpacity={0}
          key={idx}
          style={{ pointerEvents: "none" }}
        />
      ))}
    </g>
  );
}
