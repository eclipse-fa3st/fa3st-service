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
package org.eclipse.digitaltwin.fa3st.service.example.assetconnection.custom;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.ConfigurationInitializationException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValueMappingException;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceHelper;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.AbstractAssetConnection;
import org.eclipse.digitaltwin.fa3st.service.example.assetconnection.custom.provider.CustomOperationProvider;
import org.eclipse.digitaltwin.fa3st.service.example.assetconnection.custom.provider.CustomSubscriptionProvider;
import org.eclipse.digitaltwin.fa3st.service.example.assetconnection.custom.provider.CustomValueProvider;
import org.eclipse.digitaltwin.fa3st.service.example.assetconnection.custom.provider.config.CustomOperationProviderConfig;
import org.eclipse.digitaltwin.fa3st.service.example.assetconnection.custom.provider.config.CustomSubscriptionProviderConfig;
import org.eclipse.digitaltwin.fa3st.service.example.assetconnection.custom.provider.config.CustomValueProviderConfig;


public class CustomAssetConnection extends
        AbstractAssetConnection<CustomAssetConnection, CustomAssetConnectionConfig, CustomValueProviderConfig, CustomValueProvider, CustomOperationProviderConfig, CustomOperationProvider, CustomSubscriptionProviderConfig, CustomSubscriptionProvider> {

    @Override
    public void doDisconnect() throws AssetConnectionException {
        // nothing to do here
    }


    @Override
    public String getEndpointInformation() {
        return "This is my custom asset connection";
    }


    @Override
    protected CustomOperationProvider createOperationProvider(Reference reference, CustomOperationProviderConfig providerConfig) throws AssetConnectionException {
        try {
            return new CustomOperationProvider(reference, providerConfig, serviceContext);
        }
        catch (ConfigurationInitializationException e) {
            throw new AssetConnectionException(String.format("creating value provider failed (reference: %s)", ReferenceHelper.toString(reference)), e);
        }
    }


    @Override
    protected CustomSubscriptionProvider createSubscriptionProvider(Reference reference, CustomSubscriptionProviderConfig providerConfig) throws AssetConnectionException {
        try {
            return new CustomSubscriptionProvider(reference, providerConfig, serviceContext);
        }
        catch (ResourceNotFoundException | ValueMappingException | PersistenceException e) {
            throw new AssetConnectionException(String.format("creating subscription provider failed (reference: %s)", ReferenceHelper.toString(reference)), e);
        }
    }


    @Override
    protected CustomValueProvider createValueProvider(Reference reference, CustomValueProviderConfig providerConfig) throws AssetConnectionException {
        try {
            return new CustomValueProvider(reference, providerConfig, serviceContext);
        }
        catch (ResourceNotFoundException | ValueMappingException | PersistenceException e) {
            throw new AssetConnectionException(String.format("creating value provider failed (reference: %s)", ReferenceHelper.toString(reference)), e);
        }
    }


    @Override
    protected void doConnect() {
        // nothing to do here
    }

}
