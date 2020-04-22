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
} from "..";

export default {
  title: "ColumnMapper",
  component: ColumnMapper,
  includeStories: [], // or don't load this file at all
};

export const example = () => (
  <div style={{ minHeight: 300, display: "flex", flexFlow: "column" }}>
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
                          <NodeRef
                            id={"Group name"}
                            parentId={"Something here"}
                            boundaryId={"Source"}
                            overrideWidth={"Source-width"}
                          >
                            <DocumentGroup
                              id={"Groupname"}
                              name={"Group name"}
                              type={"COMPLEX"}
                              expanded={true}
                              level={1}
                              position={1}
                              setSize={1}
                            >
                              <NodeRef
                                id={"Nested group"}
                                parentId={"Group name"}
                                boundaryId={"Source"}
                                overrideWidth={"Source-width"}
                              >
                                <DocumentGroup
                                  id={"Nestedgroup"}
                                  name={"Nested group"}
                                  type={"COMPLEX"}
                                  level={2}
                                  position={1}
                                  setSize={2}
                                >
                                  <NodeRef
                                    id={"Foo bar"}
                                    parentId={"Nested group"}
                                    boundaryId={"Source"}
                                    overrideWidth={"Source-width"}
                                  >
                                    <DocumentField
                                      name={"Foo bar"}
                                      type={"STRING"}
                                      level={3}
                                      position={1}
                                      setSize={1}
                                    />
                                  </NodeRef>
                                </DocumentGroup>
                              </NodeRef>
                              <NodeRef
                                id={"Fiz"}
                                parentId={"Group name"}
                                boundaryId={"Source"}
                                overrideWidth={"Source-width"}
                              >
                                <DocumentField
                                  name={"Fiz"}
                                  type={"STRING"}
                                  level={2}
                                  position={2}
                                  setSize={2}
                                />
                              </NodeRef>
                            </DocumentGroup>
                          </NodeRef>
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
                            level={1}
                            position={1}
                            setSize={2}
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
                            level={1}
                            position={2}
                            setSize={2}
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
                          <NodeRef
                            id={"Group foo"}
                            parentId={"Lorem dolor"}
                            boundaryId={"Target"}
                            overrideWidth={"Target-width"}
                          >
                            <DocumentGroup
                              id={"Groupfoo"}
                              name={"Group foo"}
                              type={"COMPLEX"}
                              expanded={true}
                              level={1}
                              position={1}
                              setSize={3}
                            >
                              <NodeRef
                                id={"Foo"}
                                parentId={"Group foo"}
                                boundaryId={"Target"}
                                overrideWidth={"Target-width"}
                              >
                                <DocumentField
                                  name={"Foo"}
                                  type={"STRING"}
                                  level={2}
                                  position={1}
                                  setSize={1}
                                />
                              </NodeRef>
                            </DocumentGroup>
                          </NodeRef>
                          <NodeRef
                            id={"Bar"}
                            parentId={"Lorem dolor"}
                            boundaryId={"Target"}
                            overrideWidth={"Target-width"}
                          >
                            <DocumentField
                              name={"Bar"}
                              type={"STRING"}
                              level={1}
                              position={2}
                              setSize={3}
                            />
                          </NodeRef>
                          <NodeRef
                            id={"Baz"}
                            parentId={"Lorem dolor"}
                            boundaryId={"Target"}
                            overrideWidth={"Target-width"}
                          >
                            <DocumentField
                              name={"Baz"}
                              type={"STRING"}
                              level={1}
                              position={3}
                              setSize={3}
                            />
                          </NodeRef>
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
  </div>
);
