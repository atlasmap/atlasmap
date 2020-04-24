import React from "react";

import { Tree, TreeGroup, TreeItem } from ".";

export default {
  title: "Tree",
  component: Tree,
  includeStories: [], // or don't load this file at all
};

export const example = () => (
  <Tree>
    <TreeGroup
      id={"g1"}
      level={1}
      position={1}
      setSize={3}
      renderLabel={({ expanded, focused }) =>
        `${expanded ? "⌄" : "›"} G1 ${focused ? "- has focus" : ""}`
      }
    >
      {() => (
        <>
          <TreeGroup
            id={"g1-1"}
            level={2}
            position={1}
            setSize={3}
            renderLabel={({ expanded, focused }) =>
              `${expanded ? "⌄" : "›"} Group G1-1 ${
                focused ? "- has focus" : ""
              }`
            }
          >
            {() => (
              <>
                <TreeItem level={3} position={1} setSize={3}>
                  {({ focused }) =>
                    `Item G1-1 1 ${focused ? "- has focus" : ""}`
                  }
                </TreeItem>
                <TreeItem level={3} position={2} setSize={3}>
                  {({ focused }) =>
                    `Item G1-1 2 ${focused ? "- has focus" : ""}`
                  }
                </TreeItem>
                <TreeItem level={3} position={3} setSize={3}>
                  {({ focused }) =>
                    `Item G1-1 3 ${focused ? "- has focus" : ""}`
                  }
                </TreeItem>
              </>
            )}
          </TreeGroup>
          <TreeItem level={1} position={2} setSize={2}>
            {({ focused }) => `Item G1-2 ${focused ? "- has focus" : ""}`}
          </TreeItem>
        </>
      )}
    </TreeGroup>
    <TreeItem level={1} position={2} setSize={3}>
      {({ focused }) => `Item G2 ${focused ? "- has focus" : ""}`}
    </TreeItem>
    <TreeItem level={1} position={3} setSize={3}>
      {({ focused }) => `Item G3 ${focused ? "- has focus" : ""}`}
    </TreeItem>
  </Tree>
);
