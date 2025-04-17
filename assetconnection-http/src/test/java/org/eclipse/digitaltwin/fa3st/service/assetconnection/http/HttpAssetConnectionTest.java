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
package org.eclipse.digitaltwin.fa3st.service.assetconnection.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.request;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.net.ssl.SSLHandshakeException;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.ConfigurationInitializationException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValueFormatException;
import org.eclipse.digitaltwin.fa3st.common.model.value.DataElementValue;
import org.eclipse.digitaltwin.fa3st.common.model.value.Datatype;
import org.eclipse.digitaltwin.fa3st.common.model.value.PropertyValue;
import org.eclipse.digitaltwin.fa3st.common.model.value.TypedValue;
import org.eclipse.digitaltwin.fa3st.common.model.value.TypedValueFactory;
import org.eclipse.digitaltwin.fa3st.common.typing.ElementValueTypeInfo;
import org.eclipse.digitaltwin.fa3st.common.typing.TypeExtractor;
import org.eclipse.digitaltwin.fa3st.common.util.LambdaExceptionHelper;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetConnection;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetValueProvider;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.NewDataListener;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.common.format.JsonFormat;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.http.provider.config.HttpOperationProviderConfig;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.http.provider.config.HttpSubscriptionProviderConfig;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.http.provider.config.HttpValueProviderConfig;
import org.eclipse.digitaltwin.fa3st.service.config.CoreConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class HttpAssetConnectionTest {

    private static final long DEFAULT_TIMEOUT = 10000;
    private static final File KEY_STORE_FILE = new File("src/test/resources/dummy-self-signed-certificate.p12");
    private static final String KEY_PASSWORD = "changeit";
    private static final String KEY_STORE_PASSWORD = "changeit";
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final Reference REFERENCE = ReferenceHelper.parseReference("(Property)[ID_SHORT]Temperature");

    private URL httpUrl;
    private URL httpsUrl;

    @Rule
    public WireMockRule server = new WireMockRule(options()
            .dynamicHttpsPort()
            .dynamicPort()
            .httpDisabled(false)
            .keystoreType(KEYSTORE_TYPE)
            .keystorePath(KEY_STORE_FILE.getAbsolutePath())
            .keystorePassword(KEY_PASSWORD)
            .keyManagerPassword(KEY_STORE_PASSWORD));

    @Before
    public void init() throws MalformedURLException {
        httpUrl = new URL("http", "localhost", server.port(), "");
        httpsUrl = new URL("https", "localhost", server.httpsPort(), "");
    }


    @Test
    public void testValueProviderPropertySetValueWithTemplateJSONHttps() throws AssetConnectionException,
            ConfigurationInitializationException,
            ValueFormatException,
            ResourceNotFoundException, PersistenceException {
        String template = "{\"foo\" : \"${value}\", \"bar\": [1, 2, 3]}";
        String value = "5";
        assertValueProviderPropertyWriteJson(
                PropertyValue.of(Datatype.INT, "5"),
                template,
                template.replaceAll("\\$\\{value\\}", value),
                true);
    }


    @Test
    public void testValueProviderWithHeaders()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertValueProviderHeaders(Map.of(), Map.of(), Map.of(), false);
        assertValueProviderHeaders(Map.of("foo", "bar"), Map.of(), Map.of("foo", "bar"), false);
        assertValueProviderHeaders(Map.of("foo", "bar"), Map.of("foo", "bar"), Map.of("foo", "bar"), false);
        assertValueProviderHeaders(Map.of("foo", "bar"), Map.of("foo", "bar2"), Map.of("foo", "bar2"), false);
        assertValueProviderHeaders(Map.of("foo", "bar"), Map.of("bar", "foo"), Map.of("foo", "bar", "bar", "foo"), false);
    }


    @Test
    public void testValueProviderPropertyGetValueJSON()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertValueProviderPropertyReadJson(
                PropertyValue.of(Datatype.INT, "5"),
                "5",
                null,
                false);
    }


    @Test
    public void testHttpsUntrusted() {
        HttpAssetConnectionConfig config = HttpAssetConnectionConfig.builder()
                .baseUrl(httpsUrl)
                .build();
        AssetConnectionException exception = Assert.assertThrows(
                AssetConnectionException.class,
                () -> assertValueProviderPropertyReadJson(
                        PropertyValue.of(Datatype.INT, "5"),
                        "5",
                        null,
                        config));
        Throwable cause = exception.getCause();
        while (Objects.nonNull(cause)) {
            if (SSLHandshakeException.class.isAssignableFrom(cause.getClass())) {
                return;
            }
            cause = cause.getCause();
        }
        Assert.fail("Expected SSLHandshakeException but none found");
    }


    @Test
    public void testValueProviderPropertyGetValueWithQueryJSON()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertValueProviderPropertyReadJson(
                PropertyValue.of(Datatype.INT, "5"),
                "{\"foo\" : [1, 2, 5]}",
                "$.foo[2]",
                false);
    }


    @Test
    public void testValueProviderPropertySetValueJSON()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertValueProviderPropertyWriteJson(
                PropertyValue.of(Datatype.INT, "5"),
                null,
                "5",
                false);
    }


    @Test
    public void testValueProviderPropertySetValueWithTemplateJSON()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        String template = "{\"foo\" : \"${value}\", \"bar\": [1, 2, 3]}";
        String value = "5";
        assertValueProviderPropertyWriteJson(
                PropertyValue.of(Datatype.INT, "5"),
                template,
                template.replaceAll("\\$\\{value\\}", value),
                false);
    }


    @Test
    public void testSubscriptionProviderPropertyJsonGET()
            throws AssetConnectionException, ConfigurationInitializationException, InterruptedException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertSubscriptionProviderPropertyJson(
                Datatype.INT,
                RequestMethod.GET,
                List.of("{ \"value\": 1}",
                        "{ \"value\": 2}"),
                "$.value",
                //null,
                false,
                PropertyValue.of(Datatype.INT, "1"),
                PropertyValue.of(Datatype.INT, "2"));
    }


    @Test
    public void testSubscriptionProviderPropertyJsonGET2()
            throws AssetConnectionException, ConfigurationInitializationException, InterruptedException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertSubscriptionProviderPropertyJson(
                Datatype.INT,
                RequestMethod.GET,
                List.of("{\n"
                        + "	\"data\": [\n"
                        + "		{\n"
                        + "			\"value\": 42\n"
                        + "		}\n"
                        + "	]\n"
                        + "}"),
                "$.data[-1:].value",
                //null,
                false,
                PropertyValue.of(Datatype.INT, "42"));
    }


    @Test
    public void testSubscriptionProviderPropertyJsonPOST()
            throws AssetConnectionException, ConfigurationInitializationException, InterruptedException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertSubscriptionProviderPropertyJson(
                Datatype.INT,
                RequestMethod.GET,
                List.of("{ \"value\": 1}",
                        "{ \"value\": 2}"),
                "$.value",
                //"{ \"input\": \"foo\"}",
                false,
                PropertyValue.of(Datatype.INT, "1"),
                PropertyValue.of(Datatype.INT, "2"));
    }


    @Test
    public void testOperationProviderPropertyJsonPOSTNoParameters() throws AssetConnectionException,
            ConfigurationInitializationException,
            ResourceNotFoundException, PersistenceException {
        assertOperationProviderPropertyJson(
                RequestMethod.POST,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false);
    }


    @Test
    public void testOperationProviderPropertyJsonPOSTInputOnly()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertOperationProviderPropertyJson(
                RequestMethod.POST,
                "{ \"parameters\": { \"in1\": ${in1} }}",
                "{ \"parameters\": { \"in1\": \"foo\" }}",
                null,
                null,
                Map.of("in1", TypedValueFactory.create(Datatype.STRING, "foo")),
                null,
                null,
                null,
                false);
    }


    @Test
    public void testOperationProviderPropertyJsonPOSTOutputOnly()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertOperationProviderPropertyJson(
                RequestMethod.POST,
                null,
                null,
                "{ \"result\": 1.5 }",
                Map.of("out1", "$.result"),
                null,
                null,
                Map.of("out1", TypedValueFactory.create(Datatype.DOUBLE, "1.5")),
                null,
                false);
    }


    @Test
    public void testOperationProviderPropertyJsonPOSTInputOutputOnly()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertOperationProviderPropertyJson(
                RequestMethod.POST,
                "{ \"parameters\": { \"in1\": ${in1}, \"in2\": ${in2} }}",
                "{ \"parameters\": { \"in1\": 42, \"in2\": 17 }}",
                "{ \"result\": 25 }",
                Map.of("out1", "$.result"),
                Map.of("in1", TypedValueFactory.create(Datatype.INTEGER, "42"),
                        "in2", TypedValueFactory.create(Datatype.INTEGER, "17")),
                null,
                Map.of("out1", TypedValueFactory.create(Datatype.INTEGER, "25")),
                null,
                false);
    }


    @Test
    public void testOperationProviderPropertyJsonPOSTInoutputOnly()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertOperationProviderPropertyJson(
                RequestMethod.POST,
                "{ \"parameters\": { \"inout1\": ${inout1}}}",
                "{ \"parameters\": { \"inout1\": 42}}",
                "{ \"result\": { \"inout1\": 17}}",
                Map.of("inout1", "$.result.inout1"),
                null,
                Map.of("inout1", TypedValueFactory.create(Datatype.INTEGER, "42")),
                null,
                Map.of("inout1", TypedValueFactory.create(Datatype.INTEGER, "17")),
                false);
    }


    @Test
    public void testOperationProviderPropertyJsonPOST()
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        assertOperationProviderPropertyJson(
                RequestMethod.POST,
                "{ \"parameters\": { \"in1\": ${in1}, \"inout1\": ${inout1} }}",
                "{ \"parameters\": { \"in1\": 1, \"inout1\": 2 }}",
                "{ \"result\": 3, \"modified\": { \"inout1\": 4 }}",
                Map.of("out1", "$.result",
                        "inout1", "$.modified.inout1"),
                Map.of("in1", TypedValueFactory.create(Datatype.INT, "1")),
                Map.of("inout1", TypedValueFactory.create(Datatype.INT, "2")),
                Map.of("out1", TypedValueFactory.create(Datatype.INT, "3")),
                Map.of("inout1", TypedValueFactory.create(Datatype.INT, "4")),
                false);
    }


    private void assertValueProviderHeaders(
                                            Map<String, String> connectionHeaders,
                                            Map<String, String> providerHeaders,
                                            Map<String, String> expectedHeaders,
                                            boolean useHttps)
            throws AssetConnectionException, ConfigurationInitializationException, ValueFormatException, ResourceNotFoundException, PersistenceException {
        PropertyValue value = PropertyValue.of(Datatype.INT, "5");
        assertValueProviderPropertyJson(
                value.getValue().getDataType(),
                RequestMethod.GET,
                connectionHeaders,
                providerHeaders,
                value.getValue().asString(),
                null,
                null,
                useHttps,
                x -> {
                    RequestPatternBuilder result = x;
                    if (expectedHeaders != null) {
                        for (var expectedHeader: expectedHeaders.entrySet()) {
                            result = result.withHeader(expectedHeader.getKey(), equalTo(expectedHeader.getValue()));
                        }
                    }
                    return result;
                },
                LambdaExceptionHelper.rethrowConsumer(x -> Assert.assertEquals(value, x.getValue())));
    }


    private void assertValueProviderPropertyReadJson(PropertyValue expected, String httpResponseBody, String query, boolean useHttps)
            throws AssetConnectionException, ConfigurationInitializationException, ResourceNotFoundException, PersistenceException {

        assertValueProviderPropertyJson(
                expected.getValue().getDataType(),
                RequestMethod.GET,
                null,
                null,
                httpResponseBody,
                query,
                null,
                useHttps,
                x -> x.withHeader(HttpHeaders.CONTENT_TYPE, equalTo(ContentType.APPLICATION_JSON.getMimeType())),
                LambdaExceptionHelper.rethrowConsumer(x -> Assert.assertEquals(expected, x.getValue())));
    }


    private void assertValueProviderPropertyReadJson(PropertyValue expected, String httpResponseBody, String query, HttpAssetConnectionConfig config)
            throws AssetConnectionException, ConfigurationInitializationException, ResourceNotFoundException, PersistenceException {
        assertValueProviderPropertyJson(
                expected.getValue().getDataType(),
                RequestMethod.GET,
                null,
                httpResponseBody,
                query,
                null,
                config,
                x -> x.withHeader(HttpHeaders.CONTENT_TYPE, equalTo(ContentType.APPLICATION_JSON.getMimeType())),
                LambdaExceptionHelper.rethrowConsumer(x -> {
                    try {
                        Assert.assertEquals("Unexpected property value.", expected, x.getValue());
                    }
                    catch (AssertionError e) {
                        throw new AssertionError("Assertion failed in assertValueProviderPropertyReadJson().", e);
                    }
                }));
    }


    private void assertValueProviderPropertyWriteJson(PropertyValue newValue, String template, String expectedResponseBody, boolean useHttps)
            throws AssetConnectionException, ConfigurationInitializationException, ResourceNotFoundException, PersistenceException {
        assertValueProviderPropertyJson(
                newValue.getValue().getDataType(),
                RequestMethod.PUT,
                null,
                null,
                null,
                null,
                template,
                useHttps,
                x -> x.withRequestBody(equalToJson(expectedResponseBody)),
                LambdaExceptionHelper.rethrowConsumer(x -> x.setValue(newValue)));
    }


    private void awaitConnection(AssetConnection connection) {
        await().atMost(60, TimeUnit.SECONDS)
                .with()
                .pollInterval(2, TimeUnit.SECONDS)
                .until(() -> {
                    try {
                        connection.connect();
                    }
                    catch (AssetConnectionException e) {
                        // do nothing
                    }
                    return connection.isConnected();
                });
    }


    private HttpAssetConnectionConfig createAssetConnectionConfig(Map<String, String> connectionHeaders, boolean useHttps) {
        HttpAssetConnectionConfig result = HttpAssetConnectionConfig.builder()
                .headers(connectionHeaders != null ? connectionHeaders : Map.of())
                .baseUrl(httpUrl)
                .build();
        if (useHttps) {
            result.setBaseUrl(httpsUrl);
            result.getTrustedCertificates().setKeyStorePath(KEY_STORE_FILE.getAbsolutePath());
            result.getTrustedCertificates().setKeyStorePassword(KEY_PASSWORD);
        }
        return result;
    }


    private void assertValueProviderPropertyJson(Datatype datatype,
                                                 RequestMethod method,
                                                 Map<String, String> connectionHeaders,
                                                 Map<String, String> providerHeaders,
                                                 String httpResponseBody,
                                                 String query,
                                                 String template,
                                                 boolean useHttps,
                                                 Function<RequestPatternBuilder, RequestPatternBuilder> verifierModifier,
                                                 Consumer<AssetValueProvider> customAssert)
            throws AssetConnectionException, ConfigurationInitializationException, ResourceNotFoundException, PersistenceException {
        assertValueProviderPropertyJson(
                datatype,
                method,
                providerHeaders,
                httpResponseBody,
                query,
                template,
                createAssetConnectionConfig(connectionHeaders, useHttps),
                verifierModifier,
                customAssert);
    }


    private void assertValueProviderPropertyJson(Datatype datatype,
                                                 RequestMethod method,
                                                 Map<String, String> providerHeaders,
                                                 String httpResponseBody,
                                                 String query,
                                                 String template,
                                                 HttpAssetConnectionConfig config,
                                                 Function<RequestPatternBuilder, RequestPatternBuilder> verifierModifier,
                                                 Consumer<AssetValueProvider> customAssert)
            throws AssetConnectionException, ConfigurationInitializationException, ResourceNotFoundException, PersistenceException {
        ServiceContext serviceContext = mock(ServiceContext.class);
        doReturn(ElementValueTypeInfo.builder()
                .type(PropertyValue.class)
                .datatype(datatype)
                .build())
                .when(serviceContext)
                .getTypeInfo(REFERENCE);
        String path = String.format("/test/random/%s", UUID.randomUUID());
        stubFor(request(method.getName(), urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                        .withBody(httpResponseBody)));
        config.getValueProviders().put(REFERENCE, HttpValueProviderConfig.builder()
                .path(path)
                .headers(providerHeaders != null ? providerHeaders : Map.of())
                .format(JsonFormat.KEY)
                .query(query)
                .template(template)
                .build());
        HttpAssetConnection connection = new HttpAssetConnection(
                CoreConfig.builder().build(),
                config,
                serviceContext);
        awaitConnection(connection);
        try {
            if (customAssert != null) {
                customAssert.accept(connection.getValueProviders().get(REFERENCE));
            }
            RequestPatternBuilder verifier = new RequestPatternBuilder(method, urlEqualTo(path));
            if (verifierModifier != null) {
                verifier = verifierModifier.apply(verifier);
            }
            verify(exactly(1), verifier);
        }
        finally {
            connection.disconnect();
        }
    }


    private void assertSubscriptionProviderPropertyJson(Datatype datatype,
                                                        RequestMethod method,
                                                        List<String> httpResponseBodies,
                                                        String query,
                                                        boolean useHttps,
                                                        PropertyValue... expected)
            throws AssetConnectionException, ConfigurationInitializationException, InterruptedException, ResourceNotFoundException, PersistenceException {
        ServiceContext serviceContext = mock(ServiceContext.class);
        doReturn(ElementValueTypeInfo.builder()
                .type(PropertyValue.class)
                .datatype(datatype)
                .build())
                .when(serviceContext)
                .getTypeInfo(REFERENCE);
        String id = UUID.randomUUID().toString();
        String path = String.format("/test/random/%s", id);
        if (httpResponseBodies != null && !httpResponseBodies.isEmpty()) {
            stubFor(request(method.getName(), urlEqualTo(path))
                    .inScenario(id)
                    .whenScenarioStateIs(Scenario.STARTED)
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                            .withBody(httpResponseBodies.get(0)))
                    .willSetStateTo("1"));
            for (int i = 1; i < httpResponseBodies.size(); i++) {
                stubFor(request(method.getName(), urlEqualTo(path))
                        .inScenario(id)
                        .whenScenarioStateIs(Objects.toString(i))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                                .withBody(httpResponseBodies.get(i)))
                        .willSetStateTo(Objects.toString(i + 1)));
            }
        }
        HttpAssetConnectionConfig config = createAssetConnectionConfig(null, useHttps);
        config.getSubscriptionProviders().put(REFERENCE, HttpSubscriptionProviderConfig.builder()
                .interval(1000)
                .path(path)
                .format(JsonFormat.KEY)
                .query(query)
                .build());
        HttpAssetConnection connection = new HttpAssetConnection(
                CoreConfig.builder()
                        .build(),
                config,
                serviceContext);
        awaitConnection(connection);
        NewDataListener listener = null;
        try {
            CountDownLatch condition = new CountDownLatch(expected.length);
            final PropertyValue[] actual = new PropertyValue[expected.length];
            final AtomicInteger pointer = new AtomicInteger(0);
            listener = (DataElementValue data) -> {
                if (pointer.get() <= expected.length) {
                    actual[pointer.getAndIncrement()] = ((PropertyValue) data);
                    condition.countDown();
                }
            };
            connection.getSubscriptionProviders().get(REFERENCE).addNewDataListener(listener);
            condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            connection.getSubscriptionProviders().get(REFERENCE).removeNewDataListener(listener);
            Assert.assertArrayEquals(expected, actual);
            RequestPatternBuilder verifier = new RequestPatternBuilder(method, urlEqualTo(path));
            verify(exactly(httpResponseBodies.size()), verifier);
        }
        finally {
            connection.disconnect();
        }
    }


    private void assertOperationProviderPropertyJson(
                                                     RequestMethod method,
                                                     String template,
                                                     String expectedRequestToAsset,
                                                     String assetResponse,
                                                     Map<String, String> queries,
                                                     Map<String, TypedValue> input,
                                                     Map<String, TypedValue> inoutput,
                                                     Map<String, TypedValue> expectedOutput,
                                                     Map<String, TypedValue> expectedInoutput,
                                                     boolean useHttps)
            throws AssetConnectionException, ResourceNotFoundException, ConfigurationInitializationException, PersistenceException {
        ServiceContext serviceContext = mock(ServiceContext.class);
        OperationVariable[] output = toOperationVariables(expectedOutput);
        doReturn(output)
                .when(serviceContext)
                .getOperationOutputVariables(REFERENCE);
        if (output != null) {
            Stream.of(output).forEach(x -> {
                try {
                    doReturn(TypeExtractor.extractTypeInfo(x.getValue()))
                            .when(serviceContext)
                            .getTypeInfo(AasUtils.toReference(REFERENCE, x.getValue()));
                }
                catch (ResourceNotFoundException | PersistenceException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        String path = String.format("/test/random/%s", "foo");
        stubFor(request(method.getName(), urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                        .withBody(assetResponse)));
        HttpAssetConnectionConfig config = createAssetConnectionConfig(null, useHttps);
        config.getOperationProviders().put(REFERENCE, HttpOperationProviderConfig.builder()
                .method(method.toString())
                .path(path)
                .queries(queries)
                .format(JsonFormat.KEY)
                .template(template)
                .build());
        HttpAssetConnection connection = new HttpAssetConnection(
                CoreConfig.builder()
                        .build(),
                config,
                serviceContext);
        awaitConnection(connection);
        try {
            OperationVariable[] actualInput = toOperationVariables(input);
            OperationVariable[] actualInoutput = toOperationVariables(inoutput);
            OperationVariable[] actualOutput = connection.getOperationProviders().get(REFERENCE).invoke(actualInput, actualInoutput);
            // assert output is as correct
            Assert.assertArrayEquals(output, actualOutput);
            // assert inoutput is correct
            Assert.assertArrayEquals(toOperationVariables(expectedInoutput), actualInoutput);
            // assert correct HTTP request to asset has been made
            RequestPatternBuilder verifier = new RequestPatternBuilder(method, urlEqualTo(path));
            if (expectedRequestToAsset != null) {
                verifier = verifier.withHeader(HttpHeaders.CONTENT_TYPE, equalTo(ContentType.APPLICATION_JSON.getMimeType()))
                        .withRequestBody(equalToJson(expectedRequestToAsset));
            }
            verify(exactly(1), verifier);
        }
        finally {
            connection.disconnect();
        }
    }


    private OperationVariable[] toOperationVariables(Map<String, TypedValue> values) {
        if (values == null) {
            return new OperationVariable[0];
        }
        return values.entrySet().stream()
                .map(x -> new DefaultOperationVariable.Builder()
                        .value(new DefaultProperty.Builder()
                                .idShort(x.getKey())
                                .value(x.getValue().asString())
                                .valueType(x.getValue().getDataType().getAas4jDatatype())
                                .build())
                        .build())
                .toArray(OperationVariable[]::new);
    }
}
