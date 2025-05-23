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
package org.eclipse.digitaltwin.fa3st.service.endpoint.http;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import org.eclipse.digitaltwin.fa3st.common.certificate.CertificateConfig;
import org.eclipse.digitaltwin.fa3st.common.certificate.CertificateInformation;
import org.eclipse.digitaltwin.fa3st.common.util.KeyStoreHelper;
import org.eclipse.digitaltwin.fa3st.common.util.PortHelper;
import org.eclipse.digitaltwin.fa3st.service.Service;
import org.eclipse.digitaltwin.fa3st.service.config.CoreConfig;
import org.eclipse.digitaltwin.fa3st.service.filestorage.FileStorage;
import org.eclipse.digitaltwin.fa3st.service.messagebus.MessageBus;
import org.eclipse.digitaltwin.fa3st.service.persistence.Persistence;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.transport.HttpClientTransportDynamic;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.BeforeClass;


public class HttpEndpointWithProvidedCertificateTest extends AbstractHttpEndpointTest {

    private static final String DEFAULT_KEY_STORE_TYPE = "PKCS12";
    private static final CertificateInformation SELFSIGNED_CERTIFICATE_INFORMATION = CertificateInformation.builder()
            .applicationUri("urn:org:eclipse:digitaltwin:fa3st:service:endpoint:http:test")
            .commonName("FA³ST Service HTTP Endpoint - Unit Test")
            .countryCode("BE")
            .localityName("Brussels")
            .organization("Eclipse FA3ST")
            .build();
    private static final String KEYSTORE_PASSWORD = "password";
    private static File keyStoreTempFile;

    @BeforeClass
    public static void init() throws Exception {
        port = PortHelper.findFreePort();
        persistence = mock(Persistence.class);
        fileStorage = mock(FileStorage.class);
        generateCertificate();
        startServer();
        startClient();
    }


    private static void generateCertificate() throws Exception {
        keyStoreTempFile = Files.createTempFile("http-endpoint", "https-cert").toFile();
        keyStoreTempFile.deleteOnExit();
        KeyStoreHelper.save(
                KeyStoreHelper.generateSelfSigned(SELFSIGNED_CERTIFICATE_INFORMATION),
                keyStoreTempFile,
                DEFAULT_KEY_STORE_TYPE,
                null,
                KEYSTORE_PASSWORD,
                KEYSTORE_PASSWORD);
    }


    private static void startServer() throws Exception {
        scheme = HttpScheme.HTTPS.toString();
        endpoint = new HttpEndpoint();
        server = new Server();
        service = spy(new Service(CoreConfig.DEFAULT, persistence, fileStorage, mock(MessageBus.class), List.of(endpoint), List.of()));
        endpoint.init(
                CoreConfig.DEFAULT,
                HttpEndpointConfig.builder()
                        .port(port)
                        .cors(true)
                        .certificate(CertificateConfig.builder()
                                .keyStorePath(keyStoreTempFile)
                                .keyStorePassword(KEYSTORE_PASSWORD)
                                .build())
                        .build(),
                service);
        server.start();
        service.start();
    }


    private static void startClient() throws Exception {
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        sslContextFactory.setKeyStorePath(keyStoreTempFile.getAbsolutePath());
        sslContextFactory.setKeyStorePassword(KEYSTORE_PASSWORD);
        ClientConnector clientConnector = new ClientConnector();
        clientConnector.setSslContextFactory(sslContextFactory);
        client = new HttpClient(new HttpClientTransportDynamic(clientConnector));
        client.start();
    }
}
