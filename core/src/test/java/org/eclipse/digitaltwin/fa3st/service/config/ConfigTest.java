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
package org.eclipse.digitaltwin.fa3st.service.config;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import net.javacrumbs.jsonunit.core.Option;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonMapperFactory;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.SimpleAbstractTypeResolverFactory;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceHelper;
import org.eclipse.digitaltwin.fa3st.service.config.fixtures.DummyAssetConnectionConfig;
import org.eclipse.digitaltwin.fa3st.service.config.fixtures.DummyNodeBasedProviderConfig;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class ConfigTest {

    private static final File CONFIG_FILE = new File("src/test/resources/dummy-config.json");
    private static ServiceConfig config;
    private static ObjectMapper mapper;

    @BeforeClass
    public static void init() {
        DummyAssetConnectionConfig assetConnection = new DummyAssetConnectionConfig();
        assetConnection.setHost("tcp://localhost");
        assetConnection.setPort(1234);
        DummyNodeBasedProviderConfig valueProvider = new DummyNodeBasedProviderConfig();
        valueProvider.setNodeId("some.opc.ua.node.id");
        assetConnection.getValueProviders().put(ReferenceHelper.parse("(Property)Temperature"), valueProvider);
        config = ServiceConfig.builder()
                .core(CoreConfig.builder()
                        .requestHandlerThreadPoolSize(2)
                        .build())
                .assetConnection(assetConnection)
                .build();
        mapper = new JsonMapperFactory().create(new SimpleAbstractTypeResolverFactory().create())
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);;
    }


    @Test
    public void testSerialization() throws JsonProcessingException, IOException {
        String expected = Files.readString(CONFIG_FILE.toPath());
        String actual = mapper.writeValueAsString(config);
        assertThatJson(actual)
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expected);
    }


    @Test
    public void testDeserialization() throws JsonProcessingException, IOException {
        ServiceConfig actual = mapper.readValue(CONFIG_FILE, ServiceConfig.class);
        Assert.assertEquals(config, actual);
    }
}
