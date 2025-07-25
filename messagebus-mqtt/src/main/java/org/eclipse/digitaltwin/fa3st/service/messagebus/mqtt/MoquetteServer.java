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
package org.eclipse.digitaltwin.fa3st.service.messagebus.mqtt;

import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The mqtt server based on moquette to publish message bus events.
 */
public class MoquetteServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoquetteServer.class);

    private Server server;

    /**
     * The MQTT Id used by FA³ST service to connect to the MQTT broker.
     */
    private final String clientId;
    private final MessageBusMqttConfig config;

    public MoquetteServer(MessageBusMqttConfig config) {
        clientId = "FA³ST MQTT Server (" + UUID.randomUUID() + ")";
        this.config = config;
    }


    /**
     * Publish a message.
     *
     * @param topic
     * @param message
     * @param qos
     */
    public void publish(String topic, String message, int qos) {
        if (server != null) {
            final ByteBuf payload = ByteBufUtil.writeUtf8(UnpooledByteBufAllocator.DEFAULT, message);
            MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.valueOf(qos), false, 0);
            MqttPublishVariableHeader varHeader = new MqttPublishVariableHeader(topic, 0);
            MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(fixedHeader, varHeader, payload);
            server.internalPublish(mqttPublishMessage, clientId);
        }
    }


    /**
     * Starts the broker.
     *
     * @throws java.io.IOException when starting the server fails with an IO error
     */
    public void start() throws IOException {
        server = new Server();
        IConfig serverConfig = new MemoryConfig(new Properties());
        serverConfig.setProperty(IConfig.DATA_PATH_PROPERTY_NAME, "data/" + UUID.randomUUID());
        // Ensure immediate flush
        serverConfig.setProperty(IConfig.BUFFER_FLUSH_MS_PROPERTY_NAME, "0");
        serverConfig.setProperty(IConfig.PORT_PROPERTY_NAME, Integer.toString(config.getPort()));
        serverConfig.setProperty(IConfig.HOST_PROPERTY_NAME, config.getHost());
        serverConfig.setProperty(IConfig.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.toString(config.getUsers().isEmpty()));
        if (config.getUseWebsocket()) {
            serverConfig.setProperty(IConfig.WEB_SOCKET_PORT_PROPERTY_NAME, Integer.toString(config.getWebsocketPort()));
        }
        if (Objects.nonNull(config.getServerCertificate())
                && Objects.nonNull(config.getServerCertificate().getKeyStorePath())
                && !config.getServerCertificate().getKeyStorePath().isEmpty()) {
            LOGGER.debug("Configuring keystore for ssl");
            serverConfig.setProperty(IConfig.JKS_PATH_PROPERTY_NAME, config.getServerCertificate().getKeyStorePath());
            serverConfig.setProperty(IConfig.KEY_STORE_TYPE, config.getServerCertificate().getKeyStoreType());
            if (Objects.nonNull(config.getServerCertificate().getKeyStorePassword())) {
                serverConfig.setProperty(IConfig.KEY_STORE_PASSWORD_PROPERTY_NAME, config.getServerCertificate().getKeyStorePassword());
            }
            if (Objects.nonNull(config.getServerCertificate().getKeyPassword())) {
                serverConfig.setProperty(IConfig.KEY_MANAGER_PASSWORD_PROPERTY_NAME, config.getServerCertificate().getKeyPassword());
            }
            serverConfig.setProperty(IConfig.SSL_PORT_PROPERTY_NAME, Integer.toString(config.getSslPort()));
            if (config.getUseWebsocket()) {
                serverConfig.setProperty(IConfig.WSS_PORT_PROPERTY_NAME, Integer.toString(config.getSslWebsocketPort()));
            }
        }
        MoquetteAuthenticator authenticator = new MoquetteAuthenticator(config);
        server.startServer(serverConfig, null, null, authenticator, authenticator);
    }


    /**
     * Stops the broker.
     */
    public void stop() {
        if (Objects.nonNull(server)) {
            server.stopServer();
        }
    }
}
