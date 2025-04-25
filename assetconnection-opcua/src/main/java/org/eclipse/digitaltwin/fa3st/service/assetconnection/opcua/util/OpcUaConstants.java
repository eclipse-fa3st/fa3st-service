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
package org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.util;

import org.eclipse.digitaltwin.fa3st.common.certificate.CertificateInformation;


/**
 * Constant values related to OPC UA.
 */
public class OpcUaConstants {

    public static final String NODE_ID_SEPARATOR = ";";
    public static final String IP_LOCALHOST = "127.0.0.1";
    public static final String DNS_LOCALHOST = "localhost";
    public static final String CERTIFICATE_APPLICATION_URI = "urn:org.eclipse.digitaltwin.fa3st:service:assetconnection:opcua";
    public static final String CERTIFICATE_APPLICATION_NAME = "FA³ST Service OPC UA Asset Connection";
    public static final String CERTIFICATE_ORGANIZATION = "Eclipse FA3ST";
    public static final String CERTIFICATE_LOCALITY = "Brussels";
    public static final String CERTIFICATE_COUNTRYCODE = "BE";

    public static final CertificateInformation DEFAULT_APPLICATION_CERTIFICATE_INFO = CertificateInformation.builder()
            .applicationUri(CERTIFICATE_APPLICATION_URI)
            .commonName(CERTIFICATE_APPLICATION_NAME)
            .countryCode(CERTIFICATE_COUNTRYCODE)
            .localityName(CERTIFICATE_LOCALITY)
            .organization(CERTIFICATE_ORGANIZATION)
            .ipAddress(IP_LOCALHOST)
            .dnsName(DNS_LOCALHOST)
            .build();

    public static final CertificateInformation DEFAULT_AUTHENTICATION_CERTIFICATE_INFO = CertificateInformation.builder()
            .applicationUri(CERTIFICATE_APPLICATION_URI)
            .commonName(CERTIFICATE_APPLICATION_NAME)
            .countryCode(CERTIFICATE_COUNTRYCODE)
            .localityName(CERTIFICATE_LOCALITY)
            .organization(CERTIFICATE_ORGANIZATION)
            .autodetectDnsAndIp()
            .build();

    /**
     * Hide the implicit public constructor.
     */
    private OpcUaConstants() {

    }
}
