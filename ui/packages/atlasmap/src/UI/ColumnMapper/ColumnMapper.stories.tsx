import React from "react";

import { action } from "@storybook/addon-actions";

import {
  Column,
  ColumnBody,
  ColumnHeader,
  ColumnMapper,
  Columns,
  Document,
  DocumentField,
  DocumentGroup,
  NodeRef,
  NodeRefProvider,
  NodesArc,
  SearchableColumnHeader,
  CanvasProvider,
  Tree,
  TreeGroup,
} from "..";
import { TreeItem } from "../Tree/TreeItem";

export default {
  title: "ColumnMapper",
  component: ColumnMapper,
  includeStories: [], // or don't load this file at all
};

export const example = () => (
  <CanvasProvider>
    <ColumnMapper onClick={action("onClick")}>
      <NodeRefProvider>
        <Columns>
          <Column totalColumns={3}>
            <SearchableColumnHeader
              title={"Source"}
              onSearch={action("onSearch")}
            />
            <NodeRef id={"Source"}>
              <ColumnBody>
                <NodeRef id={"Source-width"}>
                  <div>
                    <NodeRef
                      id={"Something here"}
                      boundaryId={"Source"}
                      overrideWidth={"Source-width"}
                    >
                      <Document title={"Something here"}>
                        <Tree>
                          <NodeRef
                            id={"Group name"}
                            parentId={"Something here"}
                            boundaryId={"Source"}
                            overrideWidth={"Source-width"}
                          >
                            <TreeGroup
                              id={"Groupname"}
                              level={1}
                              position={1}
                              setSize={1}
                              renderLabel={({ expanded }) => (
                                <DocumentGroup
                                  name={"Group name"}
                                  type={"COMPLEX"}
                                  expanded={expanded}
                                />
                              )}
                            >
                              {() => (
                                <>
                                  <NodeRef
                                    id={"Nested group"}
                                    parentId={"Group name"}
                                    boundaryId={"Source"}
                                    overrideWidth={"Source-width"}
                                  >
                                    <TreeGroup
                                      id={"Nestedgroup"}
                                      level={2}
                                      position={1}
                                      setSize={2}
                                      renderLabel={({ expanded }) => (
                                        <DocumentGroup
                                          name={"Nested group"}
                                          type={"COMPLEX"}
                                          expanded={expanded}
                                        />
                                      )}
                                    >
                                      {() => (
                                        <NodeRef
                                          id={"Foo bar"}
                                          parentId={"Nested group"}
                                          boundaryId={"Source"}
                                          overrideWidth={"Source-width"}
                                        >
                                          <TreeItem
                                            level={3}
                                            position={1}
                                            setSize={1}
                                          >
                                            {() => (
                                              <DocumentField
                                                name={"Foo bar"}
                                                type={"STRING"}
                                              />
                                            )}
                                          </TreeItem>
                                        </NodeRef>
                                      )}
                                    </TreeGroup>
                                  </NodeRef>
                                  <NodeRef
                                    id={"Fiz"}
                                    parentId={"Group name"}
                                    boundaryId={"Source"}
                                    overrideWidth={"Source-width"}
                                  >
                                    <TreeItem
                                      level={2}
                                      position={2}
                                      setSize={2}
                                    >
                                      {() => (
                                        <DocumentField
                                          name={"Fiz"}
                                          type={"STRING"}
                                        />
                                      )}
                                    </TreeItem>
                                  </NodeRef>
                                </>
                              )}
                            </TreeGroup>
                          </NodeRef>
                        </Tree>
                      </Document>
                    </NodeRef>
                    <Document title={"Lorem dolor"}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={"Lorem dolor"}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={"Lorem dolor"}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={"Lorem dolor"}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={"Lorem dolor"}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={"Lorem dolor"}>
                      <div>lorem dolor</div>
                    </Document>
                  </div>
                </NodeRef>
              </ColumnBody>
            </NodeRef>
          </Column>
          <Column totalColumns={3}>
            <ColumnHeader title={"Mapping"} />
            <NodeRef id={"Mapping"}>
              <ColumnBody>
                <NodeRef id={"Mapping-width"}>
                  <div>
                    <NodeRef
                      id={"Mapping 1"}
                      boundaryId={"Mapping"}
                      overrideWidth={"Mapping-width"}
                    >
                      <Document title={"Mapping 1"}>
                        <DocumentField
                          name={"Many To One"}
                          type={"Concatenate"}
                        />
                      </Document>
                    </NodeRef>
                    <NodeRef
                      id={"Mapping 2"}
                      boundaryId={"Mapping"}
                      overrideWidth={"Mapping-width"}
                    >
                      <Document title={"Mapping 2"}>
                        <DocumentField
                          name={"Many To One"}
                          type={"Concatenate"}
                        />
                      </Document>
                    </NodeRef>
                  </div>
                </NodeRef>
              </ColumnBody>
            </NodeRef>
          </Column>
          <Column totalColumns={3}>
            <SearchableColumnHeader
              title={"Target"}
              onSearch={action("onSearch")}
            />
            <NodeRef id={"Target"}>
              <ColumnBody>
                <NodeRef id={"Target-width"}>
                  <div>
                    <NodeRef
                      id={"Lorem dolor"}
                      boundaryId={"Target"}
                      overrideWidth={"Target-width"}
                    >
                      <Document title={"Lorem dolor"}>
                        <Tree>
                          <>
                            <NodeRef
                              id={"Group foo"}
                              parentId={"Lorem dolor"}
                              boundaryId={"Target"}
                              overrideWidth={"Target-width"}
                            >
                              <TreeGroup
                                id={"Groupfoo"}
                                expanded={true}
                                level={1}
                                position={1}
                                setSize={3}
                                renderLabel={({ expanded }) => (
                                  <DocumentGroup
                                    name={"Group foo"}
                                    type={"COMPLEX"}
                                    expanded={expanded}
                                  />
                                )}
                              >
                                {() => (
                                  <NodeRef
                                    id={"Foo"}
                                    parentId={"Group foo"}
                                    boundaryId={"Target"}
                                    overrideWidth={"Target-width"}
                                  >
                                    <TreeItem
                                      level={2}
                                      position={1}
                                      setSize={1}
                                    >
                                      {() => (
                                        <DocumentField
                                          name={"Foo"}
                                          type={"STRING"}
                                        />
                                      )}
                                    </TreeItem>
                                  </NodeRef>
                                )}
                              </TreeGroup>
                            </NodeRef>
                            <NodeRef
                              id={"Bar"}
                              parentId={"Lorem dolor"}
                              boundaryId={"Target"}
                              overrideWidth={"Target-width"}
                            >
                              <DocumentField name={"Bar"} type={"STRING"} />
                            </NodeRef>
                            <NodeRef
                              id={"Baz"}
                              parentId={"Lorem dolor"}
                              boundaryId={"Target"}
                              overrideWidth={"Target-width"}
                            >
                              <DocumentField name={"Baz"} type={"STRING"} />
                            </NodeRef>
                          </>
                        </Tree>
                      </Document>
                    </NodeRef>
                    <Document title={"Lorem dolor"}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={"Lorem dolor"}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={"Lorem dolor"}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={"Lorem dolor"}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={"Lorem dolor"}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={"Lorem dolor"}>
                      <div>lorem dolor</div>
                    </Document>
                  </div>
                </NodeRef>
              </ColumnBody>
            </NodeRef>
          </Column>
        </Columns>
        <NodesArc start={"Fiz"} end={"Mapping 1"} />
        <NodesArc start={"Mapping 1"} end={"Foo"} />
        <NodesArc start={"Foo bar"} end={"Mapping 2"} />
        <NodesArc start={"Mapping 2"} end={"Baz"} />
      </NodeRefProvider>
    </ColumnMapper>
  </CanvasProvider>
);
