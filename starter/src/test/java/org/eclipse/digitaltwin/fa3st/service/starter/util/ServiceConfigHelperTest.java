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
package org.eclipse.digitaltwin.fa3st.service.starter.util;

import java.io.IOException;
import java.util.List;
import org.eclipse.digitaltwin.fa3st.service.config.CoreConfig;
import org.eclipse.digitaltwin.fa3st.service.config.ServiceConfig;
import org.eclipse.digitaltwin.fa3st.service.endpoint.EndpointConfig;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.HttpEndpoint;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.HttpEndpointConfig;
import org.eclipse.digitaltwin.fa3st.service.filestorage.memory.FileStorageInMemoryConfig;
import org.eclipse.digitaltwin.fa3st.service.messagebus.internal.MessageBusInternalConfig;
import org.eclipse.digitaltwin.fa3st.service.persistence.memory.PersistenceInMemoryConfig;
import org.eclipse.digitaltwin.fa3st.service.starter.model.ConfigOverride;
import org.eclipse.digitaltwin.fa3st.service.starter.model.ConfigOverrideSource;
import org.junit.Assert;
import org.junit.Test;


public class ServiceConfigHelperTest {

    private static ServiceConfig getConfigWithHttpEndpoint() {
        return new ServiceConfig.Builder()
                .core(new CoreConfig.Builder().requestHandlerThreadPoolSize(2).build())
                .endpoints(List.of(new HttpEndpointConfig.Builder()
                        .port(443)
                        .build()))
                .persistence(new PersistenceInMemoryConfig())
                .fileStorage(new FileStorageInMemoryConfig())
                .messageBus(new MessageBusInternalConfig())
                .build();
    }


    @Test
    public void testWithProperties() throws IOException, Exception {
        ServiceConfig input = new ServiceConfig.Builder()
                .core(new CoreConfig.Builder()
                        .requestHandlerThreadPoolSize(3)
                        .build())
                .endpoints(List.of(new EndpointConfig() {
                    public int getPort() {
                        return 1337;
                    }
                }))
                .persistence(new PersistenceInMemoryConfig())
                .fileStorage(new FileStorageInMemoryConfig())
                .messageBus(new MessageBusInternalConfig())
                .build();
        ServiceConfig expected = getConfigWithHttpEndpoint();
        ServiceConfig actual = ServiceConfigHelper.withProperties(input,
                List.of(
                        ConfigOverride.builder()
                                .originalKey(ParameterConstants.REQUEST_HANDLER_THREAD_POOL_SIZE)
                                .value(Integer.toString(expected.getCore().getRequestHandlerThreadPoolSize()))
                                .source(ConfigOverrideSource.ENV)
                                .build(),
                        ConfigOverride.builder()
                                .originalKey(ParameterConstants.ENDPOINT_0_CLASS)
                                .value(HttpEndpoint.class.getCanonicalName())
                                .source(ConfigOverrideSource.ENV)
                                .build(),
                        ConfigOverride.builder()
                                .originalKey(ParameterConstants.ENDPOINT_0_PORT)
                                .value("443")
                                .source(ConfigOverrideSource.ENV)
                                .build()));
        Assert.assertEquals(expected, actual);
    }

}
