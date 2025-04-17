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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.util.Ensure;
import org.eclipse.digitaltwin.fa3st.common.util.LambdaExceptionHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Maps multiple FA³ST subscriptions to a single MQTT subscription.
 */
public class MqttSubscriptionMultiplexer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttSubscriptionMultiplexer.class);
    private final ServiceContext serviceContext;
    private final ConcurrentHashMap<String, List<Consumer<byte[]>>> listenersByTopic;
    private MqttClient client;

    public MqttSubscriptionMultiplexer(ServiceContext serviceContext, MqttClient client) {
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        Ensure.requireNonNull(client, "client must be non-null");
        this.serviceContext = serviceContext;
        this.client = client;
        this.listenersByTopic = new ConcurrentHashMap<>();
    }


    private void subscribeTopic(String topic) throws AssetConnectionException {
        try {
            client.subscribe(topic, (actualTopic, message) -> notify(topic, message.getPayload()));
        }
        catch (MqttException e) {
            throw new AssetConnectionException(String.format("error subscribing to MQTT asset connection (topic: %s)", topic), e);
        }
    }


    private void unsubscribeTopic(String topic) {
        try {
            client.unsubscribe(topic);
        }
        catch (MqttException e) {
            LOGGER.info("error unsubscribing from MQTT asset connection (topic: {})", topic, e);
        }
    }


    private void cleanupSubscriptions() {
        for (var iterator = listenersByTopic.entrySet().iterator(); iterator.hasNext();) {
            var entry = iterator.next();
            if (Objects.isNull(entry.getValue()) || entry.getValue().isEmpty()) {
                unsubscribeTopic(entry.getKey());
                iterator.remove();
            }
        }
    }


    private void notify(String topic, byte[] value) {
        listenersByTopic.get(topic).forEach(x -> x.accept(value));
    }


    /**
     * Adds a listener.
     *
     * @param topic the topic to listen to
     * @param listener The listener to add
     */
    public void addListener(String topic, Consumer<byte[]> listener) throws AssetConnectionException {
        Ensure.requireNonNull(topic, "topic must be non-null");
        Ensure.requireNonNull(listener, "listener must be non-null");
        if (!listenersByTopic.containsKey(topic)) {
            listenersByTopic.put(topic, new ArrayList<>());
            subscribeTopic(topic);
        }
        listenersByTopic.get(topic).add(listener);
    }


    /**
     * Reconnects underlying subscriptions after connection loss.
     *
     * @param client the new client
     * @throws AssetConnectionException if reconnecting fails
     */
    public void reconnect(MqttClient client) throws AssetConnectionException {
        this.client = client;
        listenersByTopic.keySet().forEach(LambdaExceptionHelper.rethrowConsumer(this::subscribeTopic));
    }


    /**
     * Removes a listener.
     *
     * @param topic the topic
     * @param listener The listener to remove
     */
    public void removeListener(String topic, Consumer<byte[]> listener) {
        listenersByTopic.getOrDefault(topic, new ArrayList<>()).remove(listener);
        cleanupSubscriptions();
    }


    /**
     * Removes a listener.
     *
     * @param listener The listener to remove
     */
    public void removeListener(Consumer<byte[]> listener) {
        listenersByTopic.values().forEach(x -> x.remove(listener));
        cleanupSubscriptions();
    }


    /**
     * Closes the multiplexer, i.e. ends the underlying MQTT subscription
     *
     * @throws AssetConnectionException if closing the MQTT subscription fails
     */
    public void close() throws AssetConnectionException {
        if (client != null && client.isConnected()) {
            listenersByTopic.keySet().forEach(this::unsubscribeTopic);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(serviceContext, client, listenersByTopic);
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
        final MqttSubscriptionMultiplexer that = (MqttSubscriptionMultiplexer) obj;
        return Objects.equals(serviceContext, that.serviceContext)
                && Objects.equals(client, that.client)
                && Objects.equals(listenersByTopic, that.listenersByTopic);
    }
}
