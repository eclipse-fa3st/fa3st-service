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
package org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.provider;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.server.EmbeddedOpcUaServer;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.server.EmbeddedOpcUaServerConfig;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.server.EndpointSecurityConfiguration;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.server.Protocol;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.junit.Test;


public class OpcUaValueProviderTest {

    @Test
    public void testEquals() throws Exception {
        EmbeddedOpcUaServer server = new EmbeddedOpcUaServer(
                EmbeddedOpcUaServerConfig.builder().endpointSecurityConfiguration(EndpointSecurityConfiguration.NO_SECURITY_ANONYMOUS).build());
        server.startup();
        try {
            OpcUaClient client1 = OpcUaClient.create(server.getEndpoint(Protocol.TCP));
            OpcUaClient client2 = OpcUaClient.create(server.getEndpoint(Protocol.TCP));
            EqualsVerifier.simple().forClass(OpcUaValueProvider.class)
                    .withPrefabValues(OpcUaClient.class, client1, client2)
                    .verify();
            client1.disconnect();
            client2.disconnect();
        }
        finally {
            server.shutdown();
        }
    }

}
