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

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidConfigurationException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.model.value.DataElementValue;
import org.eclipse.digitaltwin.fa3st.common.model.value.Datatype;
import org.eclipse.digitaltwin.fa3st.common.model.value.PropertyValue;
import org.eclipse.digitaltwin.fa3st.common.typing.ElementValueTypeInfo;
import org.eclipse.digitaltwin.fa3st.common.typing.TypeInfo;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetValueProvider;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.conversion.ValueConversionException;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.conversion.ValueConverter;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.provider.config.OpcUaValueProviderConfig;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.util.ArrayHelper;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.util.OpcUaHelper;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;


/**
 * Implementation of ValueProvider for OPC UA asset connections. Supports reading and writing values from/to OPC UA.
 */
public class OpcUaValueProvider extends AbstractOpcUaProviderWithArray<OpcUaValueProviderConfig> implements AssetValueProvider {

    private Datatype datatype;

    public OpcUaValueProvider(ServiceContext serviceContext,
            OpcUaClient client,
            Reference reference,
            OpcUaValueProviderConfig providerConfig,
            ValueConverter valueConverter) throws AssetConnectionException, InvalidConfigurationException {
        super(serviceContext, client, reference, providerConfig, valueConverter);
        init();
    }


    private void init() throws AssetConnectionException {
        final String baseErrorMessage = "error registering value provider";
        TypeInfo<?> typeInfo;
        try {
            typeInfo = serviceContext.getTypeInfo(reference);
        }
        catch (ResourceNotFoundException | PersistenceException ex) {
            throw new AssetConnectionException(
                    String.format("%s - could not resolve type information (reference: %s)",
                            baseErrorMessage,
                            ReferenceHelper.toString(reference)));
        }
        if (typeInfo == null) {
            throw new AssetConnectionException(
                    String.format("%s - could not resolve type information (reference: %s)",
                            baseErrorMessage,
                            ReferenceHelper.toString(reference)));
        }
        if (!ElementValueTypeInfo.class.isAssignableFrom(typeInfo.getClass())) {
            throw new AssetConnectionException(
                    String.format("%s - reference must point to element with value (reference: %s)",
                            baseErrorMessage,
                            ReferenceHelper.toString(reference)));
        }
        ElementValueTypeInfo valueTypeInfo = (ElementValueTypeInfo) typeInfo;
        if (!PropertyValue.class.isAssignableFrom(valueTypeInfo.getType())) {
            throw new AssetConnectionException(String.format("%s - unsupported element type (reference: %s, element type: %s)",
                    baseErrorMessage,
                    ReferenceHelper.toString(reference),
                    valueTypeInfo.getType()));
        }
        datatype = valueTypeInfo.getDatatype();
        if (datatype == null) {
            throw new AssetConnectionException(String.format("%s - missing datatype (reference: %s)",
                    baseErrorMessage,
                    ReferenceHelper.toString(reference)));
        }
    }


    @Override
    public DataElementValue getValue() throws AssetConnectionException {
        try {
            DataValue dataValue = client.readValue(0, TimestampsToReturn.Neither, node.getNodeId()).get();
            OpcUaHelper.checkStatusCode(dataValue.getStatusCode(), "error reading value from asset conenction");
            return new PropertyValue(valueConverter.convert(ArrayHelper.unwrapValue(dataValue, arrayIndex), datatype));
        }
        catch (InterruptedException | ExecutionException | ValueConversionException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException(String.format("error reading value from asset conenction (reference: %s)", ReferenceHelper.toString(reference)), e);
        }
    }


    @Override
    public void setValue(DataElementValue value) throws AssetConnectionException {
        if (value == null) {
            throw new AssetConnectionException(
                    String.format("error setting value on asset connection - value must be non-null (reference: %s)", ReferenceHelper.toString(reference)));
        }
        if (!PropertyValue.class.isAssignableFrom(value.getClass())) {
            throw new AssetConnectionException(String.format("error setting value on asset connection - unsupported element type (reference: %s, element type: %s)",
                    ReferenceHelper.toString(reference),
                    value.getClass()));
        }
        try {
            Variant valueToWrite = valueConverter.convert(((PropertyValue) value).getValue(), node.getDataType());
            if (ArrayHelper.isValidArrayIndex(providerConfig.getArrayIndex())) {
                valueToWrite = ArrayHelper.wrapValue(
                        client.readValue(0, TimestampsToReturn.Neither, node.getNodeId()).get(),
                        valueToWrite,
                        arrayIndex);
            }
            StatusCode result = client.writeValue(node.getNodeId(), new DataValue(
                    valueToWrite,
                    null,
                    null)).get();
            OpcUaHelper.checkStatusCode(result, "error setting value on asset connection");
        }
        catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException("error writing asset connection value", e);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), node, datatype);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OpcUaValueProvider that = (OpcUaValueProvider) obj;
        return super.equals(that)
                && Objects.equals(node, that.node)
                && Objects.equals(datatype, that.datatype);
    }
}
