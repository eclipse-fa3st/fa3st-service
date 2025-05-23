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
package org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.provider;

import java.util.Arrays;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidConfigurationException;
import org.eclipse.digitaltwin.fa3st.common.util.Ensure;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.conversion.ValueConverter;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.provider.config.AbstractOpcUaProviderWithArrayConfig;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.util.ArrayHelper;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.core.nodes.VariableNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;


/**
 * Superclass for all OPC UA provider classes.
 *
 * @param <T> type of the asset provider config
 */
public abstract class AbstractOpcUaProviderWithArray<T extends AbstractOpcUaProviderWithArrayConfig> extends AbstractOpcUaProvider<T> {

    protected int[] arrayIndex;
    protected VariableNode node;

    protected AbstractOpcUaProviderWithArray(ServiceContext serviceContext,
            OpcUaClient client,
            Reference reference,
            T providerConfig,
            ValueConverter valueConverter) throws InvalidConfigurationException, AssetConnectionException {
        super(serviceContext, client, reference, providerConfig, valueConverter);
        arrayIndex = ArrayHelper.parseArrayIndex(providerConfig.getArrayIndex());
        validateArrayIndex();
    }


    private void validateArrayIndex() throws InvalidConfigurationException {
        Ensure.require(
                Objects.equals(NodeClass.Variable, super.node.getNodeClass())
                        && VariableNode.class.isAssignableFrom(super.node.getClass()),
                new InvalidConfigurationException(
                        String.format("nodeId does not point to a variable node (nodeId: %s, node type: %s)",
                                providerConfig.getNodeId(),
                                super.node.getNodeClass())));
        this.node = (VariableNode) super.node;
        UInteger[] actualArrayDimensions = node.getArrayDimensions();
        if (ArrayHelper.isValidArrayIndex(arrayIndex) && arrayIndex.length > actualArrayDimensions.length) {
            throw new InvalidConfigurationException(
                    String.format("provided array index has more dimensions than the corresponding node (provided dimensions: %d, actual dimensions: %d, nodeId: %s)",
                            arrayIndex.length,
                            actualArrayDimensions.length,
                            providerConfig.getNodeId()));
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), node, Arrays.hashCode(arrayIndex));
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractOpcUaProviderWithArray)) {
            return false;
        }
        final AbstractOpcUaProviderWithArray<?> that = (AbstractOpcUaProviderWithArray<?>) obj;
        return super.equals(obj)
                && Objects.equals(node, that.node)
                && Arrays.equals(arrayIndex, that.arrayIndex);
    }

}
