import { JavaObject, javaToFieldGroup } from './utils/fromJava';
import { JsonObject, jsonToFieldGroup } from './utils/fromJson';
import { XMLObject, xmlToFieldGroup } from './utils/fromXML';

const mockJSONInstanceSource: JsonObject = {
  JsonInspectionResponse: {
    jsonType: 'io.atlasmap.json.v2.JsonInspectionResponse',
    jsonDocument: {
      jsonType: 'io.atlasmap.json.v2.JsonDocument',
      fields: {
        field: [
          {
            jsonType: 'io.atlasmap.json.v2.JsonComplexType',
            path: '/order',
            status: 'SUPPORTED',
            fieldType: 'COMPLEX',
            name: 'order',
            jsonFields: {
              jsonField: [
                {
                  jsonType: 'io.atlasmap.json.v2.JsonComplexType',
                  path: '/order/address',
                  status: 'SUPPORTED',
                  fieldType: 'COMPLEX',
                  name: 'address',
                  jsonFields: {
                    jsonField: [
                      {
                        jsonType: 'io.atlasmap.json.v2.JsonField',
                        value: '123 any st',
                        path: '/order/address/street',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'street',
                      },
                      {
                        jsonType: 'io.atlasmap.json.v2.JsonField',
                        value: 'Austin',
                        path: '/order/address/city',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'city',
                      },
                      {
                        jsonType: 'io.atlasmap.json.v2.JsonField',
                        value: 'TX',
                        path: '/order/address/state',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'state',
                      },
                      {
                        jsonType: 'io.atlasmap.json.v2.JsonField',
                        value: '78626',
                        path: '/order/address/zip',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'zip',
                      },
                    ],
                  },
                },
                {
                  jsonType: 'io.atlasmap.json.v2.JsonComplexType',
                  path: '/order/contact',
                  status: 'SUPPORTED',
                  fieldType: 'COMPLEX',
                  name: 'contact',
                  jsonFields: {
                    jsonField: [
                      {
                        jsonType: 'io.atlasmap.json.v2.JsonField',
                        value: 'james',
                        path: '/order/contact/firstName',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'firstName',
                      },
                      {
                        jsonType: 'io.atlasmap.json.v2.JsonField',
                        value: 'smith',
                        path: '/order/contact/lastName',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'lastName',
                      },
                      {
                        jsonType: 'io.atlasmap.json.v2.JsonField',
                        value: '512-123-1234',
                        path: '/order/contact/phone',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'phone',
                      },
                    ],
                  },
                },
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  value: '123',
                  path: '/order/orderId',
                  status: 'SUPPORTED',
                  fieldType: 'STRING',
                  name: 'orderId',
                },
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  value: '',
                  path: '/order/customerName',
                  status: 'SUPPORTED',
                  fieldType: 'STRING',
                  name: 'customerName',
                },
              ],
            },
          },
          {
            jsonType: 'io.atlasmap.json.v2.JsonComplexType',
            path: '/primitives',
            status: 'SUPPORTED',
            fieldType: 'COMPLEX',
            name: 'primitives',
            jsonFields: {
              jsonField: [
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  value: 'some value',
                  path: '/primitives/stringPrimitive',
                  status: 'SUPPORTED',
                  fieldType: 'STRING',
                  name: 'stringPrimitive',
                },
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  value: true,
                  path: '/primitives/booleanPrimitive',
                  status: 'SUPPORTED',
                  fieldType: 'BOOLEAN',
                  name: 'booleanPrimitive',
                },
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  value: 24,
                  path: '/primitives/numberPrimitive',
                  status: 'SUPPORTED',
                  fieldType: 'INTEGER',
                  name: 'numberPrimitive',
                },
              ],
            },
          },
          {
            jsonType: 'io.atlasmap.json.v2.JsonComplexType',
            collectionType: 'LIST',
            path: '/addressList<>',
            status: 'SUPPORTED',
            fieldType: 'COMPLEX',
            name: 'addressList',
            jsonFields: {
              jsonField: [
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  value: '123 any st',
                  path: '/addressList<>/street',
                  status: 'SUPPORTED',
                  fieldType: 'STRING',
                  name: 'street',
                },
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  value: 'Austin',
                  path: '/addressList<>/city',
                  status: 'SUPPORTED',
                  fieldType: 'STRING',
                  name: 'city',
                },
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  value: 'TX',
                  path: '/addressList<>/state',
                  status: 'SUPPORTED',
                  fieldType: 'STRING',
                  name: 'state',
                },
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  value: '78626',
                  path: '/addressList<>/zip',
                  status: 'SUPPORTED',
                  fieldType: 'STRING',
                  name: 'zip',
                },
              ],
            },
          },
        ],
      },
    },
    executionTime: 10,
  },
};
const mockJSONSchemaSource: JsonObject = {
  JsonInspectionResponse: {
    jsonType: 'io.atlasmap.json.v2.JsonInspectionResponse',
    jsonDocument: {
      jsonType: 'io.atlasmap.json.v2.JsonDocument',
      fields: {
        field: [
          {
            jsonType: 'io.atlasmap.json.v2.JsonComplexType',
            path: '/order',
            status: 'SUPPORTED',
            fieldType: 'COMPLEX',
            name: 'order',
            jsonFields: {
              jsonField: [
                {
                  jsonType: 'io.atlasmap.json.v2.JsonComplexType',
                  path: '/order/address',
                  status: 'SUPPORTED',
                  fieldType: 'COMPLEX',
                  name: 'address',
                  jsonFields: {
                    jsonField: [
                      {
                        jsonType: 'io.atlasmap.json.v2.JsonField',
                        path: '/order/address/street',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'street',
                      },
                      {
                        jsonType: 'io.atlasmap.json.v2.JsonField',
                        path: '/order/address/city',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'city',
                      },
                      {
                        jsonType: 'io.atlasmap.json.v2.JsonField',
                        path: '/order/address/state',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'state',
                      },
                      {
                        jsonType: 'io.atlasmap.json.v2.JsonField',
                        path: '/order/address/zip',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'zip',
                      },
                    ],
                  },
                },
                {
                  jsonType: 'io.atlasmap.json.v2.JsonComplexType',
                  path: '/order/contact',
                  status: 'SUPPORTED',
                  fieldType: 'COMPLEX',
                  name: 'contact',
                  jsonFields: {
                    jsonField: [
                      {
                        jsonType: 'io.atlasmap.json.v2.JsonField',
                        path: '/order/contact/firstName',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'firstName',
                      },
                      {
                        jsonType: 'io.atlasmap.json.v2.JsonField',
                        path: '/order/contact/lastName',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'lastName',
                      },
                      {
                        jsonType: 'io.atlasmap.json.v2.JsonField',
                        path: '/order/contact/phone',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'phone',
                      },
                    ],
                  },
                },
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  path: '/order/orderId',
                  status: 'SUPPORTED',
                  fieldType: 'STRING',
                  name: 'orderId',
                },
              ],
            },
          },
          {
            jsonType: 'io.atlasmap.json.v2.JsonComplexType',
            path: '/primitives',
            status: 'SUPPORTED',
            fieldType: 'COMPLEX',
            name: 'primitives',
            jsonFields: {
              jsonField: [
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  path: '/primitives/stringPrimitive',
                  status: 'SUPPORTED',
                  fieldType: 'STRING',
                  name: 'stringPrimitive',
                },
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  path: '/primitives/booleanPrimitive',
                  status: 'SUPPORTED',
                  fieldType: 'BOOLEAN',
                  name: 'booleanPrimitive',
                },
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  path: '/primitives/numberPrimitive',
                  status: 'SUPPORTED',
                  fieldType: 'NUMBER',
                  name: 'numberPrimitive',
                },
              ],
            },
          },
          {
            jsonType: 'io.atlasmap.json.v2.JsonComplexType',
            path: '/primitiveArrays',
            status: 'SUPPORTED',
            fieldType: 'COMPLEX',
            name: 'primitiveArrays',
            jsonFields: {
              jsonField: [
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  collectionType: 'LIST',
                  path: '/primitiveArrays/stringArray<>',
                  status: 'SUPPORTED',
                  fieldType: 'STRING',
                  name: 'stringArray',
                },
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  collectionType: 'LIST',
                  path: '/primitiveArrays/booleanArray<>',
                  status: 'SUPPORTED',
                  fieldType: 'BOOLEAN',
                  name: 'booleanArray',
                },
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  collectionType: 'LIST',
                  path: '/primitiveArrays/numberArray<>',
                  status: 'SUPPORTED',
                  fieldType: 'NUMBER',
                  name: 'numberArray',
                },
              ],
            },
          },
          {
            jsonType: 'io.atlasmap.json.v2.JsonComplexType',
            collectionType: 'LIST',
            path: '/addressList<>',
            status: 'SUPPORTED',
            fieldType: 'COMPLEX',
            name: 'addressList',
            jsonFields: {
              jsonField: [
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  path: '/addressList<>/street',
                  status: 'SUPPORTED',
                  fieldType: 'STRING',
                  name: 'street',
                },
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  path: '/addressList<>/city',
                  status: 'SUPPORTED',
                  fieldType: 'STRING',
                  name: 'city',
                },
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  path: '/addressList<>/state',
                  status: 'SUPPORTED',
                  fieldType: 'STRING',
                  name: 'state',
                },
                {
                  jsonType: 'io.atlasmap.json.v2.JsonField',
                  path: '/addressList<>/zip',
                  status: 'SUPPORTED',
                  fieldType: 'STRING',
                  name: 'zip',
                },
              ],
            },
          },
        ],
      },
    },
    executionTime: 2,
  },
};

const ioPaulBicycle: JavaObject = {
  ClassInspectionResponse: {
    jsonType: 'io.atlasmap.java.v2.ClassInspectionResponse',
    javaClass: {
      jsonType: 'io.atlasmap.java.v2.JavaClass',
      path: '/',
      fieldType: 'COMPLEX',
      modifiers: {
        modifier: ['PUBLIC'],
      },
      className: 'io.paul.Bicycle',
      canonicalClassName: 'io.paul.Bicycle',
      primitive: false,
      synthetic: false,
      javaEnumFields: {
        javaEnumField: [],
      },
      javaFields: {
        javaField: [
          {
            jsonType: 'io.atlasmap.java.v2.JavaField',
            path: '/cadence',
            status: 'SUPPORTED',
            fieldType: 'INTEGER',
            modifiers: {
              modifier: ['PUBLIC'],
            },
            name: 'cadence',
            className: 'int',
            canonicalClassName: 'int',
            primitive: true,
            synthetic: false,
          },
          {
            jsonType: 'io.atlasmap.java.v2.JavaField',
            path: '/gear',
            status: 'SUPPORTED',
            fieldType: 'INTEGER',
            modifiers: {
              modifier: ['PUBLIC'],
            },
            name: 'gear',
            className: 'int',
            canonicalClassName: 'int',
            primitive: true,
            synthetic: false,
          },
          {
            jsonType: 'io.atlasmap.java.v2.JavaField',
            path: '/speed',
            status: 'SUPPORTED',
            fieldType: 'INTEGER',
            modifiers: {
              modifier: ['PUBLIC'],
            },
            name: 'speed',
            className: 'int',
            canonicalClassName: 'int',
            primitive: true,
            synthetic: false,
          },
          {
            jsonType: 'io.atlasmap.java.v2.JavaField',
            path: '/serialId',
            status: 'SUPPORTED',
            fieldType: 'STRING',
            modifiers: {
              modifier: ['PUBLIC'],
            },
            name: 'serialId',
            className: 'java.lang.String',
            canonicalClassName: 'java.lang.String',
            primitive: true,
            synthetic: false,
          },
          {
            jsonType: 'io.atlasmap.java.v2.JavaField',
            arrayDimensions: 1,
            collectionType: 'ARRAY',
            path: '/seatHeight',
            status: 'SUPPORTED',
            fieldType: 'FLOAT',
            modifiers: {
              modifier: ['PUBLIC'],
            },
            name: 'seatHeight',
            className: 'float',
            canonicalClassName: 'float',
            primitive: true,
            synthetic: false,
          },
          {
            jsonType: 'io.atlasmap.java.v2.JavaField',
            arrayDimensions: 1,
            collectionType: 'ARRAY',
            path: '/color',
            status: 'SUPPORTED',
            fieldType: 'STRING',
            modifiers: {
              modifier: ['PUBLIC'],
            },
            name: 'color',
            className: 'java.lang.String',
            canonicalClassName: 'java.lang.String',
            primitive: true,
            synthetic: false,
          },
          {
            jsonType: 'io.atlasmap.java.v2.JavaClass',
            path: '/geoLocation',
            status: 'SUPPORTED',
            fieldType: 'COMPLEX',
            modifiers: {
              modifier: ['PUBLIC', 'PUBLIC'],
            },
            name: 'geoLocation',
            className: 'io.paul.GeoLocation',
            canonicalClassName: 'io.paul.GeoLocation',
            primitive: false,
            synthetic: false,
            javaEnumFields: {
              javaEnumField: [],
            },
            javaFields: {
              javaField: [
                {
                  jsonType: 'io.atlasmap.java.v2.JavaField',
                  path: '/geoLocation/lattitude',
                  status: 'SUPPORTED',
                  fieldType: 'DOUBLE',
                  modifiers: {
                    modifier: ['PACKAGE_PRIVATE'],
                  },
                  name: 'lattitude',
                  className: 'double',
                  canonicalClassName: 'double',
                  primitive: true,
                  synthetic: false,
                },
                {
                  jsonType: 'io.atlasmap.java.v2.JavaField',
                  path: '/geoLocation/longitude',
                  status: 'SUPPORTED',
                  fieldType: 'DOUBLE',
                  modifiers: {
                    modifier: ['PACKAGE_PRIVATE'],
                  },
                  name: 'longitude',
                  className: 'double',
                  canonicalClassName: 'double',
                  primitive: true,
                  synthetic: false,
                },
              ],
            },
            packageName: 'io.paul',
            annotation: false,
            annonymous: false,
            enumeration: false,
            localClass: false,
            memberClass: false,
            uri: 'atlas:java?className=io.paul.GeoLocation',
            interface: false,
          },
        ],
      },
      packageName: 'io.paul',
      annotation: false,
      annonymous: false,
      enumeration: false,
      localClass: false,
      memberClass: false,
      uri: 'atlas:java?className=io.paul.Bicycle',
      interface: false,
    },
    executionTime: 247,
  },
};

const XMLSchemaSource: XMLObject = {
  XmlInspectionResponse: {
    jsonType: 'io.atlasmap.xml.v2.XmlInspectionResponse',
    xmlDocument: {
      jsonType: 'io.atlasmap.xml.v2.XmlDocument',
      fields: {
        field: [
          {
            jsonType: 'io.atlasmap.xml.v2.XmlComplexType',
            path: '/tns:request',
            fieldType: 'COMPLEX',
            name: 'tns:request',
            xmlFields: {
              xmlField: [
                {
                  jsonType: 'io.atlasmap.xml.v2.XmlComplexType',
                  path: '/tns:request/tns:body',
                  fieldType: 'COMPLEX',
                  name: 'tns:body',
                  xmlFields: {
                    xmlField: [
                      {
                        jsonType: 'io.atlasmap.xml.v2.XmlComplexType',
                        path: '/tns:request/tns:body/Pet',
                        fieldType: 'COMPLEX',
                        name: 'Pet',
                        xmlFields: {
                          xmlField: [
                            {
                              jsonType: 'io.atlasmap.xml.v2.XmlField',
                              path: '/tns:request/tns:body/Pet/id',
                              fieldType: 'DECIMAL',
                              restrictions: {
                                restriction: [],
                              },
                              name: 'id',
                            },
                            {
                              jsonType: 'io.atlasmap.xml.v2.XmlComplexType',
                              path: '/tns:request/tns:body/Pet/Category',
                              fieldType: 'COMPLEX',
                              name: 'Category',
                              xmlFields: {
                                xmlField: [
                                  {
                                    jsonType: 'io.atlasmap.xml.v2.XmlField',
                                    path:
                                      '/tns:request/tns:body/Pet/Category/id',
                                    fieldType: 'DECIMAL',
                                    restrictions: {
                                      restriction: [],
                                    },
                                    name: 'id',
                                  },
                                  {
                                    jsonType: 'io.atlasmap.xml.v2.XmlField',
                                    path:
                                      '/tns:request/tns:body/Pet/Category/name',
                                    fieldType: 'STRING',
                                    restrictions: {
                                      restriction: [],
                                    },
                                    name: 'name',
                                  },
                                ],
                              },
                            },
                            {
                              jsonType: 'io.atlasmap.xml.v2.XmlField',
                              path: '/tns:request/tns:body/Pet/name',
                              fieldType: 'STRING',
                              restrictions: {
                                restriction: [],
                              },
                              name: 'name',
                            },
                            {
                              jsonType: 'io.atlasmap.xml.v2.XmlComplexType',
                              path: '/tns:request/tns:body/Pet/photoUrl',
                              fieldType: 'COMPLEX',
                              name: 'photoUrl',
                              xmlFields: {
                                xmlField: [
                                  {
                                    jsonType: 'io.atlasmap.xml.v2.XmlField',
                                    path:
                                      '/tns:request/tns:body/Pet/photoUrl/photoUrl',
                                    fieldType: 'STRING',
                                    restrictions: {
                                      restriction: [],
                                    },
                                    name: 'photoUrl',
                                  },
                                ],
                              },
                            },
                            {
                              jsonType: 'io.atlasmap.xml.v2.XmlComplexType',
                              path: '/tns:request/tns:body/Pet/tag',
                              fieldType: 'COMPLEX',
                              name: 'tag',
                              xmlFields: {
                                xmlField: [
                                  {
                                    jsonType:
                                      'io.atlasmap.xml.v2.XmlComplexType',
                                    collectionType: 'LIST',
                                    path: '/tns:request/tns:body/Pet/tag/Tag',
                                    fieldType: 'COMPLEX',
                                    name: 'Tag',
                                    xmlFields: {
                                      xmlField: [
                                        {
                                          jsonType:
                                            'io.atlasmap.xml.v2.XmlField',
                                          path:
                                            '/tns:request/tns:body/Pet/tag/Tag/id',
                                          fieldType: 'DECIMAL',
                                          restrictions: {
                                            restriction: [],
                                          },
                                          name: 'id',
                                        },
                                        {
                                          jsonType:
                                            'io.atlasmap.xml.v2.XmlField',
                                          path:
                                            '/tns:request/tns:body/Pet/tag/Tag/name',
                                          fieldType: 'STRING',
                                          restrictions: {
                                            restriction: [],
                                          },
                                          name: 'name',
                                        },
                                      ],
                                    },
                                  },
                                ],
                              },
                            },
                            {
                              jsonType: 'io.atlasmap.xml.v2.XmlField',
                              path: '/tns:request/tns:body/Pet/status',
                              fieldType: 'STRING',
                              restrictions: {
                                restriction: [],
                              },
                              name: 'status',
                            },
                          ],
                        },
                      },
                    ],
                  },
                },
              ],
            },
          },
        ],
      },
      xmlNamespaces: {
        xmlNamespace: [
          {
            alias: 'tns',
            uri: 'http://syndesis.io/v1/swagger-connector-template/request',
          },
        ],
      },
    },
    executionTime: 65,
  },
};

const XMLInstanceSource: XMLObject = {
  XmlInspectionResponse: {
    jsonType: 'io.atlasmap.xml.v2.XmlInspectionResponse',
    xmlDocument: {
      jsonType: 'io.atlasmap.xml.v2.XmlDocument',
      fields: {
        field: [
          {
            jsonType: 'io.atlasmap.xml.v2.XmlComplexType',
            path: '/ns:XmlOE',
            fieldType: 'COMPLEX',
            name: 'ns:XmlOE',
            typeName:
              'http://atlasmap.io/xml/test/v2 atlas-xml-test-model-v2.xsd ',
            xmlFields: {
              xmlField: [
                {
                  jsonType: 'io.atlasmap.xml.v2.XmlField',
                  value: 'ns:orderId',
                  path: '/ns:XmlOE/ns:orderId',
                  status: 'SUPPORTED',
                  fieldType: 'STRING',
                  name: 'ns:orderId',
                },
                {
                  jsonType: 'io.atlasmap.xml.v2.XmlComplexType',
                  path: '/ns:XmlOE/ns:Address',
                  fieldType: 'COMPLEX',
                  name: 'ns:Address',
                  xmlFields: {
                    xmlField: [
                      {
                        jsonType: 'io.atlasmap.xml.v2.XmlField',
                        value: 'ns:addressLine1',
                        path: '/ns:XmlOE/ns:Address/ns:addressLine1',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'ns:addressLine1',
                      },
                      {
                        jsonType: 'io.atlasmap.xml.v2.XmlField',
                        value: 'ns:addressLine2',
                        path: '/ns:XmlOE/ns:Address/ns:addressLine2',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'ns:addressLine2',
                      },
                      {
                        jsonType: 'io.atlasmap.xml.v2.XmlField',
                        value: 'ns:city',
                        path: '/ns:XmlOE/ns:Address/ns:city',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'ns:city',
                      },
                      {
                        jsonType: 'io.atlasmap.xml.v2.XmlField',
                        value: 'ns:state',
                        path: '/ns:XmlOE/ns:Address/ns:state',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'ns:state',
                      },
                      {
                        jsonType: 'io.atlasmap.xml.v2.XmlField',
                        value: 'ns:zipCode',
                        path: '/ns:XmlOE/ns:Address/ns:zipCode',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'ns:zipCode',
                      },
                    ],
                  },
                },
                {
                  jsonType: 'io.atlasmap.xml.v2.XmlComplexType',
                  path: '/ns:XmlOE/ns:Contact',
                  fieldType: 'COMPLEX',
                  name: 'ns:Contact',
                  xmlFields: {
                    xmlField: [
                      {
                        jsonType: 'io.atlasmap.xml.v2.XmlField',
                        value: 'ns:firstName',
                        path: '/ns:XmlOE/ns:Contact/ns:firstName',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'ns:firstName',
                      },
                      {
                        jsonType: 'io.atlasmap.xml.v2.XmlField',
                        value: 'ns:lastName',
                        path: '/ns:XmlOE/ns:Contact/ns:lastName',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'ns:lastName',
                      },
                      {
                        jsonType: 'io.atlasmap.xml.v2.XmlField',
                        value: 'ns:phoneNumber',
                        path: '/ns:XmlOE/ns:Contact/ns:phoneNumber',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'ns:phoneNumber',
                      },
                      {
                        jsonType: 'io.atlasmap.xml.v2.XmlField',
                        value: 'ns:zipCode',
                        path: '/ns:XmlOE/ns:Contact/ns:zipCode',
                        status: 'SUPPORTED',
                        fieldType: 'STRING',
                        name: 'ns:zipCode',
                      },
                    ],
                  },
                },
              ],
            },
          },
        ],
      },
      xmlNamespaces: {
        xmlNamespace: [
          {
            alias: 'xsi',
            uri: 'http://www.w3.org/2001/XMLSchema-instance',
          },
          {
            alias: 'ns',
            uri: 'http://atlasmap.io/xml/test/v2',
          },
        ],
      },
    },
    executionTime: 26,
  },
};

export const sources = [
  {
    id: 'JSONInstanceSource',
    name: 'JSONInstanceSource',
    type: 'JSON',
    fields: jsonToFieldGroup(mockJSONInstanceSource, 'JSONInstanceSource'),
  },
  {
    id: 'JSONSchemaSource',
    name: 'JSONSchemaSource',
    type: 'JSON',
    fields: jsonToFieldGroup(mockJSONSchemaSource, 'JSONSchemaSource'),
  },
];

export const targets = [
  {
    id: 'XMLInstanceSource',
    name: 'XMLInstanceSource',
    type: 'XML',
    fields: xmlToFieldGroup(XMLInstanceSource, 'XMLInstanceSource'),
  },
  {
    id: 'XMLSchemaSource',
    name: 'XMLSchemaSource',
    type: 'XML',
    fields: xmlToFieldGroup(XMLSchemaSource, 'XMLSchemaSource'),
  },
  {
    id: 'io.paul.Bicycle',
    name: 'io.paul.Bicycle',
    type: 'JAVA',
    fields: javaToFieldGroup(ioPaulBicycle, 'io.paul.Bicycle'),
  },
];

export const mappings = [
  {
    id: 'a',
    name: 'Lorem',
    sourceFields: [
      {
        id: 'JSONInstanceSource-/order/address/city',
        name: 'city',
        tip: '/order/address/city',
      },
      {
        id: 'JSONInstanceSource-/order/address/state',
        name: 'state',
        tip: '/order/address/state',
      },
    ],
    targetFields: [
      {
        id: 'XMLInstanceSource-/ns:XmlOE/ns:Address/ns:addressLine1',
        name: 'ns:addressLine1',
        tip: '/ns:XmlOE/ns:Address/ns:addressLine1',
      },
    ],
  },
  {
    id: 'b',
    name: 'Lorem',
    sourceFields: [
      {
        id: 'JSONInstanceSource-/primitives/numberPrimitive',
        name: 'numberPrimitive',
        tip: '/primitives/numberPrimitive',
      },
    ],
    targetFields: [
      {
        id: 'io.paul.Bicycle-/cadence',
        name: 'cadence',
        tip: '/cadence'
      },
      {
        id: 'io.paul.Bicycle-/gear',
        name: 'gear',
        tip: '/gear'
      },
      {
        id: 'io.paul.Bicycle-/speed',
        name: 'speed',
        tip: '/speed'
      },
    ],
  },
  {
    id: 'c',
    name: 'Lorem',
    sourceFields: [
      {
        id: 'JSONInstanceSource-/primitives/stringPrimitive',
        name: 'stringPrimitive',
        tip: '/primitives/stringPrimitive',
      },
      {
        id: 'JSONInstanceSource-/order/orderId',
        name: 'orderId',
        tip: '/order/orderId',
      },
      {
        id: 'JSONSchemaSource-/primitives/stringPrimitive',
        name: 'stringPrimitive',
        tip: '/primitives/stringPrimitive',
      },
    ],
    targetFields: [
      {
        id: 'io.paul.Bicycle-/serialId',
        name: 'serialId',
        tip: '/serialId',
      },
    ],
  },
];
