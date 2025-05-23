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
package org.eclipse.digitaltwin.fa3st.service.config.fixtures;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetConnection;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetOperationProvider;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetSubscriptionProvider;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetValueProvider;
import org.eclipse.digitaltwin.fa3st.service.config.CoreConfig;


public class DummyAssetConnection
        implements
        AssetConnection<DummyAssetConnectionConfig, DummyNodeBasedProviderConfig, AssetValueProvider, DummyNodeBasedProviderConfig, AssetOperationProvider, DummySubscriptionBasedProviderConfig, AssetSubscriptionProvider> {

    private CoreConfig coreConfig;
    private final Map<Reference, AssetValueProvider> valueProviders = new TreeMap<>();
    private final Map<Reference, AssetOperationProvider> operationProviders = new TreeMap<>();
    private final Map<Reference, AssetSubscriptionProvider> subscriptionProviders = new TreeMap<>();
    private String host;
    private int port;

    private volatile boolean connected = false;

    public void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public void connect() throws AssetConnectionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public void disconnect() throws AssetConnectionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public String getEndpointInformation() {
        return "";
    }


    @Override
    public boolean isConnected() {
        return connected;
    }


    @Override
    public void registerValueProvider(Reference reference, DummyNodeBasedProviderConfig valueProvider) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void registerOperationProvider(Reference reference, DummyNodeBasedProviderConfig operationProvider) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void registerSubscriptionProvider(Reference reference, DummySubscriptionBasedProviderConfig subscriptionProvider) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void unregisterOperationProvider(Reference reference) {
        operationProviders.remove(reference);
    }


    @Override
    public void unregisterSubscriptionProvider(Reference reference) {
        subscriptionProviders.remove(reference);
    }


    @Override
    public void init(CoreConfig coreConfig, DummyAssetConnectionConfig config, ServiceContext context) {
        this.host = config.getHost();
        this.port = config.getPort();
        this.coreConfig = coreConfig;
        config.getValueProviders().forEach((k, v) -> registerValueProvider(k, v));
        config.getOperationProviders().forEach((k, v) -> registerOperationProvider(k, v));
        config.getSubscriptionProviders().forEach((k, v) -> registerSubscriptionProvider(k, v));
    }


    @Override
    public DummyAssetConnectionConfig asConfig() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public Map<Reference, AssetValueProvider> getValueProviders() {
        return valueProviders;
    }


    @Override
    public Map<Reference, AssetOperationProvider> getOperationProviders() {
        return operationProviders;
    }


    @Override
    public Map<Reference, AssetSubscriptionProvider> getSubscriptionProviders() {
        return subscriptionProviders;
    }


    @Override
    public boolean sameAs(AssetConnection obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DummyAssetConnection other = (DummyAssetConnection) obj;
        return Objects.equals(port, other.port)
                && Objects.equals(this.host, other.host)
                && Objects.equals(this.coreConfig, other.coreConfig);
    }


    @Override
    public void unregisterValueProvider(Reference reference) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
