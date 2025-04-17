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
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidConfigurationException;
import org.eclipse.digitaltwin.fa3st.common.util.Ensure;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetSubscriptionProvider;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.NewDataListener;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.conversion.ValueConverter;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.provider.config.OpcUaSubscriptionProviderConfig;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;


/**
 * Implementation of SubscriptionProvider for OPC UA asset connections. Supports subscribing to OPC UA.
 */
public class OpcUaSubscriptionProvider extends AbstractOpcUaProviderWithArray<OpcUaSubscriptionProviderConfig> implements AssetSubscriptionProvider {

    private ManagedSubscription opcUaSubscription;
    private SubscriptionMultiplexer multiplexer = null;

    public OpcUaSubscriptionProvider(ServiceContext serviceContext,
            Reference reference,
            OpcUaSubscriptionProviderConfig providerConfig,
            OpcUaClient client,
            ManagedSubscription opcUaSubscription,
            ValueConverter valueConverter) throws InvalidConfigurationException, AssetConnectionException {
        super(serviceContext, client, reference, providerConfig, valueConverter);
        Ensure.requireNonNull(opcUaSubscription, "opcUaSubscription must be non-null");
        this.opcUaSubscription = opcUaSubscription;
    }


    public String getNodeId() {
        return providerConfig.getNodeId();
    }


    public Reference getReference() {
        return reference;
    }


    /**
     * Reconnects underlying subscriptions after connection loss.
     *
     * @param client the new client
     * @param opcUaSubscription the new underlying OPC UA subscription
     * @throws AssetConnectionException if reconnecting fails
     */
    public void reconnect(OpcUaClient client, ManagedSubscription opcUaSubscription) throws AssetConnectionException {
        this.client = client;
        this.opcUaSubscription = opcUaSubscription;
        if (multiplexer != null) {
            multiplexer.reconnect(client, opcUaSubscription);
        }
    }


    @Override
    public void addNewDataListener(NewDataListener listener) throws AssetConnectionException {
        if (multiplexer == null) {
            multiplexer = new SubscriptionMultiplexer(
                    serviceContext,
                    reference,
                    providerConfig,
                    client,
                    opcUaSubscription,
                    valueConverter);
        }
        multiplexer.addListener(listener);
    }


    /**
     * Ends all OPC UA subscriptions.
     *
     * @throws AssetConnectionException if unsubscribing via OPC UA fails
     */
    public void close() throws AssetConnectionException {
        if (multiplexer != null) {
            multiplexer.close();
        }
    }


    @Override
    public void removeNewDataListener(NewDataListener listener) throws AssetConnectionException {
        if (multiplexer != null) {
            try {
                multiplexer.removeListener(listener);
                if (!multiplexer.isActive()) {
                    multiplexer = null;
                }
            }
            catch (AssetConnectionException e) {
                throw new AssetConnectionException(
                        String.format("Removing subscription failed (reference: %s, nodeId: %s)",
                                ReferenceHelper.toString(reference),
                                providerConfig.getNodeId()),
                        e);
            }
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), opcUaSubscription, multiplexer);
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
        final OpcUaSubscriptionProvider that = (OpcUaSubscriptionProvider) obj;
        return super.equals(that)
                && Objects.equals(opcUaSubscription, that.opcUaSubscription)
                && Objects.equals(multiplexer, that.multiplexer);
    }

}
