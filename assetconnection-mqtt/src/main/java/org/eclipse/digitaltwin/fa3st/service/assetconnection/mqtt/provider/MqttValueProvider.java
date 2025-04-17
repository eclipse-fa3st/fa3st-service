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
import org.eclipse.digitaltwin.fa3st.service.assetconnection.common.provider.MultiFormatValueProvider;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.mqtt.provider.config.MqttValueProviderConfig;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


/**
 * ValueProvider for MQTT.
 */
public class MqttValueProvider extends MultiFormatValueProvider<MqttValueProviderConfig> {

    private final ServiceContext serviceContext;
    private final Reference reference;
    private final MqttClient client;

    public MqttValueProvider(ServiceContext serviceContext, Reference reference, MqttClient client, MqttValueProviderConfig config) {
        super(config);
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.requireNonNull(client, "client must be non-null");
        this.serviceContext = serviceContext;
        this.reference = reference;
        this.client = client;
    }


    @Override
    public byte[] getRawValue() throws AssetConnectionException {
        throw new UnsupportedOperationException("Read operation not supported by MQTT");
    }


    @Override
    public void setRawValue(byte[] value) throws AssetConnectionException {
        try {
            client.publish(config.getTopic(), new MqttMessage(value));
        }
        catch (MqttException e) {
            throw new AssetConnectionException("writing value via MQTT asset connection failed", e);
        }
    }


    @Override
    protected TypeInfo getTypeInfo() {
        try {
            return serviceContext.getTypeInfo(reference);
        }
        catch (ResourceNotFoundException | PersistenceException e) {
            throw new IllegalStateException(String.format(
                    "MQTT value provider could not get type info as resource does not exist or storage failed - this should not be able to occur (reference: %s)",
                    ReferenceHelper.toString(reference)),
                    e);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), serviceContext, client, reference);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MqttValueProvider)) {
            return false;
        }
        final MqttValueProvider that = (MqttValueProvider) obj;
        return super.equals(obj)
                && Objects.equals(serviceContext, that.serviceContext)
                && Objects.equals(client, that.client)
                && Objects.equals(reference, that.reference);
    }
}
