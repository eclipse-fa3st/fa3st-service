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
package org.eclipse.digitaltwin.fa3st.service.assetconnection.mqtt.provider;

import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.typing.TypeInfo;
import org.eclipse.digitaltwin.fa3st.common.util.Ensure;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.common.provider.MultiFormatSubscriptionProvider;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.mqtt.provider.config.MqttSubscriptionProviderConfig;


/**
 * SubscriptionProvider for MQTT protocol.
 */
public class MqttSubscriptionProvider extends MultiFormatSubscriptionProvider<MqttSubscriptionProviderConfig> {

    private final ServiceContext serviceContext;
    private final Reference reference;
    private final MqttSubscriptionMultiplexer multiplexer;

    public MqttSubscriptionProvider(ServiceContext serviceContext, Reference reference, MqttSubscriptionProviderConfig config, MqttSubscriptionMultiplexer multiplexer) {
        super(config);
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.requireNonNull(multiplexer, "multiplexer must be non-null");
        this.serviceContext = serviceContext;
        this.reference = reference;
        this.multiplexer = multiplexer;
    }


    @Override
    protected TypeInfo getTypeInfo() {
        try {
            return serviceContext.getTypeInfo(reference);
        }
        catch (ResourceNotFoundException | PersistenceException e) {
            throw new IllegalStateException(String.format(
                    "MQTT subscription provider could not get type info as resource does not exist or storage failed - this should not be able to occur (reference: %s)",
                    ReferenceHelper.toString(reference)),
                    e);
        }
    }


    @Override
    public void subscribe() throws AssetConnectionException {
        multiplexer.addListener(config.getTopic(), this::fireNewDataReceived);
    }


    @Override
    protected void unsubscribe() throws AssetConnectionException {
        multiplexer.removeListener(config.getTopic(), this::fireNewDataReceived);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), serviceContext, reference, multiplexer);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MqttSubscriptionProvider)) {
            return false;
        }
        final MqttSubscriptionProvider that = (MqttSubscriptionProvider) obj;
        return super.equals(obj)
                && Objects.equals(serviceContext, that.serviceContext)
                && Objects.equals(reference, that.reference)
                && Objects.equals(multiplexer, that.multiplexer);
    }
}
