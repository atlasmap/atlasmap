/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import { AddCircleOIcon, ImportIcon } from '@patternfly/react-icons';
import {
  CanvasProvider,
  Column,
  ColumnBody,
  ColumnMapper,
  Columns,
  Document,
  DocumentField,
  DocumentGroup,
  NodeRef,
  NodeRefProvider,
  NodesArc,
  SearchableColumnHeader,
  Tree,
  TreeGroup,
} from '..';

import { Button } from '@patternfly/react-core';
import React from 'react';
import { TreeItem } from '../Tree/TreeItem';
import { action } from '@storybook/addon-actions';

const obj = {
  title: 'ColumnMapper',
  component: ColumnMapper,
  includeStories: [], // or don't load this file at all
};
export default obj;

export const example = () => (
  <CanvasProvider>
    <ColumnMapper onClick={action('onClick')}>
      <NodeRefProvider>
        <Columns>
          <Column data-testid={'column-source-area'} totalColumns={2}>
            <SearchableColumnHeader
              title={'Source'}
              onSearch={action('onSearch')}
              actions={[
                <Button variant={'plain'} key={1}>
                  <ImportIcon />
                </Button>,
                <Button variant={'plain'} key={2}>
                  <AddCircleOIcon />
                </Button>,
              ]}
            />
            <NodeRef id={'Source'}>
              <ColumnBody>
                <NodeRef id={'Source-width'}>
                  <div>
                    <NodeRef
                      id={'Something here'}
                      boundaryId={'Source'}
                      overrideWidth={'Source-width'}
                    >
                      <Document title={'Something here'} startExpanded={true}>
                        <Tree>
                          <NodeRef
                            id={'Group name'}
                            parentId={'Something here'}
                            boundaryId={'Source'}
                            overrideWidth={'Source-width'}
                          >
                            <TreeGroup
                              id={'Groupname'}
                              level={1}
                              position={1}
                              setSize={1}
                              expanded={true}
                              renderLabel={({ expanded }) => (
                                <DocumentGroup
                                  name={'Group name'}
                                  type={'COMPLEX'}
                                  expanded={expanded}
                                />
                              )}
                            >
                              {() => (
                                <>
                                  <NodeRef
                                    id={'Nested group'}
                                    parentId={'Group name'}
                                    boundaryId={'Source'}
                                    overrideWidth={'Source-width'}
                                  >
                                    <TreeGroup
                                      id={'Nestedgroup'}
                                      level={2}
                                      position={1}
                                      setSize={2}
                                      expanded={true}
                                      renderLabel={({ expanded }) => (
                                        <DocumentGroup
                                          name={'Nested group'}
                                          type={'COMPLEX'}
                                          expanded={expanded}
                                        />
                                      )}
                                    >
                                      {() => (
                                        <NodeRef
                                          id={'Foo bar'}
                                          parentId={'Nested group'}
                                          boundaryId={'Source'}
                                          overrideWidth={'Source-width'}
                                        >
                                          <TreeItem
                                            level={3}
                                            position={1}
                                            setSize={1}
                                          >
                                            {() => (
                                              <DocumentField
                                                name={'Foo bar'}
                                                type={'STRING'}
                                              />
                                            )}
                                          </TreeItem>
                                        </NodeRef>
                                      )}
                                    </TreeGroup>
                                  </NodeRef>
                                  <NodeRef
                                    id={'Fiz'}
                                    parentId={'Group name'}
                                    boundaryId={'Source'}
                                    overrideWidth={'Source-width'}
                                  >
                                    <TreeItem
                                      level={2}
                                      position={2}
                                      setSize={2}
                                    >
                                      {() => (
                                        <DocumentField
                                          name={'Fiz'}
                                          type={'STRING'}
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
                    <Document title={'Lorem dolor'}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={'Lorem dolor'}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={'Lorem dolor'}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={'Lorem dolor'}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={'Lorem dolor'}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={'Lorem dolor'}>
                      <div>lorem dolor</div>
                    </Document>
                  </div>
                </NodeRef>
              </ColumnBody>
            </NodeRef>
          </Column>
          <Column data-testid={'column-target-area'} totalColumns={2}>
            <SearchableColumnHeader
              title={'Target'}
              onSearch={action('onSearch')}
              actions={[
                <Button variant={'plain'} key={1}>
                  <ImportIcon />
                </Button>,
                <Button variant={'plain'} key={2}>
                  <AddCircleOIcon />
                </Button>,
              ]}
            />
            <NodeRef id={'Target'}>
              <ColumnBody>
                <NodeRef id={'Target-width'}>
                  <div>
                    <NodeRef
                      id={'Lorem dolor'}
                      boundaryId={'Target'}
                      overrideWidth={'Target-width'}
                    >
                      <Document title={'Lorem dolor'} startExpanded={true}>
                        <Tree>
                          <>
                            <NodeRef
                              id={'Group foo'}
                              parentId={'Lorem dolor'}
                              boundaryId={'Target'}
                              overrideWidth={'Target-width'}
                            >
                              <TreeGroup
                                id={'Groupfoo'}
                                expanded={true}
                                level={1}
                                position={1}
                                setSize={3}
                                renderLabel={({ expanded }) => (
                                  <DocumentGroup
                                    name={'Group foo'}
                                    type={'COMPLEX'}
                                    expanded={expanded}
                                  />
                                )}
                              >
                                {() => (
                                  <NodeRef
                                    id={'Foo'}
                                    parentId={'Group foo'}
                                    boundaryId={'Target'}
                                    overrideWidth={'Target-width'}
                                  >
                                    <TreeItem
                                      level={2}
                                      position={1}
                                      setSize={1}
                                    >
                                      {() => (
                                        <DocumentField
                                          name={'Foo'}
                                          type={'STRING'}
                                        />
                                      )}
                                    </TreeItem>
                                  </NodeRef>
                                )}
                              </TreeGroup>
                            </NodeRef>
                            <NodeRef
                              id={'Bar'}
                              parentId={'Lorem dolor'}
                              boundaryId={'Target'}
                              overrideWidth={'Target-width'}
                            >
                              <DocumentField name={'Bar'} type={'STRING'} />
                            </NodeRef>
                            <NodeRef
                              id={'Baz'}
                              parentId={'Lorem dolor'}
                              boundaryId={'Target'}
                              overrideWidth={'Target-width'}
                            >
                              <DocumentField name={'Baz'} type={'STRING'} />
                            </NodeRef>
                          </>
                        </Tree>
                      </Document>
                    </NodeRef>
                    <Document title={'Lorem dolor'}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={'Lorem dolor'}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={'Lorem dolor'}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={'Lorem dolor'}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={'Lorem dolor'}>
                      <div>lorem dolor</div>
                    </Document>
                    <Document title={'Lorem dolor'}>
                      <div>lorem dolor</div>
                    </Document>
                  </div>
                </NodeRef>
              </ColumnBody>
            </NodeRef>
          </Column>
        </Columns>
        <NodesArc start={'Fiz'} end={'Foo'} data-testid={'Fiz:Foo'} />
        <NodesArc start={'Foo bar'} end={'Baz'} data-testid={'Foo bar:Baz'} />
      </NodeRefProvider>
    </ColumnMapper>
  </CanvasProvider>
);
