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

import java.util.List;
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
import org.junit.BeforeClass;


public class HttpEndpointWithSslDisabledTest extends AbstractHttpEndpointTest {

    @BeforeClass
    public static void init() throws Exception {
        port = PortHelper.findFreePort();
        persistence = mock(Persistence.class);
        fileStorage = mock(FileStorage.class);

        startServer();
        startClient();
    }


    private static void startServer() throws Exception {
        scheme = HttpScheme.HTTP.toString();
        endpoint = new HttpEndpoint();
        server = new Server();
        service = spy(new Service(CoreConfig.DEFAULT, persistence, fileStorage, mock(MessageBus.class), List.of(endpoint), List.of()));
        endpoint.init(
                CoreConfig.DEFAULT,
                HttpEndpointConfig.builder()
                        .port(port)
                        .cors(true)
                        .ssl(false)
                        .build(),
                service);
        server.start();
        service.start();
    }


    private static void startClient() throws Exception {
        client = new HttpClient(new HttpClientTransportDynamic(new ClientConnector()));
        client.start();
    }

}
