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

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.fa3st.common.certificate.CertificateConfig;
import org.eclipse.digitaltwin.fa3st.common.certificate.CertificateData;
import org.eclipse.digitaltwin.fa3st.common.certificate.CertificateInformation;
import org.eclipse.digitaltwin.fa3st.common.exception.ConfigurationInitializationException;
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.EventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.SubscriptionId;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.SubscriptionInfo;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.access.AccessEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.access.ElementReadEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.access.ExecuteEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.access.OperationFinishEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.access.OperationInvokeEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.access.ReadEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.access.ValueReadEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ChangeEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ElementChangeEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ElementCreateEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ElementDeleteEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ElementUpdateEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ValueChangeEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.error.ErrorEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.error.ErrorLevel;
import org.eclipse.digitaltwin.fa3st.common.model.value.PropertyValue;
import org.eclipse.digitaltwin.fa3st.common.model.value.primitive.IntValue;
import org.eclipse.digitaltwin.fa3st.common.model.value.primitive.StringValue;
import org.eclipse.digitaltwin.fa3st.common.util.KeyStoreHelper;
import org.eclipse.digitaltwin.fa3st.common.util.LambdaExceptionHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.config.CoreConfig;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;


public abstract class AbstractMessageBusMqttTest<T> {

    private static final ServiceContext SERVICE_CONTEXT = Mockito.mock(ServiceContext.class);
    private static final long DEFAULT_TIMEOUT = 5000;
    private static final String DEFAULT_KEY_STORE_TYPE = "JKS";

    protected static final String PASSWORD_FILE = "src/test/resources/password_file.conf";
    protected static final String KEYSTORE_PASSWORD = "keystore-password";
    protected static final String KEY_PASSWORD = "key-password";
    protected static final String USER = "user";
    protected static final String USER_PASSWORD_VALID = "user-password";
    protected static final String USER_PASSWORD_INVALID = "user-password-wrong";

    private static final Property PROPERTY = new DefaultProperty.Builder()
            .idShort("ExampleProperty")
            .valueType(DataTypeDefXsd.STRING)
            .value("bar")
            .build();

    private static final Property PARAMETER_IN = new DefaultProperty.Builder()
            .idShort("ParameterIn")
            .valueType(DataTypeDefXsd.STRING)
            .build();

    private static final Property PARAMETER_OUT = new DefaultProperty.Builder()
            .idShort("ParameterOut")
            .valueType(DataTypeDefXsd.STRING)
            .build();

    private static final Operation OPERATION = new DefaultOperation.Builder()
            .idShort("ExampleOperation")
            .inputVariables(new DefaultOperationVariable.Builder()
                    .value(PARAMETER_IN)
                    .build())
            .outputVariables(new DefaultOperationVariable.Builder()
                    .value(PARAMETER_OUT)
                    .build())
            .build();

    private static final Reference OPERATION_REFERENCE = new DefaultReference.Builder()
            .keys(new DefaultKey.Builder()
                    .type(KeyTypes.OPERATION)
                    .value(OPERATION.getIdShort())
                    .build())
            .build();

    private static final Reference PROPERTY_REFERENCE = new DefaultReference.Builder()
            .keys(new DefaultKey.Builder()
                    .type(KeyTypes.PROPERTY)
                    .value(PROPERTY.getIdShort())
                    .build())
            .build();

    private static final ValueChangeEventMessage VALUE_CHANGE_MESSAGE = ValueChangeEventMessage.builder()
            .oldValue(new PropertyValue(new IntValue(100)))
            .oldValue(new PropertyValue(new IntValue(123)))
            .build();

    private static final ElementReadEventMessage ELEMENT_READ_MESSAGE = ElementReadEventMessage.builder()
            .element(PROPERTY_REFERENCE)
            .value(PROPERTY)
            .build();

    private static final ValueReadEventMessage VALUE_READ_MESSAGE = ValueReadEventMessage.builder()
            .element(PROPERTY_REFERENCE)
            .value(new PropertyValue(new StringValue(PROPERTY.getValue())))
            .build();

    private static final ElementCreateEventMessage ELEMENT_CREATE_MESSAGE = ElementCreateEventMessage.builder()
            .element(PROPERTY_REFERENCE)
            .value(PROPERTY)
            .build();

    private static final ElementDeleteEventMessage ELEMENT_DELETE_MESSAGE = ElementDeleteEventMessage.builder()
            .element(PROPERTY_REFERENCE)
            .value(PROPERTY)
            .build();

    private static final ElementUpdateEventMessage ELEMENT_UPDATE_MESSAGE = ElementUpdateEventMessage.builder()
            .element(PROPERTY_REFERENCE)
            .value(PROPERTY)
            .build();

    private static final OperationInvokeEventMessage OPERATION_INVOKE_MESSAGE = OperationInvokeEventMessage.builder()
            .element(OPERATION_REFERENCE)
            .input(PARAMETER_IN.getIdShort(), new PropertyValue(new StringValue("input")))
            .build();

    private static final OperationFinishEventMessage OPERATION_FINISH_MESSAGE = OperationFinishEventMessage.builder()
            .element(OPERATION_REFERENCE)
            .output(PARAMETER_OUT.getIdShort(), new PropertyValue(new StringValue("result")))
            .build();

    private static final ErrorEventMessage ERROR_MESSAGE = ErrorEventMessage.builder()
            .element(PROPERTY_REFERENCE)
            .level(ErrorLevel.ERROR)
            .build();

    private static final List<EventMessage> ALL_MESSAGES = List.of(
            VALUE_CHANGE_MESSAGE,
            ELEMENT_READ_MESSAGE,
            VALUE_READ_MESSAGE,
            ELEMENT_CREATE_MESSAGE,
            ELEMENT_DELETE_MESSAGE,
            ELEMENT_UPDATE_MESSAGE,
            OPERATION_INVOKE_MESSAGE,
            OPERATION_FINISH_MESSAGE,
            ERROR_MESSAGE);

    private static final List<EventMessage> EXECUTE_MESSAGES = List.of(
            OPERATION_INVOKE_MESSAGE,
            OPERATION_FINISH_MESSAGE);

    private static final List<EventMessage> READ_MESSAGES = List.of(
            ELEMENT_READ_MESSAGE,
            VALUE_READ_MESSAGE);

    private static final List<EventMessage> ACCESS_MESSAGES = Stream.concat(EXECUTE_MESSAGES.stream(), READ_MESSAGES.stream())
            .collect(Collectors.toList());

    private static final List<EventMessage> ELEMENT_CHANGE_MESSAGES = List.of(
            ELEMENT_CREATE_MESSAGE,
            ELEMENT_UPDATE_MESSAGE,
            ELEMENT_DELETE_MESSAGE);

    private static final List<EventMessage> CHANGE_MESSAGES = Stream.concat(ELEMENT_CHANGE_MESSAGES.stream(), Stream.of(VALUE_CHANGE_MESSAGE))
            .collect(Collectors.toList());

    private static final CertificateInformation SERVER_CERTIFICATE_INFORMATION = CertificateInformation.builder()
            .applicationUri("urn:org:eclipse:digitaltwin:fa3st:service:messagebus:mqtt:test:server")
            .commonName("FA³ST Service MQTT MessageBus Unit Test - Server")
            .countryCode("BE")
            .localityName("Brussels")
            .organization("Eclipse FA3ST")
            .build();

    private static final CertificateInformation CLIENT_CERTIFICATE_INFORMATION = CertificateInformation.builder()
            .applicationUri("urn:org:eclipse:digitaltwin:fa3st:service:messagebus:mqtt:test:client")
            .commonName("FA³ST Service MQTT MessageBus Unit Test - Client")
            .countryCode("BE")
            .localityName("Brussels")
            .organization("Eclipse FA3ST")
            .build();

    protected static String serverKeyStorePath;
    protected static String clientKeyStorePath;

    protected abstract MessageBusMqttConfig getBaseConfig();


    protected abstract T startServer(MessageBusMqttConfig config) throws Exception;


    protected abstract void stopServer(T server);


    @BeforeClass
    public static void createCertificates() throws IOException, GeneralSecurityException {
        File serverKeyStoreFile = File.createTempFile("fa3st-", "-keystore-server.p12");
        serverKeyStoreFile.deleteOnExit();
        CertificateData serverCertificateData = KeyStoreHelper.generateSelfSigned(SERVER_CERTIFICATE_INFORMATION);
        KeyStoreHelper.save(serverCertificateData,
                serverKeyStoreFile,
                DEFAULT_KEY_STORE_TYPE,
                "server-cert",
                KEY_PASSWORD,
                KEYSTORE_PASSWORD);
        serverKeyStorePath = serverKeyStoreFile.getAbsolutePath();

        File clientKeyStoreFile = File.createTempFile("fa3st-", "-keystore-client.p12");
        clientKeyStoreFile.deleteOnExit();
        CertificateData clientCertificateData = KeyStoreHelper.generateSelfSigned(CLIENT_CERTIFICATE_INFORMATION);
        KeyStore clientKeyStore = KeyStoreHelper.create(clientCertificateData,
                DEFAULT_KEY_STORE_TYPE,
                "client-cert",
                KEY_PASSWORD,
                KEYSTORE_PASSWORD);
        clientKeyStore.setCertificateEntry("server-key", serverCertificateData.getCertificate());
        KeyStoreHelper.save(clientKeyStore, clientKeyStoreFile, KEYSTORE_PASSWORD);
        clientKeyStorePath = clientKeyStoreFile.getAbsolutePath();
    }


    @Test
    public void testDistinctTypesSubscription() throws Exception {
        MessageBusMqttConfig config = configureAnonymousSuccess();
        MessageBusInfo messageBusInfo = startMessageBus(config);
        assertExactTypeSubscription(messageBusInfo);
        assertSuperTypeSubscription(messageBusInfo);
        assertDistinctTypesSubscription(messageBusInfo);
        assertNotMatchingSubscription(messageBusInfo);
        assertUnsubscribeWorks(config);
        stopMessageBus(messageBusInfo);
    }


    @Test
    public void testAnonymousSuccess() throws Exception {
        assertConnectionWorks(configureAnonymousSuccess());
    }


    @Test
    public void testWebsocketAsAnonymousSuccess() throws Exception {
        assertConnectionWorks(configureWebsocketAsAnonymousSuccess());
    }


    @Test
    public void testWithSslAsAnonymousSuccess() throws Exception {
        assertConnectionWorks(configureWithSslAsAnonymousSuccess());
    }


    @Test
    public void testWebsocketWithSslAsAnonymousSuccess() throws Exception {
        assertConnectionWorks(configureWebsocketWithSslAsAnonymousSuccess());
    }


    @Test
    public void testAsAnonymousFail() throws Exception {
        assertConnectionFails(configureAsAnonymousFail());
    }


    @Test
    public void testWebsocketAsAnonymousFail() throws Exception {
        assertConnectionFails(configureWebsocketAsAnonymousFail());
    }


    @Test
    public void testWithSslAsAnonymousFail() throws Exception {
        assertConnectionFails(configureWithSslAsAnonymousFail());
    }


    @Test
    public void testInternalWebsocketWithSslAsAnonymousFail() throws Exception {
        assertConnectionFails(configureInternalWebsocketWithSslAsAnonymousFail());
    }


    @Test
    public void testAsInvalidUser() throws Exception {
        assertConnectionFails(configureAsInvalidUser());
    }


    @Test
    public void testWebsocketAsInvalidUser() throws Exception {
        assertConnectionFails(configureWebsocketAsInvalidUser());
    }


    @Test
    public void testWithSslAsInvalidUser() throws Exception {
        assertConnectionFails(configureWithSslAsInvalidUser());
    }


    @Test
    public void testWebsocketWithSslAsInvalidUser() throws Exception {
        assertConnectionFails(configureWebsocketWithSslAsInvalidUser());
    }


    @Test
    public void testAsValidUser() throws Exception {
        assertConnectionWorks(configureAsValidUser());
    }


    @Test
    public void testWebsocketAsValidUser() throws Exception {
        assertConnectionWorks(configureWebsocketAsValidUser());
    }


    @Test
    public void testWithSslAsValidUser() throws Exception {
        assertConnectionWorks(configureWithSslAsValidUser());
    }


    @Test
    public void testWebsocketWithSslAsValidUser() throws Exception {
        assertConnectionWorks(configureWebsocketWithSslAsValidUser());
    }


    protected MessageBusMqttConfig configureAnonymousSuccess() {
        return MessageBusMqttConfig.builder()
                .from(getBaseConfig())
                .build();
    }


    protected MessageBusMqttConfig configureWebsocketAsAnonymousSuccess() {
        return MessageBusMqttConfig.builder()
                .from(getBaseConfig())
                .useWebsocket(true)
                .build();
    }


    protected MessageBusMqttConfig configureWithSslAsAnonymousSuccess() {
        return MessageBusMqttConfig.builder()
                .from(getBaseConfig())
                .serverCertificate(CertificateConfig.builder()
                        .keyStorePath(serverKeyStorePath)
                        .keyStorePassword(KEYSTORE_PASSWORD)
                        .keyPassword(KEY_PASSWORD)
                        .build())
                .clientCertificate(CertificateConfig.builder()
                        .keyStorePath(clientKeyStorePath)
                        .keyStorePassword(KEYSTORE_PASSWORD)
                        .keyPassword(KEY_PASSWORD)
                        .build())
                .build();
    }


    protected MessageBusMqttConfig configureWebsocketWithSslAsAnonymousSuccess() {
        return MessageBusMqttConfig.builder()
                .from(getBaseConfig())
                .useWebsocket(true)
                .serverCertificate(CertificateConfig.builder()
                        .keyStorePath(serverKeyStorePath)
                        .keyStorePassword(KEYSTORE_PASSWORD)
                        .keyPassword(KEY_PASSWORD)
                        .build())
                .clientCertificate(CertificateConfig.builder()
                        .keyStorePath(clientKeyStorePath)
                        .keyStorePassword(KEYSTORE_PASSWORD)
                        .keyPassword(KEY_PASSWORD)
                        .build())
                .build();
    }


    protected MessageBusMqttConfig configureAsAnonymousFail() {
        return MessageBusMqttConfig.builder()
                .from(getBaseConfig())
                .user(USER, USER_PASSWORD_VALID)
                .build();
    }


    protected MessageBusMqttConfig configureWebsocketAsAnonymousFail() {
        return MessageBusMqttConfig.builder()
                .from(getBaseConfig())
                .useWebsocket(true)
                .user(USER, USER_PASSWORD_VALID)
                .build();
    }


    protected MessageBusMqttConfig configureWithSslAsAnonymousFail() {
        return MessageBusMqttConfig.builder()
                .from(getBaseConfig())
                .user(USER, USER_PASSWORD_VALID)
                .serverCertificate(CertificateConfig.builder()
                        .keyStorePath(serverKeyStorePath)
                        .keyStorePassword(KEYSTORE_PASSWORD)
                        .keyPassword(KEY_PASSWORD)
                        .build())
                .clientCertificate(CertificateConfig.builder()
                        .keyStorePath(clientKeyStorePath)
                        .keyStorePassword(KEYSTORE_PASSWORD)
                        .keyPassword(KEY_PASSWORD)
                        .build())
                .build();
    }


    protected MessageBusMqttConfig configureInternalWebsocketWithSslAsAnonymousFail() {
        return MessageBusMqttConfig.builder()
                .from(getBaseConfig())
                .useWebsocket(true)
                .user(USER, USER_PASSWORD_VALID)
                .serverCertificate(CertificateConfig.builder()
                        .keyStorePath(serverKeyStorePath)
                        .keyStorePassword(KEYSTORE_PASSWORD)
                        .keyPassword(KEY_PASSWORD)
                        .build())
                .clientCertificate(CertificateConfig.builder()
                        .keyStorePath(clientKeyStorePath)
                        .keyStorePassword(KEYSTORE_PASSWORD)
                        .keyPassword(KEY_PASSWORD)
                        .build())
                .build();
    }


    protected MessageBusMqttConfig configureAsInvalidUser() {
        return MessageBusMqttConfig.builder()
                .from(getBaseConfig())
                .user(USER, USER_PASSWORD_VALID)
                .username(USER)
                .password(USER_PASSWORD_INVALID)
                .build();
    }


    protected MessageBusMqttConfig configureWebsocketAsInvalidUser() {
        return MessageBusMqttConfig.builder()
                .from(getBaseConfig())
                .useWebsocket(true)
                .user(USER, USER_PASSWORD_VALID)
                .username(USER)
                .password(USER_PASSWORD_INVALID)
                .build();
    }


    protected MessageBusMqttConfig configureWithSslAsInvalidUser() {
        return MessageBusMqttConfig.builder()
                .from(getBaseConfig())
                .user(USER, USER_PASSWORD_VALID)
                .username(USER)
                .password(USER_PASSWORD_INVALID)
                .serverCertificate(CertificateConfig.builder()
                        .keyStorePath(serverKeyStorePath)
                        .keyStorePassword(KEYSTORE_PASSWORD)
                        .keyPassword(KEY_PASSWORD)
                        .build())
                .clientCertificate(CertificateConfig.builder()
                        .keyStorePath(clientKeyStorePath)
                        .keyStorePassword(KEYSTORE_PASSWORD)
                        .keyPassword(KEY_PASSWORD)
                        .build())
                .build();
    }


    protected MessageBusMqttConfig configureWebsocketWithSslAsInvalidUser() {
        return MessageBusMqttConfig.builder()
                .from(getBaseConfig())
                .useWebsocket(true)
                .user(USER, USER_PASSWORD_VALID)
                .username(USER)
                .password(USER_PASSWORD_INVALID)
                .serverCertificate(CertificateConfig.builder()
                        .keyStorePath(serverKeyStorePath)
                        .keyStorePassword(KEYSTORE_PASSWORD)
                        .keyPassword(KEY_PASSWORD)
                        .build())
                .clientCertificate(CertificateConfig.builder()
                        .keyStorePath(clientKeyStorePath)
                        .keyStorePassword(KEYSTORE_PASSWORD)
                        .keyPassword(KEY_PASSWORD)
                        .build())
                .build();
    }


    protected MessageBusMqttConfig configureAsValidUser() {
        return MessageBusMqttConfig.builder()
                .from(getBaseConfig())
                .user(USER, USER_PASSWORD_VALID)
                .username(USER)
                .password(USER_PASSWORD_VALID)
                .build();
    }


    protected MessageBusMqttConfig configureWebsocketAsValidUser() {
        return MessageBusMqttConfig.builder()
                .from(getBaseConfig())
                .useWebsocket(true)
                .user(USER, USER_PASSWORD_VALID)
                .username(USER)
                .password(USER_PASSWORD_VALID)
                .build();
    }


    protected MessageBusMqttConfig configureWithSslAsValidUser() {
        return MessageBusMqttConfig.builder()
                .from(getBaseConfig())
                .user(USER, USER_PASSWORD_VALID)
                .username(USER)
                .password(USER_PASSWORD_VALID)
                .serverCertificate(CertificateConfig.builder()
                        .keyStorePath(serverKeyStorePath)
                        .keyStorePassword(KEYSTORE_PASSWORD)
                        .keyPassword(KEY_PASSWORD)
                        .build())
                .clientCertificate(CertificateConfig.builder()
                        .keyStorePath(clientKeyStorePath)
                        .keyStorePassword(KEYSTORE_PASSWORD)
                        .keyPassword(KEY_PASSWORD)
                        .build())
                .build();
    }


    protected MessageBusMqttConfig configureWebsocketWithSslAsValidUser() {
        return MessageBusMqttConfig.builder()
                .from(getBaseConfig())
                .useWebsocket(true)
                .user(USER, USER_PASSWORD_VALID)
                .username(USER)
                .password(USER_PASSWORD_VALID)
                .serverCertificate(CertificateConfig.builder()
                        .keyStorePath(serverKeyStorePath)
                        .keyStorePassword(KEYSTORE_PASSWORD)
                        .keyPassword(KEY_PASSWORD)
                        .build())
                .clientCertificate(CertificateConfig.builder()
                        .keyStorePath(clientKeyStorePath)
                        .keyStorePassword(KEYSTORE_PASSWORD)
                        .keyPassword(KEY_PASSWORD)
                        .build())
                .build();
    }


    private static int findFreePort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Assert.assertNotNull(serverSocket);
            Assert.assertTrue(serverSocket.getLocalPort() > 0);
            return serverSocket.getLocalPort();
        }
        catch (IOException ex) {
            Assert.fail("error finding free port");
        }
        return -1;
    }


    private MessageBusInfo startMessageBus(MessageBusMqttConfig config) throws Exception {
        config.setPort(findFreePort());
        config.setSslPort(findFreePort());
        config.setWebsocketPort(findFreePort());
        config.setSslWebsocketPort(findFreePort());
        T server = startServer(config);
        MessageBusMqtt messageBus = new MessageBusMqtt();
        messageBus.init(CoreConfig.builder().build(), config, SERVICE_CONTEXT);
        messageBus.start();
        return new MessageBusInfo(messageBus, server);
    }


    private void assertExactTypeSubscription(MessageBusInfo messageBusInfo) throws Exception {
        ALL_MESSAGES.forEach(LambdaExceptionHelper.rethrowConsumer(x -> assertMessage(messageBusInfo, x.getClass(), x, x)));
    }


    private void assertSuperTypeSubscription(MessageBusInfo messageBusInfo) throws Exception {
        Map<Class<? extends EventMessage>, List<EventMessage>> messageTypes = Map.of(
                EventMessage.class, ALL_MESSAGES,
                AccessEventMessage.class, ACCESS_MESSAGES,
                ExecuteEventMessage.class, EXECUTE_MESSAGES,
                ReadEventMessage.class, READ_MESSAGES,
                ChangeEventMessage.class, CHANGE_MESSAGES,
                ElementChangeEventMessage.class, ELEMENT_CHANGE_MESSAGES);
        messageTypes.forEach(LambdaExceptionHelper.rethrowBiConsumer((k, v) -> assertMessages(messageBusInfo, k, v, v)));
    }


    private void assertDistinctTypesSubscription(MessageBusMqttConfig config) throws Exception {
        assertMessages(
                config,
                List.of(ChangeEventMessage.class, ErrorEventMessage.class),
                List.of(ELEMENT_CREATE_MESSAGE, ERROR_MESSAGE),
                Map.of(
                        ChangeEventMessage.class, List.of(ELEMENT_CREATE_MESSAGE),
                        ErrorEventMessage.class, List.of(ERROR_MESSAGE)));
    }


    private void assertDistinctTypesSubscription(MessageBusInfo messageBusInfo) throws Exception {
        assertMessages(
                messageBusInfo,
                List.of(ChangeEventMessage.class, ErrorEventMessage.class),
                List.of(ELEMENT_CREATE_MESSAGE, ERROR_MESSAGE),
                Map.of(
                        ChangeEventMessage.class, List.of(ELEMENT_CREATE_MESSAGE),
                        ErrorEventMessage.class, List.of(ERROR_MESSAGE)));
    }


    private void assertNotMatchingSubscription(MessageBusMqttConfig config) throws Exception {
        assertMessage(
                config,
                ErrorEventMessage.class,
                VALUE_CHANGE_MESSAGE,
                null);
    }


    private void assertNotMatchingSubscription(MessageBusInfo messageBusInfo) throws Exception {
        assertMessage(
                messageBusInfo,
                ErrorEventMessage.class,
                VALUE_CHANGE_MESSAGE,
                null);
    }


    private void assertUnsubscribeWorks(MessageBusMqttConfig config) throws Exception {
        MessageBusInfo messageBusInfo = startMessageBus(config);
        CountDownLatch condition = new CountDownLatch(1);
        SubscriptionId controlSubscription = messageBusInfo.messageBus.subscribe(SubscriptionInfo.create(
                ValueChangeEventMessage.class,
                x -> {
                    condition.countDown();
                }));
        SubscriptionId revokedSubscription = messageBusInfo.messageBus.subscribe(SubscriptionInfo.create(
                ValueChangeEventMessage.class,
                x -> {
                    Assert.fail();
                }));
        messageBusInfo.messageBus.unsubscribe(revokedSubscription);
        messageBusInfo.messageBus.publish(VALUE_CHANGE_MESSAGE);
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        messageBusInfo.messageBus.unsubscribe(controlSubscription);
        messageBusInfo.messageBus.stop();
        stopServer(messageBusInfo.server);
    }


    private void assertConnectionFails(MessageBusMqttConfig config) throws InterruptedException, MessageBusException, ConfigurationInitializationException, IOException {
        MessageBusException expection = Assert.assertThrows(MessageBusException.class, () -> startMessageBus(config));
        Assert.assertEquals("Failed to connect to MQTT server", expection.getMessage());
    }


    private void assertConnectionWorks(MessageBusMqttConfig config) throws Exception {
        MessageBusInfo messageBusInfo = startMessageBus(config);
        stopMessageBus(messageBusInfo);
    }


    private void assertMessage(
                               MessageBusMqttConfig config,
                               Class<? extends EventMessage> subscribeTo,
                               EventMessage toPublish,
                               EventMessage expected)
            throws Exception {
        assertMessages(
                config,
                subscribeTo,
                List.of(toPublish),
                Objects.isNull(expected)
                        ? List.of()
                        : List.of(expected));
    }


    private void assertMessage(
                               MessageBusInfo messageBusInfo,
                               Class<? extends EventMessage> subscribeTo,
                               EventMessage toPublish,
                               EventMessage expected)
            throws Exception {
        assertMessages(
                messageBusInfo,
                subscribeTo,
                List.of(toPublish),
                Objects.isNull(expected)
                        ? List.of()
                        : List.of(expected));
    }


    private void assertMessages(
                                MessageBusMqttConfig config,
                                Class<? extends EventMessage> subscribeTo,
                                List<EventMessage> toPublish,
                                List<EventMessage> expected)
            throws Exception {
        assertMessages(
                config,
                List.of(subscribeTo),
                toPublish,
                Objects.isNull(expected) || expected.isEmpty()
                        ? Map.of()
                        : Map.of(subscribeTo, expected));
    }


    private void assertMessages(
                                MessageBusInfo messageBusInfo,
                                Class<? extends EventMessage> subscribeTo,
                                List<EventMessage> toPublish,
                                List<EventMessage> expected)
            throws Exception {
        assertMessages(
                messageBusInfo,
                List.of(subscribeTo),
                toPublish,
                Objects.isNull(expected) || expected.isEmpty()
                        ? Map.of()
                        : Map.of(subscribeTo, expected));
    }


    private void assertMessages(
                                MessageBusMqttConfig config,
                                List<Class<? extends EventMessage>> subscribeTo,
                                List<EventMessage> toPublish,
                                Map<Class<? extends EventMessage>, List<EventMessage>> expected)
            throws Exception {
        MessageBusInfo messageBusInfo = startMessageBus(config);
        try {
            assertMessages(messageBusInfo, subscribeTo, toPublish, expected);
        }
        finally {
            messageBusInfo.messageBus.stop();
            stopServer(messageBusInfo.server);
        }
    }


    private void assertMessages(
                                MessageBusInfo messageBusInfo,
                                List<Class<? extends EventMessage>> subscribeTo,
                                List<EventMessage> toPublish,
                                Map<Class<? extends EventMessage>, List<EventMessage>> expected)
            throws Exception {
        CountDownLatch condition = new CountDownLatch(expected.values().stream().mapToInt(List::size).sum());
        final Map<Class<? extends EventMessage>, List<EventMessage>> actual = Collections.synchronizedMap(new HashMap<>());
        List<SubscriptionId> subscriptions = subscribeTo.stream()
                .map(x -> messageBusInfo.messageBus.subscribe(SubscriptionInfo.create(x, e -> {
                    if (!actual.containsKey(x)) {
                        actual.put(x, Collections.synchronizedList(new ArrayList<>()));
                    }
                    actual.get(x).add(e);
                    condition.countDown();
                })))
                .collect(Collectors.toList());
        if (Objects.nonNull(toPublish)) {
            toPublish.forEach(LambdaExceptionHelper.rethrowConsumer(messageBusInfo.messageBus::publish));
        }
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        subscriptions.forEach(messageBusInfo.messageBus::unsubscribe);
        Assert.assertEquals(Objects.isNull(expected) ? Map.of() : expected, actual);
    }


    private void stopMessageBus(MessageBusInfo messageBusInfo) {
        messageBusInfo.messageBus.stop();
        stopServer(messageBusInfo.server);
    }

    private class MessageBusInfo {

        MessageBusMqtt messageBus;
        T server;

        MessageBusInfo(MessageBusMqtt messageBus, T server) {
            this.messageBus = messageBus;
            this.server = server;
        }

    }
}
