import React from "react";

import { action } from "@storybook/addon-actions";

import {
  Canvas,
  CanvasProvider,
  Document,
  DraggableField,
  FieldDropTarget,
  HTMLObject,
  NodeRef,
  NodeRefProvider,
  NodesArc,
} from "..";
import decorators from "../../stories/decorators";

export default {
  title: "Drag & Drop",
  includeStories: [], // or don't load this file at all
  decorators,
};

export const example = () => (
  <CanvasProvider allowPanning={false} initialHeight={300}>
    <Canvas>
      <HTMLObject x={10} y={10} width={200} height={200}>
        <DraggableField
          field={{ type: "source", id: "abc123", name: "A field" }}
          onDrop={action("onDrop")}
        >
          {({ isDragging }) => <div>{isDragging ? "dragging" : "drag me"}</div>}
        </DraggableField>
      </HTMLObject>
      <HTMLObject x={250} y={10} width={200} height={200}>
        <FieldDropTarget
          accept={["source"]}
          target={{ type: "target", id: "abc123", name: "A field" }}
          canDrop={() => true}
        >
          {({ isOver, isDroppable, field }) => (
            <div style={{ border: "1px dashed black" }}>
              <p>Drop here!</p>
              <p>
                isOver: {isOver ? "yes" : "no"}
                <br />
                isDroppable: {isDroppable ? "yes" : "no"}
                <br />
                field: {JSON.stringify(field, null, 2)}
              </p>
            </div>
          )}
        </FieldDropTarget>
      </HTMLObject>
    </Canvas>
  </CanvasProvider>
);

export const canBeUsedWithNodesArc = () => (
  <NodeRefProvider>
    <div>
      <p>
        <code>Canvas</code> area is dashed.
      </p>
      <CanvasProvider allowPanning={false} initialHeight={340}>
        <Canvas style={{ border: "1px dashed grey" }} isFilled={false}>
          <HTMLObject x={10} y={10} width={200} height={100}>
            <DraggableField
              field={{ type: "source", id: "f1", name: "Field #1" }}
              style={{ height: "100%" }}
              onDrop={action("onDrop")}
            >
              {({ isDragging }) => (
                <NodeRef id={["f1", isDragging ? "dnd-start" : undefined]}>
                  <Document title="Field #1" dropTarget={isDragging}>
                    {isDragging ? "Dragging" : "Drag me"}
                  </Document>
                </NodeRef>
              )}
            </DraggableField>
            <DraggableField
              field={{ type: "source", id: "f2", name: "Field #2" }}
              style={{ height: "100%" }}
              onDrop={action("onDrop")}
            >
              {({ isDragging }) => (
                <NodeRef id={["f2", isDragging ? "dnd-start" : undefined]}>
                  <Document title={"Field #2"} dropTarget={isDragging}>
                    {isDragging ? "Dragging" : "Drag me"}
                  </Document>
                </NodeRef>
              )}
            </DraggableField>
          </HTMLObject>
          <HTMLObject x={350} y={10} width={200} height={100}>
            <FieldDropTarget
              accept={["source"]}
              target={{ type: "target", id: "t1", name: "t1" }}
              canDrop={(item) => item.id === "f2"}
              style={{ height: "100%" }}
            >
              {({ isDroppable, isTarget }) => (
                <NodeRef id={["df2", isTarget ? "dnd-target" : undefined]}>
                  <Document
                    title="Drop field #2"
                    dropTarget={isTarget}
                    dropAccepted={isDroppable}
                  >
                    {isTarget
                      ? "GIMME!"
                      : isDroppable
                      ? "Drop here!"
                      : "Drop area"}
                  </Document>
                </NodeRef>
              )}
            </FieldDropTarget>
            <FieldDropTarget
              accept={["source"]}
              target={{ type: "target", id: "t2", name: "t2" }}
              canDrop={(item) => item.id === "f1"}
              style={{ height: "100%" }}
            >
              {({ isDroppable, isTarget }) => (
                <NodeRef id={isTarget ? "dnd-target" : undefined}>
                  <Document
                    title="Drop field #1"
                    dropTarget={isTarget}
                    dropAccepted={isDroppable}
                  >
                    {isTarget
                      ? "GIMME!"
                      : isDroppable
                      ? "Drop here!"
                      : "Drop area"}
                  </Document>
                </NodeRef>
              )}
            </FieldDropTarget>
            <FieldDropTarget
              accept={["source"]}
              target={{ type: "target", id: "t3", name: "t3" }}
              canDrop={(item) => item.id === "f1"}
              style={{ height: "100%" }}
            >
              {({ isDroppable, isTarget }) => (
                <NodeRef id={["df1", isTarget ? "dnd-target" : undefined]}>
                  <Document
                    title="Drop field #1"
                    dropTarget={isTarget}
                    dropAccepted={isDroppable}
                  >
                    {isTarget
                      ? "GIMME!"
                      : isDroppable
                      ? "Drop here!"
                      : "Also field #1 drop area"}
                  </Document>
                </NodeRef>
              )}
            </FieldDropTarget>
          </HTMLObject>
          <NodesArc start={"f1"} end={"df1"} />
          <NodesArc
            start={"dnd-start"}
            end={"dnd-target"}
            color={"var(--pf-global--success-color--100)"}
          />
        </Canvas>
      </CanvasProvider>
      <div>
        <p>Can drag & drop outside the canvas too.</p>
        <FieldDropTarget
          accept={["source"]}
          target={{ type: "target", id: "t4", name: "t4" }}
          canDrop={(item) => item.id === "f1"}
          style={{ height: "100%" }}
        >
          {({ isDroppable, isTarget }) => (
            <Document
              title="Drop field #1"
              dropTarget={isTarget}
              dropAccepted={isDroppable}
              style={{ margin: 10 }}
            >
              {isTarget
                ? "GIMME!"
                : isDroppable
                ? "Drop here!"
                : "Also field #1 drop area, but outside the canvas"}
            </Document>
          )}
        </FieldDropTarget>
      </div>
    </div>
  </NodeRefProvider>
);
