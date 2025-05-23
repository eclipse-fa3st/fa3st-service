/**
 * Copyright (c) 2025 the Eclipse FA³ST Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.server;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ulong;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ushort;

import java.lang.reflect.Array;
import java.util.List;
import java.util.UUID;
import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.core.ValueRank;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespaceWithLifecycle;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.model.nodes.variables.AnalogItemTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.nodes.factories.NodeFactory;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.XmlElement;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExampleNamespace extends ManagedNamespaceWithLifecycle {

    public static final String NAMESPACE_URI = "urn:eclipse:milo:hello-world";

    private static final Object[][] STATIC_SCALAR_NODES = new Object[][] {
            {
                    "Boolean",
                    Identifiers.Boolean,
                    new Variant(false)
            },
            {
                    "Byte",
                    Identifiers.Byte,
                    new Variant(ubyte(0x00))
            },
            {
                    "SByte",
                    Identifiers.SByte,
                    new Variant((byte) 0x00)
            },
            {
                    "Integer",
                    Identifiers.Integer,
                    new Variant(32)
            },
            {
                    "Int16",
                    Identifiers.Int16,
                    new Variant((short) 16)
            },
            {
                    "Int32",
                    Identifiers.Int32,
                    new Variant(32)
            },
            {
                    "Int64",
                    Identifiers.Int64,
                    new Variant(64L)
            },
            {
                    "UInteger",
                    Identifiers.UInteger,
                    new Variant(uint(32))
            },
            {
                    "UInt16",
                    Identifiers.UInt16,
                    new Variant(ushort(16))
            },
            {
                    "UInt32",
                    Identifiers.UInt32,
                    new Variant(uint(32))
            },
            {
                    "UInt64",
                    Identifiers.UInt64,
                    new Variant(ulong(64L))
            },
            {
                    "Float",
                    Identifiers.Float,
                    new Variant(3.14f)
            },
            {
                    "Double",
                    Identifiers.Double,
                    new Variant(3.14d)
            },
            {
                    "String",
                    Identifiers.String,
                    new Variant("string value")
            },
            {
                    "DateTime",
                    Identifiers.DateTime,
                    new Variant(DateTime.now())
            },
            {
                    "Guid",
                    Identifiers.Guid,
                    new Variant(UUID.randomUUID())
            },
            {
                    "ByteString",
                    Identifiers.ByteString,
                    new Variant(new ByteString(new byte[] {
                            0x01,
                            0x02,
                            0x03,
                            0x04
                    }))
            },
            {
                    "XmlElement",
                    Identifiers.XmlElement,
                    new Variant(new XmlElement("<a>hello</a>"))
            },
            {
                    "LocalizedText",
                    Identifiers.LocalizedText,
                    new Variant(LocalizedText.english("localized text"))
            },
            {
                    "QualifiedName",
                    Identifiers.QualifiedName,
                    new Variant(new QualifiedName(1234, "defg"))
            },
            {
                    "NodeId",
                    Identifiers.NodeId,
                    new Variant(new NodeId(1234, "abcd"))
            },
            {
                    "Variant",
                    Identifiers.BaseDataType,
                    new Variant(32)
            },
            {
                    "Duration",
                    Identifiers.Duration,
                    new Variant(1.0)
            },
            {
                    "UtcTime",
                    Identifiers.UtcTime,
                    new Variant(DateTime.now())
            },
            {
                    "DateString",
                    Identifiers.String,
                    new Variant("2000-01-01+12:05")
            },
            {
                    "TimeString",
                    Identifiers.String,
                    new Variant("14:23:00.527634+03:00")
            },
            {
                    "DateTimeString",
                    Identifiers.String,
                    new Variant("2000-01-01T14:23:00.66372+14:00")
            },
            {
                    "YearString",
                    Identifiers.String,
                    new Variant("2000+03:00")
            },
            {
                    "MonthString",
                    Identifiers.String,
                    new Variant("--02+03:00")
            },
            {
                    "DayString",
                    Identifiers.String,
                    new Variant("---04+03:00")
            },
            {
                    "YearMonthString",
                    Identifiers.String,
                    new Variant("2000-02+03:00")
            },
            {
                    "MonthDayString",
                    Identifiers.String,
                    new Variant("--02-04+03:00")
            },
            {
                    "DurationString",
                    Identifiers.String,
                    new Variant("-P1Y2M3DT1H")
            }
    };

    private static final Object[][] STATIC_ARRAY_NODES = new Object[][] {
            {
                    "BooleanArray",
                    Identifiers.Boolean,
                    false
            },
            {
                    "ByteArray",
                    Identifiers.Byte,
                    ubyte(0)
            },
            {
                    "SByteArray",
                    Identifiers.SByte,
                    (byte) 0x00
            },
            {
                    "Int16Array",
                    Identifiers.Int16,
                    (short) 16
            },
            {
                    "Int32Array",
                    Identifiers.Int32,
                    32
            },
            {
                    "Int64Array",
                    Identifiers.Int64,
                    64L
            },
            {
                    "UInt16Array",
                    Identifiers.UInt16,
                    ushort(16)
            },
            {
                    "UInt32Array",
                    Identifiers.UInt32,
                    uint(32)
            },
            {
                    "UInt64Array",
                    Identifiers.UInt64,
                    ulong(64L)
            },
            {
                    "FloatArray",
                    Identifiers.Float,
                    3.14f
            },
            {
                    "DoubleArray",
                    Identifiers.Double,
                    3.14d
            },
            {
                    "StringArray",
                    Identifiers.String,
                    "string value"
            },
            {
                    "DateTimeArray",
                    Identifiers.DateTime,
                    DateTime.now()
            },
            {
                    "GuidArray",
                    Identifiers.Guid,
                    UUID.randomUUID()
            },
            {
                    "ByteStringArray",
                    Identifiers.ByteString,
                    new ByteString(new byte[] {
                            0x01,
                            0x02,
                            0x03,
                            0x04
                    })
            },
            {
                    "XmlElementArray",
                    Identifiers.XmlElement,
                    new XmlElement("<a>hello</a>")
            },
            {
                    "LocalizedTextArray",
                    Identifiers.LocalizedText,
                    LocalizedText.english("localized text")
            },
            {
                    "QualifiedNameArray",
                    Identifiers.QualifiedName,
                    new QualifiedName(1234, "defg")
            },
            {
                    "NodeIdArray",
                    Identifiers.NodeId,
                    new NodeId(1234, "abcd")
            }
    };

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final SubscriptionModel subscriptionModel;

    ExampleNamespace(OpcUaServer server) {
        super(server, NAMESPACE_URI);
        subscriptionModel = new SubscriptionModel(server, this);
        getLifecycleManager().addLifecycle(subscriptionModel);
        getLifecycleManager().addStartupTask(this::createAndAddNodes);
    }


    private void createAndAddNodes() {
        // Create a "HelloWorld" folder and add it to the node manager
        NodeId folderNodeId = newNodeId("HelloWorld");
        UaFolderNode folderNode = new UaFolderNode(
                getNodeContext(),
                folderNodeId,
                newQualifiedName("HelloWorld"),
                LocalizedText.english("HelloWorld"));
        getNodeManager().addNode(folderNode);
        // Make sure our new folder shows up under the server's Objects folder.
        folderNode.addReference(new Reference(
                folderNode.getNodeId(),
                Identifiers.Organizes,
                Identifiers.ObjectsFolder.expanded(),
                false));

        // Add the rest of the nodes
        addVariableNodes(folderNode);
        addSqrtMethod(folderNode);
    }


    private void addVariableNodes(UaFolderNode rootNode) {
        addArrayNodes(rootNode);
        addScalarNodes(rootNode);
        addAdminReadableNodes(rootNode);
        addAdminWritableNodes(rootNode);
        addDynamicNodes(rootNode);
        addDataAccessNodes(rootNode);
        addWriteOnlyNodes(rootNode);
        addMatrixNodes(rootNode);
    }


    private void addMatrixNodes(UaFolderNode rootNode) {
        UaFolderNode arrayTypesFolder = new UaFolderNode(
                getNodeContext(),
                newNodeId("HelloWorld/MatrixTypes"),
                newQualifiedName("MatrixTypes"),
                LocalizedText.english("MatrixTypes"));

        getNodeManager().addNode(arrayTypesFolder);
        rootNode.addOrganizes(arrayTypesFolder);

        for (Object[] os: STATIC_ARRAY_NODES) {
            String name = (String) os[0];
            NodeId typeId = (NodeId) os[1];
            Object value = os[2];
            Object[][] array = (Object[][]) Array.newInstance(value.getClass(), 5, 5);
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    //Array.set(array, i, value);
                    array[i][j] = value;
                }
            }
            Variant variant = new Variant(array);

            UaVariableNode.build(getNodeContext(), builder -> {
                builder.setNodeId(newNodeId("HelloWorld/MatrixTypes/" + name));
                builder.setAccessLevel(AccessLevel.READ_WRITE);
                builder.setUserAccessLevel(AccessLevel.READ_WRITE);
                builder.setBrowseName(newQualifiedName(name));
                builder.setDisplayName(LocalizedText.english(name));
                builder.setDataType(typeId);
                builder.setTypeDefinition(Identifiers.BaseDataVariableType);
                builder.setValueRank(ValueRank.OneOrMoreDimensions.getValue());
                builder.setArrayDimensions(new UInteger[] {
                        uint(0),
                        uint(0)
                });
                builder.setValue(new DataValue(variant));

                builder.addReference(new Reference(
                        builder.getNodeId(),
                        Identifiers.Organizes,
                        arrayTypesFolder.getNodeId().expanded(),
                        Reference.Direction.INVERSE));

                return builder.buildAndAdd();
            });
        }

    }


    private void addArrayNodes(UaFolderNode rootNode) {
        UaFolderNode arrayTypesFolder = new UaFolderNode(
                getNodeContext(),
                newNodeId("HelloWorld/ArrayTypes"),
                newQualifiedName("ArrayTypes"),
                LocalizedText.english("ArrayTypes"));
        getNodeManager().addNode(arrayTypesFolder);
        rootNode.addOrganizes(arrayTypesFolder);
        for (Object[] os: STATIC_ARRAY_NODES) {
            String name = (String) os[0];
            NodeId typeId = (NodeId) os[1];
            Object value = os[2];
            Object array = Array.newInstance(value.getClass(), 5);
            for (int i = 0; i < 5; i++) {
                Array.set(array, i, value);
            }
            Variant variant = new Variant(array);
            UaVariableNode.build(getNodeContext(), builder -> {
                builder.setNodeId(newNodeId("HelloWorld/ArrayTypes/" + name));
                builder.setAccessLevel(AccessLevel.READ_WRITE);
                builder.setUserAccessLevel(AccessLevel.READ_WRITE);
                builder.setBrowseName(newQualifiedName(name));
                builder.setDisplayName(LocalizedText.english(name));
                builder.setDataType(typeId);
                builder.setTypeDefinition(Identifiers.BaseDataVariableType);
                builder.setValueRank(ValueRank.OneDimension.getValue());
                builder.setArrayDimensions(new UInteger[] {
                        uint(0)
                });
                builder.setValue(new DataValue(variant));
                builder.addReference(new Reference(
                        builder.getNodeId(),
                        Identifiers.Organizes,
                        arrayTypesFolder.getNodeId().expanded(),
                        Reference.Direction.INVERSE));
                return builder.buildAndAdd();
            });
        }
    }


    private void addScalarNodes(UaFolderNode rootNode) {
        UaFolderNode scalarTypesFolder = new UaFolderNode(
                getNodeContext(),
                newNodeId("HelloWorld/ScalarTypes"),
                newQualifiedName("ScalarTypes"),
                LocalizedText.english("ScalarTypes"));
        getNodeManager().addNode(scalarTypesFolder);
        rootNode.addOrganizes(scalarTypesFolder);
        for (Object[] os: STATIC_SCALAR_NODES) {
            String name = (String) os[0];
            NodeId typeId = (NodeId) os[1];
            Variant variant = (Variant) os[2];
            UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                    .setNodeId(newNodeId("HelloWorld/ScalarTypes/" + name))
                    .setAccessLevel(AccessLevel.READ_WRITE)
                    .setUserAccessLevel(AccessLevel.READ_WRITE)
                    .setBrowseName(newQualifiedName(name))
                    .setDisplayName(LocalizedText.english(name))
                    .setDataType(typeId)
                    .setTypeDefinition(Identifiers.BaseDataVariableType)
                    .build();
            node.setValue(new DataValue(variant));
            getNodeManager().addNode(node);
            scalarTypesFolder.addOrganizes(node);
        }
    }


    private void addWriteOnlyNodes(UaFolderNode rootNode) {
        UaFolderNode writeOnlyFolder = new UaFolderNode(
                getNodeContext(),
                newNodeId("HelloWorld/WriteOnly"),
                newQualifiedName("WriteOnly"),
                LocalizedText.english("WriteOnly"));
        getNodeManager().addNode(writeOnlyFolder);
        rootNode.addOrganizes(writeOnlyFolder);
        String name = "String";
        UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                .setNodeId(newNodeId("HelloWorld/WriteOnly/" + name))
                .setAccessLevel(AccessLevel.WRITE_ONLY)
                .setUserAccessLevel(AccessLevel.WRITE_ONLY)
                .setBrowseName(newQualifiedName(name))
                .setDisplayName(LocalizedText.english(name))
                .setDataType(Identifiers.String)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();
        node.setValue(new DataValue(new Variant("can't read this")));
        getNodeManager().addNode(node);
        writeOnlyFolder.addOrganizes(node);
    }


    private void addAdminReadableNodes(UaFolderNode rootNode) {
        UaFolderNode adminFolder = new UaFolderNode(
                getNodeContext(),
                newNodeId("HelloWorld/OnlyAdminCanRead"),
                newQualifiedName("OnlyAdminCanRead"),
                LocalizedText.english("OnlyAdminCanRead"));
        getNodeManager().addNode(adminFolder);
        rootNode.addOrganizes(adminFolder);
        String name = "String";
        UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                .setNodeId(newNodeId("HelloWorld/OnlyAdminCanRead/" + name))
                .setAccessLevel(AccessLevel.READ_WRITE)
                .setBrowseName(newQualifiedName(name))
                .setDisplayName(LocalizedText.english(name))
                .setDataType(Identifiers.String)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();
        node.setValue(new DataValue(new Variant("shh... don't tell the lusers")));
        getNodeManager().addNode(node);
        adminFolder.addOrganizes(node);
    }


    private void addAdminWritableNodes(UaFolderNode rootNode) {
        UaFolderNode adminFolder = new UaFolderNode(
                getNodeContext(),
                newNodeId("HelloWorld/OnlyAdminCanWrite"),
                newQualifiedName("OnlyAdminCanWrite"),
                LocalizedText.english("OnlyAdminCanWrite"));
        getNodeManager().addNode(adminFolder);
        rootNode.addOrganizes(adminFolder);
        String name = "String";
        UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                .setNodeId(newNodeId("HelloWorld/OnlyAdminCanWrite/" + name))
                .setAccessLevel(AccessLevel.READ_WRITE)
                .setBrowseName(newQualifiedName(name))
                .setDisplayName(LocalizedText.english(name))
                .setDataType(Identifiers.String)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();
        node.setValue(new DataValue(new Variant("admin was here")));
        getNodeManager().addNode(node);
        adminFolder.addOrganizes(node);
    }


    private void addDynamicNodes(UaFolderNode rootNode) {
        UaFolderNode dynamicFolder = new UaFolderNode(
                getNodeContext(),
                newNodeId("HelloWorld/Dynamic"),
                newQualifiedName("Dynamic"),
                LocalizedText.english("Dynamic"));
        getNodeManager().addNode(dynamicFolder);
        rootNode.addOrganizes(dynamicFolder);
        // Dynamic Boolean
        {
            String name = "Boolean";
            NodeId typeId = Identifiers.Boolean;
            Variant variant = new Variant(false);
            UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                    .setNodeId(newNodeId("HelloWorld/Dynamic/" + name))
                    .setAccessLevel(AccessLevel.READ_WRITE)
                    .setBrowseName(newQualifiedName(name))
                    .setDisplayName(LocalizedText.english(name))
                    .setDataType(typeId)
                    .setTypeDefinition(Identifiers.BaseDataVariableType)
                    .build();
            node.setValue(new DataValue(variant));
            getNodeManager().addNode(node);
            dynamicFolder.addOrganizes(node);
        }
        // Dynamic Int32
        {
            String name = "Int32";
            NodeId typeId = Identifiers.Int32;
            Variant variant = new Variant(0);
            UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                    .setNodeId(newNodeId("HelloWorld/Dynamic/" + name))
                    .setAccessLevel(AccessLevel.READ_WRITE)
                    .setBrowseName(newQualifiedName(name))
                    .setDisplayName(LocalizedText.english(name))
                    .setDataType(typeId)
                    .setTypeDefinition(Identifiers.BaseDataVariableType)
                    .build();
            node.setValue(new DataValue(variant));
            getNodeManager().addNode(node);
            dynamicFolder.addOrganizes(node);
        }
        // Dynamic Double
        {
            String name = "Double";
            NodeId typeId = Identifiers.Double;
            Variant variant = new Variant(0.0);
            UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                    .setNodeId(newNodeId("HelloWorld/Dynamic/" + name))
                    .setAccessLevel(AccessLevel.READ_WRITE)
                    .setBrowseName(newQualifiedName(name))
                    .setDisplayName(LocalizedText.english(name))
                    .setDataType(typeId)
                    .setTypeDefinition(Identifiers.BaseDataVariableType)
                    .build();
            node.setValue(new DataValue(variant));
            getNodeManager().addNode(node);
            dynamicFolder.addOrganizes(node);
        }
    }


    private void addDataAccessNodes(UaFolderNode rootNode) {
        // DataAccess folder
        UaFolderNode dataAccessFolder = new UaFolderNode(
                getNodeContext(),
                newNodeId("HelloWorld/DataAccess"),
                newQualifiedName("DataAccess"),
                LocalizedText.english("DataAccess"));
        getNodeManager().addNode(dataAccessFolder);
        rootNode.addOrganizes(dataAccessFolder);
        try {
            AnalogItemTypeNode node = (AnalogItemTypeNode) getNodeFactory().createNode(
                    newNodeId("HelloWorld/DataAccess/AnalogValue"),
                    Identifiers.AnalogItemType,
                    new NodeFactory.InstantiationCallback() {
                        @Override
                        public boolean includeOptionalNode(NodeId typeDefinitionId, QualifiedName browseName) {
                            return true;
                        }
                    });
            node.setBrowseName(newQualifiedName("AnalogValue"));
            node.setDisplayName(LocalizedText.english("AnalogValue"));
            node.setDataType(Identifiers.Double);
            node.setValue(new DataValue(new Variant(3.14d)));
            node.setEURange(new Range(0.0, 100.0));
            getNodeManager().addNode(node);
            dataAccessFolder.addOrganizes(node);
        }
        catch (UaException e) {
            LOGGER.error("Error creating AnalogItemType instance: {}", e.getMessage(), e);
        }
    }


    private void addSqrtMethod(UaFolderNode folderNode) {
        UaMethodNode methodNode = UaMethodNode.builder(getNodeContext())
                .setNodeId(newNodeId("HelloWorld/sqrt(x)"))
                .setBrowseName(newQualifiedName("sqrt(x)"))
                .setDisplayName(new LocalizedText(null, "sqrt(x)"))
                .setDescription(
                        LocalizedText.english("Returns the correctly rounded positive square root of a double value."))
                .build();
        SqrtMethod sqrtMethod = new SqrtMethod(methodNode);
        methodNode.setInputArguments(sqrtMethod.getInputArguments());
        methodNode.setOutputArguments(sqrtMethod.getOutputArguments());
        methodNode.setInvocationHandler(sqrtMethod);
        getNodeManager().addNode(methodNode);
        methodNode.addReference(new Reference(
                methodNode.getNodeId(),
                Identifiers.HasComponent,
                folderNode.getNodeId().expanded(),
                false));
    }


    @Override
    public void onDataItemsCreated(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsCreated(dataItems);
    }


    @Override
    public void onDataItemsModified(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsModified(dataItems);
    }


    @Override
    public void onDataItemsDeleted(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsDeleted(dataItems);
    }


    @Override
    public void onMonitoringModeChanged(List<MonitoredItem> monitoredItems) {
        subscriptionModel.onMonitoringModeChanged(monitoredItems);
    }

}
