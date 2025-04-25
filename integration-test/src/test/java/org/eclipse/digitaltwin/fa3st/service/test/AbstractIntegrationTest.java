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

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import org.eclipse.digitaltwin.fa3st.common.certificate.CertificateInformation;
import org.eclipse.digitaltwin.fa3st.common.util.KeyStoreHelper;
import org.eclipse.digitaltwin.fa3st.common.util.PortHelper;
import org.eclipse.digitaltwin.fa3st.common.util.SslHelper;
import org.junit.BeforeClass;


/**
 * Abstract base class for integration tests that are using FA³ST Service. This class generates a certificate for the
 * HTTP endpoint and offers a HTTP connection pre-configured with that certificate to talk to the FA³ST Service.
 */
public class AbstractIntegrationTest {

    protected static final String HOST = "https://localhost";
    protected static int PORT;
    protected static final String HTTP_ENDPOINT_KEYSTORE_TYPE = "PKCS12";
    protected static final String HTTP_ENDPOINT_KEYSTORE_PASSWORD = "random-pw";
    protected static final CertificateInformation HTTP_ENDPOINT_KEYSTORE_CERTIFICATE_INFORMATION = CertificateInformation.builder()
            .applicationUri("urn:org:eclipse:digitaltwin:fa3st:service:endpoint:http:integration-test")
            .commonName("FA³ST Service HTTP Endpoint - Integration Test")
            .countryCode("BE")
            .localityName("Brussels")
            .organization("Eclipse FA3ST")
            .build();

    protected static File httpEndpointKeyStoreFile;
    protected static HttpClient httpClient;

    @BeforeClass
    public static void initialize() throws IOException, GeneralSecurityException {
        PORT = PortHelper.findFreePort();
        generateHttpEndpointCertificate();
        httpClient = SslHelper.disableHostnameVerification(
                HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NEVER)
                        .sslContext(SslHelper.newContextAcceptingCertificates(
                                httpEndpointKeyStoreFile,
                                HTTP_ENDPOINT_KEYSTORE_TYPE,
                                HTTP_ENDPOINT_KEYSTORE_PASSWORD)));
    }


    private static void generateHttpEndpointCertificate() throws IOException, GeneralSecurityException {
        httpEndpointKeyStoreFile = Files.createTempFile("http-endpoint-cert", "").toFile();
        httpEndpointKeyStoreFile.deleteOnExit();
        KeyStoreHelper.save(
                KeyStoreHelper.generateSelfSigned(HTTP_ENDPOINT_KEYSTORE_CERTIFICATE_INFORMATION),
                httpEndpointKeyStoreFile,
                HTTP_ENDPOINT_KEYSTORE_TYPE,
                null,
                HTTP_ENDPOINT_KEYSTORE_PASSWORD,
                HTTP_ENDPOINT_KEYSTORE_PASSWORD);
    }
}
