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
package org.eclipse.digitaltwin.fa3st.service.test;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.awaitility.Awaitility.await;
import static org.eclipse.digitaltwin.fa3st.service.endpoint.http.util.HttpHelper.toHttpStatusCode;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import net.javacrumbs.jsonunit.core.Option;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.ModellingKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.fa3st.common.certificate.CertificateConfig;
import org.eclipse.digitaltwin.fa3st.common.dataformat.DeserializationException;
import org.eclipse.digitaltwin.fa3st.common.dataformat.SerializationException;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.ConfigurationInitializationException;
import org.eclipse.digitaltwin.fa3st.common.exception.UnsupportedModifierException;
import org.eclipse.digitaltwin.fa3st.common.model.api.StatusCode;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.Content;
import org.eclipse.digitaltwin.fa3st.common.model.api.paging.Page;
import org.eclipse.digitaltwin.fa3st.common.model.http.HttpMethod;
import org.eclipse.digitaltwin.fa3st.common.util.DeepCopyHelper;
import org.eclipse.digitaltwin.fa3st.common.util.PortHelper;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceBuilder;
import org.eclipse.digitaltwin.fa3st.service.Service;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.OpcUaAssetConnectionConfig;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.provider.config.OpcUaValueProviderConfig;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.util.OpcUaHelper;
import org.eclipse.digitaltwin.fa3st.service.config.CoreConfig;
import org.eclipse.digitaltwin.fa3st.service.config.ServiceConfig;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.HttpEndpointConfig;
import org.eclipse.digitaltwin.fa3st.service.filestorage.memory.FileStorageInMemoryConfig;
import org.eclipse.digitaltwin.fa3st.service.messagebus.internal.MessageBusInternalConfig;
import org.eclipse.digitaltwin.fa3st.service.persistence.memory.PersistenceInMemoryConfig;
import org.eclipse.digitaltwin.fa3st.service.test.util.ApiPaths;
import org.eclipse.digitaltwin.fa3st.service.test.util.HttpHelper;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


public class AssetConnectionIT extends AbstractIntegrationTest {

    private static final String NODE_ID_SOURCE = "ns=3;s=1.Value";

    private static final int SOURCE_VALUE = 42;
    private static final int TARGET_VALUE = 0;
    private static Service service;
    private static Environment environment;
    private static Property source;
    private static Submodel submodel;
    private static Property target;

    @BeforeClass
    public static void initClass() throws IOException {
        source = new DefaultProperty.Builder()
                .idShort("source")
                .value(Integer.toString(SOURCE_VALUE))
                .valueType(DataTypeDefXsd.INTEGER)
                .build();
        target = new DefaultProperty.Builder()
                .idShort("target")
                .value(Integer.toString(TARGET_VALUE))
                .valueType(DataTypeDefXsd.INTEGER)
                .build();
        submodel = new DefaultSubmodel.Builder()
                .idShort("Submodel1")
                .id("http://example.org/submodel/1")
                .kind(ModellingKind.INSTANCE)
                .submodelElements(source)
                .submodelElements(target)
                .build();
        environment = new DefaultEnvironment.Builder()
                .assetAdministrationShells(new DefaultAssetAdministrationShell.Builder()
                        .idShort("AAS1")
                        .id("https://example.org/aas/1")
                        .submodels(ReferenceBuilder.forSubmodel(submodel))
                        .build())
                .submodels(submodel)
                .build();
    }


    @After
    public void shutdown() {
        service.stop();
    }


    @Test
    public void testServiceStartInvalidAssetConnection() throws Exception {
        int http = PortHelper.findFreePort();
        int opcua = PortHelper.findFreePort();
        service = new Service(
                withAssetConnection(
                        serviceConfig(http, opcua),
                        "invalid",
                        opcua));
        service.start();
        assertServiceAvailabilityHttp(http);
    }


    @Test
    // TODO re-add once OPC UA Endpoint is updated to AAS4j
    @Ignore
    public void testServiceStartValidAssetConnection() throws Exception {
        int http = PortHelper.findFreePort();
        int opcua = PortHelper.findFreePort();
        service = new Service(
                withAssetConnection(
                        serviceConfig(http, opcua),
                        NODE_ID_SOURCE,
                        opcua));
        service.start();
        awaitAssetConnected(service);
        assertServiceAvailabilityHttp(http);
        assertTargetValue(http, SOURCE_VALUE);
    }


    @Test
    // TODO re-add once OPC UA Endpoint is updated to AAS4j
    @Ignore
    public void testServiceStartValidAssetConnectionDelayed() throws Exception {
        int http = PortHelper.findFreePort();
        int opcua = PortHelper.findFreePort();
        int http2 = PortHelper.findFreePort();
        int opcua2 = PortHelper.findFreePort();
        ServiceConfig config = withAssetConnection(serviceConfig(http, opcua),
                NODE_ID_SOURCE,
                opcua2);
        service = new Service(config);
        service.start();
        assertServiceAvailabilityHttp(http);
        assertTargetValue(http, TARGET_VALUE);
        Service service2 = new Service(serviceConfig(http2, opcua2));
        service2.start();
        assertServiceAvailabilityOpcUa(opcua2);
        awaitAssetConnected(service);
        assertTargetValue(http, SOURCE_VALUE);
        service2.stop();
    }


    private static ServiceConfig serviceConfig(int portHttp, int portOpcUa) {
        return ServiceConfig.builder()
                .core(CoreConfig.DEFAULT)
                .persistence(PersistenceInMemoryConfig.builder()
                        .initialModel(DeepCopyHelper.deepCopy(environment))
                        .build())
                .fileStorage(new FileStorageInMemoryConfig())
                // TODO re-add once OPC UA Endpoint is updated to AAS4j
                //.endpoint(OpcUaEndpointConfig.builder()
                //        .tcpPort(portOpcUa)
                //        .supportedAuthentication(UserTokenType.Anonymous)
                //        .build())
                .endpoint(HttpEndpointConfig.builder()
                        .port(portHttp)
                        .certificate(CertificateConfig.builder()
                                .keyStorePath(httpEndpointKeyStoreFile)
                                .keyStoreType(HTTP_ENDPOINT_KEYSTORE_TYPE)
                                .keyStorePassword(HTTP_ENDPOINT_KEYSTORE_PASSWORD)
                                .build())
                        .build())
                .messageBus(MessageBusInternalConfig.builder()
                        .build())
                .build();
    }


    private static ServiceConfig withAssetConnection(ServiceConfig config, String nodeIdSource, int port) throws IOException {
        config.getAssetConnections().add(OpcUaAssetConnectionConfig.builder()
                .host("opc.tcp://" + "localhost:" + port)
                .securityBaseDir(Files.createTempDirectory("asset-connection"))
                .valueProvider(AasUtils.toReference(AasUtils.toReference(submodel), target),
                        OpcUaValueProviderConfig.builder()
                                .nodeId(nodeIdSource)
                                .build())
                .build());
        return config;
    }


    private void assertServiceAvailabilityHttp(int port)
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        List<AssetAdministrationShell> expected = environment.getAssetAdministrationShells();
        assertExecutePage(
                HttpMethod.GET,
                new ApiPaths(HOST, port).aasRepository().assetAdministrationShells(),
                StatusCode.SUCCESS,
                null,
                expected,
                AssetAdministrationShell.class);
    }


    private void assertServiceAvailabilityOpcUa(int port) throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException,
            AssetConnectionException, ConfigurationInitializationException, UaException, ExecutionException {
        OpcUaClient client = OpcUaHelper.connect(OpcUaAssetConnectionConfig.builder()
                .securityBaseDir(Files.createTempDirectory("asset-connection"))
                .host("opc.tcp://" + "localhost:" + port)
                .build());
        DataValue value = OpcUaHelper.readValue(client, NODE_ID_SOURCE);
        assertEquals(SOURCE_VALUE, Integer.parseInt(value.getValue().getValue().toString()));
    }


    private void assertTargetValue(int port, int expectedValue)
            throws IOException, InterruptedException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        HttpResponse<String> response = HttpHelper.get(
                httpClient,
                new ApiPaths(HOST, port)
                        .submodelRepository()
                        .submodelInterface(submodel)
                        .submodelElement(target, Content.VALUE));
        assertEquals(toHttpStatusCode(StatusCode.SUCCESS), response.statusCode());
        String expected = String.format("{\"target\": %d}", expectedValue);
        assertThatJson(response.body())
                .when(Option.IGNORING_ARRAY_ORDER)
                .withTolerance(0)
                .isEqualTo(expected);
    }


    private <T> Page<T> assertExecutePage(HttpMethod method, String url, StatusCode statusCode, Object input, List<T> expected, Class<T> type)
            throws IOException, InterruptedException, URISyntaxException, SerializationException, DeserializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        HttpResponse response = HttpHelper.execute(httpClient, method, url, input);
        assertEquals(toHttpStatusCode(statusCode), response.statusCode());
        Page<T> actual = HttpHelper.readResponsePage(response, type);
        if (expected != null) {
            assertEquals(expected, actual.getContent());
        }
        return actual;
    }


    private void awaitAssetConnected(Service service) {
        await().atMost(30, TimeUnit.SECONDS)
                .with()
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> service.getAssetConnectionManager().getConnections().get(0).isConnected());
    }

}
